package studio.fantasyit.maid_storage_manager.craft.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.craft.CraftManager;
import studio.fantasyit.maid_storage_manager.items.CraftGuide;
import studio.fantasyit.maid_storage_manager.storage.Target;

import java.util.ArrayList;
import java.util.List;

public class CraftGuideStepData {
    public static final ResourceLocation SPECIAL_ACTION = new ResourceLocation(MaidStorageManager.MODID, "special");
    public static Codec<CraftGuideStepData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Target.CODEC.fieldOf(CraftGuide.TAG_OP_STORAGE)
                            .forGetter(CraftGuideStepData::getStorage),
                    Codec.list(ItemStack.CODEC)
                            .fieldOf(CraftGuide.TAG_OP_INPUT)
                            .forGetter(CraftGuideStepData::getInput),
                    Codec.list(ItemStack.CODEC)
                            .fieldOf(CraftGuide.TAG_OP_OUTPUT)
                            .forGetter(CraftGuideStepData::getOutput),
                    ResourceLocation.CODEC.fieldOf(CraftGuide.TAG_OP_ACTION)
                            .forGetter(CraftGuideStepData::getActionType),
                    Codec.BOOL.fieldOf(CraftGuide.TAG_OP_MATCH_TAG)
                            .forGetter(CraftGuideStepData::isMatchTag),
                    Codec.BOOL.optionalFieldOf(CraftGuide.TAG_OP_OPTIONAL, false)
                            .forGetter(CraftGuideStepData::isOptional)
            ).apply(instance, CraftGuideStepData::new)
    );
    public Target storage;
    public List<ItemStack> input;
    public List<ItemStack> output;
    public ResourceLocation action;
    public CraftManager.CraftAction actionType;
    public boolean optional;
    public boolean matchTag;

    public CraftGuideStepData(Target storage,
                              List<ItemStack> input,
                              List<ItemStack> output,
                              ResourceLocation action,
                              boolean optional,
                              boolean matchTag) {
        this.storage = storage;
        this.input = input;
        this.output = output;
        this.action = action;
        this.optional = optional;
        this.matchTag = matchTag;
        this.actionType = CraftManager.getInstance().getAction(action);
        if (this.actionType == null) {
            this.actionType = CraftManager.getInstance().getCommonActions().get(0);
            this.action = actionType.type();
        }
    }
    public static CraftGuideStepData fromCompound(CompoundTag tag) {
        Target storage = null;
        ResourceLocation action = null;
        boolean optional = false;
        boolean matchTag = false;
        if (tag.contains(CraftGuide.TAG_OP_STORAGE))
            storage = Target.fromNbt(tag.getCompound(CraftGuide.TAG_OP_STORAGE));
        List<ItemStack> inputs = new ArrayList<>();
        if (storage != null && tag.contains(CraftGuide.TAG_OP_INPUT)) {
            ListTag list = tag.getList(CraftGuide.TAG_OP_INPUT, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                inputs.add(
                        ItemStack.of(list.getCompound(i).getCompound(CraftGuide.TAG_ITEMS_ITEM))
                                .copyWithCount(list.getCompound(i).getInt(CraftGuide.TAG_ITEMS_COUNT))
                );
            }
        }
        List<ItemStack> outputs = new ArrayList<>();
        if (storage != null && tag.contains(CraftGuide.TAG_OP_OUTPUT)) {
            ListTag list = tag.getList(CraftGuide.TAG_OP_OUTPUT, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                outputs.add(
                        ItemStack.of(list.getCompound(i).getCompound(CraftGuide.TAG_ITEMS_ITEM))
                                .copyWithCount(list.getCompound(i).getInt(CraftGuide.TAG_ITEMS_COUNT))
                );
            }
        }
        if (tag.contains(CraftGuide.TAG_OP_ACTION))
            action = ResourceLocation.tryParse(tag.getString(CraftGuide.TAG_OP_ACTION));
        if (tag.contains(CraftGuide.TAG_OP_OPTIONAL))
            optional = tag.getBoolean(CraftGuide.TAG_OP_OPTIONAL);
        if (tag.contains(CraftGuide.TAG_OP_MATCH_TAG))
            matchTag = tag.getBoolean(CraftGuide.TAG_OP_MATCH_TAG);
        return new CraftGuideStepData(storage, inputs, outputs, action, optional, matchTag);
    }
    public CompoundTag toCompound() {
        CompoundTag tag = new CompoundTag();
        tag.put(CraftGuide.TAG_OP_STORAGE, storage.toNbt());
        if (input.size() > 0) {
            ListTag list = new ListTag();
            for (ItemStack itemStack : input) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.put(CraftGuide.TAG_ITEMS_ITEM, itemStack.save(new CompoundTag()));
                itemTag.putInt(CraftGuide.TAG_ITEMS_COUNT, itemStack.getCount());
                list.add(itemTag);
            }
            tag.put(CraftGuide.TAG_OP_INPUT, list);
        }
        if (output.size() > 0) {
            ListTag list = new ListTag();
            for (ItemStack itemStack : output) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.put(CraftGuide.TAG_ITEMS_ITEM, itemStack.save(new CompoundTag()));
                itemTag.putInt(CraftGuide.TAG_ITEMS_COUNT, itemStack.getCount());
                list.add(itemTag);
            }
            tag.put(CraftGuide.TAG_OP_OUTPUT, list);
        }
        tag.putString(CraftGuide.TAG_OP_ACTION, action.toString());
        tag.putBoolean(CraftGuide.TAG_OP_OPTIONAL, optional);
        tag.putBoolean(CraftGuide.TAG_OP_MATCH_TAG, matchTag);
        return tag;
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

    public boolean isMatchTag() {
        return matchTag;
    }


    public List<ItemStack> getItems() {
        ArrayList<ItemStack> items = new ArrayList<>();
        items.addAll(input);
        items.addAll(output);
        return items;
    }

    public List<ItemStack> getNonEmptyItems() {
        return getItems().stream().filter(itemStack -> !itemStack.isEmpty()).toList();
    }

    public List<ItemStack> getInput() {
        return input.subList(0,actionType.inputCount());
    }

    public List<ItemStack> getNonEmptyInput() {
        return getInput().stream().filter(itemStack -> !itemStack.isEmpty()).toList();
    }

    public List<ItemStack> getOutput() {
        return output.subList(0,actionType.outputCount());
    }

    public List<ItemStack> getNonEmptyOutput() {
        return getOutput().stream().filter(itemStack -> !itemStack.isEmpty()).toList();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CraftGuideStepData craftGuideStepData &&
                craftGuideStepData.storage.equals(this.storage) &&
                craftGuideStepData.input.size() == this.input.size() &&
                craftGuideStepData.output.size() == this.output.size()
        ) {
            for (int i = 0; i < craftGuideStepData.output.size(); i++) {
                if (!ItemStack.isSameItem(craftGuideStepData.output.get(i), this.output.get(i))) return false;
            }
            for (int i = 0; i < craftGuideStepData.input.size(); i++) {
                if (!ItemStack.isSameItem(craftGuideStepData.input.get(i), this.input.get(i))) return false;
            }
            return true;
        }
        return false;
    }


}