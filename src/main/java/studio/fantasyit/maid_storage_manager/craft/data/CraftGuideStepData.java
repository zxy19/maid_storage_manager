package studio.fantasyit.maid_storage_manager.craft.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.items.CraftGuide;
import studio.fantasyit.maid_storage_manager.storage.Target;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CraftGuideStepData {
    public static Codec<CraftGuideStepData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Target.CODEC.optionalFieldOf(CraftGuide.TAG_OP_STORAGE)
                            .forGetter(CraftGuideStepData::getStorageOptional),
                    Codec.list(ItemStack.CODEC)
                            .fieldOf(CraftGuide.TAG_OP_ITEMS)
                            .forGetter(CraftGuideStepData::getItems),
                    ResourceLocation.CODEC.fieldOf(CraftGuide.TAG_OP_ACTION)
                            .forGetter(CraftGuideStepData::getActionType),
                    Codec.BOOL.optionalFieldOf(CraftGuide.TAG_OP_OPTIONAL, false)
                            .forGetter(CraftGuideStepData::isOptional)
            ).apply(instance, CraftGuideStepData::new)
    );
    public Target storage;
    public List<ItemStack> items;
    public ResourceLocation action;
    public boolean optional;

    public CraftGuideStepData(Optional<Target> storage, List<ItemStack> items, ResourceLocation action, boolean optional) {
        this.storage = storage.orElse(null);
        this.items = items;
        this.action = action;
        this.optional = optional;
    }

    public CraftGuideStepData(Target storage, List<ItemStack> items, ResourceLocation action, boolean optional) {
        this(Optional.ofNullable(storage), items, action, optional);
    }

    public static CraftGuideStepData fromCompound(CompoundTag tag) {
        Target storage = null;
        ResourceLocation action = null;
        boolean optional = false;
        if (tag.contains(CraftGuide.TAG_OP_STORAGE))
            storage = Target.fromNbt(tag.getCompound(CraftGuide.TAG_OP_STORAGE));
        List<ItemStack> items = new ArrayList<>();
        if (storage != null && tag.contains(CraftGuide.TAG_OP_ITEMS)) {
            ListTag list = tag.getList(CraftGuide.TAG_OP_ITEMS, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                items.add(
                        ItemStack.of(list.getCompound(i).getCompound(CraftGuide.TAG_ITEMS_ITEM))
                                .copyWithCount(list.getCompound(i).getInt(CraftGuide.TAG_ITEMS_COUNT))
                );
            }
        }
        if (tag.contains(CraftGuide.TAG_OP_ACTION))
            action = ResourceLocation.tryParse(tag.getString(CraftGuide.TAG_OP_ACTION));
        if(tag.contains(CraftGuide.TAG_OP_OPTIONAL))
            optional = tag.getBoolean(CraftGuide.TAG_OP_OPTIONAL);
        return new CraftGuideStepData(storage, items, action, optional);
    }

    public Target getStorage() {
        return storage;
    }

    public ResourceLocation getActionType() {
        return action;
    }
    public boolean isOptional() {
        return optional;
    }
    public Optional<Target> getStorageOptional() {
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
                if (!ItemStack.isSameItem(craftGuideStepData.items.get(i), this.items.get(i))) return false;
            }
            return true;
        }
        return false;
    }

    public boolean available() {
        return storage != null;
    }
}