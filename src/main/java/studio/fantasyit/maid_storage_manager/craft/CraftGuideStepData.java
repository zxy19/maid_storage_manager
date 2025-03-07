package studio.fantasyit.maid_storage_manager.craft;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import studio.fantasyit.maid_storage_manager.items.CraftGuide;
import studio.fantasyit.maid_storage_manager.storage.Storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CraftGuideStepData {
    public static Codec<CraftGuideStepData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Storage.CODEC.optionalFieldOf(CraftGuide.TAG_OP_STORAGE)
                            .forGetter(CraftGuideStepData::getStorageOptional),
                    Codec.list(ItemStack.CODEC)
                            .fieldOf(CraftGuide.TAG_OP_ITEMS)
                            .forGetter(CraftGuideStepData::getItems)
            ).apply(instance, CraftGuideStepData::new)
    );
    public Storage storage;
    public List<ItemStack> items;

    public CraftGuideStepData(Optional<Storage> storage, List<ItemStack> items) {
        this.storage = storage.orElse(null);
        this.items = items;
    }

    public CraftGuideStepData(Storage storage, List<ItemStack> items) {
        this(Optional.ofNullable(storage), items);
    }

    public static CraftGuideStepData fromCompound(CompoundTag tag) {
        Storage storage = null;
        if (tag.contains(CraftGuide.TAG_OP_STORAGE))
            storage = Storage.fromNbt(tag.getCompound(CraftGuide.TAG_OP_STORAGE));
        List<ItemStack> items = new ArrayList<>();
        if (tag.contains(CraftGuide.TAG_OP_ITEMS)) {
            ListTag list = tag.getList(CraftGuide.TAG_OP_ITEMS, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                items.add(
                        ItemStack.of(list.getCompound(i).getCompound(CraftGuide.TAG_ITEMS_ITEM))
                                .copyWithCount(list.getCompound(i).getInt(CraftGuide.TAG_ITEMS_COUNT))
                );
            }
        }
        return new CraftGuideStepData(storage, items);
    }

    public Storage getStorage() {
        return storage;
    }

    public Optional<Storage> getStorageOptional() {
        return Optional.ofNullable(storage);
    }

    public List<ItemStack> getItems() {
        return items;
    }

    public List<ItemStack> getNonEmptyItems() {
        return items.stream().filter(itemStack -> !itemStack.isEmpty()).toList();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CraftGuideStepData craftGuideStepData &&
                craftGuideStepData.storage.equals(this.storage) &&
                craftGuideStepData.items.size() == this.items.size()
        ) {
            for (int i = 0; i < craftGuideStepData.items.size(); i++) {
                if (!ItemStack.isSameItemSameTags(craftGuideStepData.items.get(i), this.items.get(i))) return false;
            }
            return true;
        }
        return false;
    }

    public boolean available() {
        return storage != null;
    }
}