package studio.fantasyit.maid_storage_manager.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.DataComponentUtil;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;

import java.util.List;
import java.util.Objects;
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
            return ItemStack.isSameItemSameComponents(stack1, stack2);
        return ItemStack.isSameItem(stack1, stack2);
    }

    public static TagKey<Item> MatchItem = TagKey.create(BuiltInRegistries.ITEM.key(), ResourceLocation.fromNamespaceAndPath(MaidStorageManager.MODID, "no_components"));
    public static TagKey<Item> NoMatchItem = TagKey.create(BuiltInRegistries.ITEM.key(), ResourceLocation.fromNamespaceAndPath(MaidStorageManager.MODID, "use_components"));


    public static boolean isSameInCrafting(ItemStack stack1, ItemStack stack2) {
        if (stack1.getItem() != stack2.getItem()) return false;
        if (stack1.isEmpty() || stack2.isEmpty()) return true;
        boolean matchTag = Config.craftingMatchTag;
        if (stack1.is(MatchItem)) matchTag = false;
        if (stack1.is(NoMatchItem)) matchTag = true;
        if (!matchTag) return true;
        return isSameTagInCrafting(stack1, stack2);
    }

    public static boolean isSameTagInCrafting(ItemStack stack1, ItemStack stack2) {
        DataComponentMap components1 = stack1.getComponents();
        DataComponentMap components2 = stack2.getComponents();
        for (TypedDataComponent<?> c : components1) {
            if (Config.noMatchPaths.contains(c.type().toString())) continue;
            if (!components2.has(c.type())) return false;
            if (!Objects.equals(components2.get(c.type()), c.value())) return false;
        }
        return true;
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

    public static ItemStack parseStack(HolderLookup.Provider holderLookup, CompoundTag tag) {
        return OPTIONAL_CODEC_UNLIMITED.parse(holderLookup.createSerializationContext(NbtOps.INSTANCE), tag).getOrThrow();
    }

    public static CompoundTag saveStack(HolderLookup.Provider holderLookup, ItemStack stack) {
        return (CompoundTag) DataComponentUtil.wrapEncodingExceptions(stack, OPTIONAL_CODEC_UNLIMITED, holderLookup);
    }
}
