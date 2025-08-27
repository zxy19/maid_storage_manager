package studio.fantasyit.maid_storage_manager.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.craft.CraftManager;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

public class ItemStackUtil {
    static Codec<ItemStack> CODEC_UNLIMITED = Codec.lazyInitialized(() -> RecordCodecBuilder.create((p_347288_) -> p_347288_.group(
                    ItemStack.ITEM_NON_AIR_CODEC.fieldOf("id").forGetter(ItemStack::getItemHolder),
                    Codec.INT.fieldOf("count").orElse(1).forGetter(ItemStack::getCount),
                    DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(ItemStack::getComponentsPatch)
            ).apply(p_347288_, ItemStack::new))
    );
    public static Codec<ItemStack> OPTIONAL_CODEC_UNLIMITED = ExtraCodecs.optionalEmptyMap(CODEC_UNLIMITED)
            .xmap((p_330099_) -> p_330099_.orElse(ItemStack.EMPTY), (p_330101_) -> p_330101_.isEmpty() ? Optional.empty() : Optional.of(p_330101_));


    public static StreamCodec<RegistryFriendlyByteBuf, ItemStack> OPTIONAL_STREAM_CODEC = new StreamCodec<>() {
        private static final StreamCodec<RegistryFriendlyByteBuf, Holder<Item>> ITEM_STREAM_CODEC;

        public ItemStack decode(RegistryFriendlyByteBuf p_320491_) {
            int i = p_320491_.readInt();
            if (i <= 0) {
                return ItemStack.EMPTY;
            } else {
                Holder<Item> holder = ITEM_STREAM_CODEC.decode(p_320491_);
                DataComponentPatch datacomponentpatch = DataComponentPatch.STREAM_CODEC.decode(p_320491_);
                return new ItemStack(holder, i, datacomponentpatch);
            }
        }

        public void encode(RegistryFriendlyByteBuf p_320527_, ItemStack p_320873_) {
            if (p_320873_.isEmpty()) {
                p_320527_.writeInt(0);
            } else {
                p_320527_.writeInt(p_320873_.getCount());
                ITEM_STREAM_CODEC.encode(p_320527_, p_320873_.getItemHolder());
                DataComponentPatch.STREAM_CODEC.encode(p_320527_, p_320873_.getComponentsPatch());
            }

        }

        static {
            ITEM_STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.ITEM);
        }
    };

    public static boolean isSame(ItemStack stack1, ItemStack stack2, boolean matchTag) {
        if (stack1.isEmpty() && stack2.isEmpty()) return true;
        if (stack1.isEmpty() || stack2.isEmpty()) return false;
        if (matchTag)
            return ItemStack.isSameItemSameTags(stack1, stack2);
        return ItemStack.isSameItem(stack1, stack2);
    }

    public static TagKey<Item> MatchItem = TagKey.create(ForgeRegistries.ITEMS.getRegistryKey(), new ResourceLocation(MaidStorageManager.MODID, "no_nbt"));
    public static TagKey<Item> NoMatchItem = TagKey.create(ForgeRegistries.ITEMS.getRegistryKey(), new ResourceLocation(MaidStorageManager.MODID, "use_nbt"));


    public static boolean isSameInCrafting(ItemStack stack1, ItemStack stack2) {
        if (stack1.getItem() != stack2.getItem()) return false;
        if (stack1.isEmpty() || stack2.isEmpty()) return true;
        boolean matchTag = Config.craftingMatchTag;
        if (stack1.is(MatchItem)) matchTag = false;
        if (stack1.is(NoMatchItem)) matchTag = true;
        if (!matchTag) return true;
        Optional<Boolean> specialPredicator = CraftManager.getInstance().predicateItemStack(stack1, stack2);
        return specialPredicator.orElseGet(() -> isSameTagInCrafting(stack1, stack2));
    }

    public static boolean isSameTagInCrafting(ItemStack stack1, ItemStack stack2) {
        CompoundTag tag1 = Optional.ofNullable(stack1.getTag()).map(CompoundTag::copy).orElseGet(CompoundTag::new);
        CompoundTag tag2 = Optional.ofNullable(stack2.getTag()).map(CompoundTag::copy).orElseGet(CompoundTag::new);
        for (String tagPath : Config.noMatchPaths) {
            String[] path = tagPath.split("[\\.\\[]");
            tag1 = CompoundTagUtil.removeKeyFrom(tag1, path, 0);
            tag2 = CompoundTagUtil.removeKeyFrom(tag2, path, 0);
        }
        return tag1.equals(tag2);
    }

    public static ItemStack removeIsMatchInList(List<ItemStack> list, ItemStack itemStack, boolean matchTag) {
        return removeIsMatchInList(list, itemStack, (a, b) -> isSame(a, b, matchTag));
    }

    public static ItemStack removeIsMatchInList(List<ItemStack> list, ItemStack itemStack, BiFunction<ItemStack, ItemStack, Boolean> isMatch) {
        for (int i = 0; i < list.size(); i++) {
            ItemStack item = list.get(i);
            if (isMatch.apply(item, itemStack)) {
                int costCount = Math.min(item.getCount(), itemStack.getCount());
                item.shrink(costCount);
                itemStack.shrink(costCount);
                if (item.isEmpty()) {
                    list.remove(i);
                    i--;
                }
            }
            if (itemStack.isEmpty())
                return ItemStack.EMPTY;
        }
        return itemStack;
    }

    public static ItemStack addToList(List<ItemStack> list, ItemStack itemStack, boolean matchTag) {
        return ItemStackUtil.addToList(list, itemStack, (a, b) -> isSame(a, b, matchTag));
    }

    public static ItemStack addToList(List<ItemStack> list, ItemStack itemStack, BiFunction<ItemStack, ItemStack, Boolean> isMatch) {
        for (int i = 0; i < list.size(); i++) {
            ItemStack item = list.get(i);
            if (isMatch.apply(item, itemStack)) {
                item.grow(itemStack.getCount());
                return item;
            }
        }
        list.add(itemStack.copy());
        return itemStack.copy();
    }

    public static ItemStack parseStack(CompoundTag tag) {
        if (!tag.contains("__item") || !tag.contains("count"))
            return ItemStack.of(tag);
        int count = tag.getInt("count");
        ItemStack item = ItemStack.of(tag.getCompound("__item"));
        item.setCount(count);
        return item;
    }

    public static CompoundTag saveStack(ItemStack stack) {
        int count = stack.getCount();
        if (count > 127) {
            stack = stack.copyWithCount(1);
        }
        CompoundTag tmp = new CompoundTag();
        tmp.put("__item", stack.save(new CompoundTag()));
        tmp.putInt("count", count);
        return tmp;
    }
}
