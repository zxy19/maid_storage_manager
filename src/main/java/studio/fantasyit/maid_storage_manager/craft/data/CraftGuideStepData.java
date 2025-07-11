package studio.fantasyit.maid_storage_manager.craft.data;

import com.google.common.collect.ImmutableCollection;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.craft.CraftManager;
import studio.fantasyit.maid_storage_manager.craft.action.CraftAction;
import studio.fantasyit.maid_storage_manager.items.CraftGuide;
import studio.fantasyit.maid_storage_manager.storage.Target;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
                    Codec.BOOL.optionalFieldOf(CraftGuide.TAG_OP_OPTIONAL, false)
                            .forGetter(CraftGuideStepData::isOptional),
                    CompoundTag.CODEC.fieldOf(CraftGuide.TAG_OP_EXTRA)
                            .forGetter(CraftGuideStepData::getExtraData)
            ).apply(instance, CraftGuideStepData::new)
    );
    public Target storage;
    public List<ItemStack> input;
    public List<ItemStack> output;
    public ResourceLocation action;
    public CraftAction actionType;
    public boolean optional;
    public CompoundTag extraData;

    public CraftGuideStepData(Target storage,
                              List<ItemStack> input,
                              List<ItemStack> output,
                              ResourceLocation action,
                              boolean optional,
                              CompoundTag extraData) {
        this.storage = storage;
        this.action = action;
        this.optional = optional;
        this.actionType = CraftManager.getInstance().getAction(action);
        if (this.actionType == null) {
            this.actionType = CraftManager.getInstance().getDefaultAction();
            this.action = actionType.type();
        }
        if (input.size() < this.actionType.inputCount())
            input = new ArrayList<>(input);
        while (input.size() < this.actionType.inputCount())
            input.add(ItemStack.EMPTY);
        if (output.size() < this.actionType.outputCount())
            output = new ArrayList<>(output);
        while (output.size() < this.actionType.outputCount())
            output.add(ItemStack.EMPTY);
        this.input = input;
        this.output = output;
        this.extraData = extraData;
    }

    public static CraftGuideStepData createFromTypeStorage(Target storage, ResourceLocation action) {
        CraftAction action1 = Objects.requireNonNull(CraftManager.getInstance().getAction(action));
        List<ItemStack> inputs = new ArrayList<>();
        for (int i = 0; i < action1.inputCount(); i++)
            inputs.add(ItemStack.EMPTY);
        List<ItemStack> outputs = new ArrayList<>();
        for (int i = 0; i < action1.outputCount(); i++)
            outputs.add(ItemStack.EMPTY);
        return new CraftGuideStepData(storage, inputs, outputs, action, false, new CompoundTag());
    }

    public static CraftGuideStepData fromCompound(CompoundTag tag) {
        Target storage = null;
        ResourceLocation action = null;
        boolean optional = false;
        CompoundTag extraData = new CompoundTag();
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
        if (tag.contains(CraftGuide.TAG_OP_EXTRA))
            extraData = tag.getCompound(CraftGuide.TAG_OP_EXTRA);
        return new CraftGuideStepData(storage, inputs, outputs, action, optional, extraData);
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
        tag.put(CraftGuide.TAG_OP_EXTRA, extraData);
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



    public List<ItemStack> getItems() {
        ArrayList<ItemStack> items = new ArrayList<>();
        items.addAll(getInput());
        items.addAll(getOutput());
        return items;
    }

    public List<ItemStack> getNonEmptyItems() {
        return getItems().stream().filter(itemStack -> !itemStack.isEmpty()).toList();
    }

    public List<ItemStack> getInput() {
        return input.subList(0, actionType.inputCount());
    }

    public void setInput(int i, ItemStack itemStack) {
        if (input instanceof ImmutableCollection<?>)
            input = new ArrayList<>(input);
        input.set(i, itemStack);
    }

    public void clearInput() {
        if (input instanceof ImmutableCollection<?>)
            input = new ArrayList<>(input);
        input.clear();
    }

    public List<ItemStack> getNonEmptyInput() {
        return getInput().stream().filter(itemStack -> !itemStack.isEmpty()).toList();
    }

    public List<ItemStack> getOutput() {
        return output.subList(0, actionType.outputCount());
    }

    public void setOutput(int i, ItemStack itemStack) {
        if (output instanceof ImmutableCollection<?>)
            output = new ArrayList<>(output);
        output.set(i, itemStack);
    }

    public void clearOutput() {
        if (output instanceof ImmutableCollection<?>)
            output = new ArrayList<>(output);
        output.clear();
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


    public void setAction(ResourceLocation action) {
        this.action = action;
        this.actionType = CraftManager.getInstance().getAction(action);
        if (input.size() < actionType.inputCount()) {
            if (input instanceof ImmutableCollection<?>)
                input = new ArrayList<>(input);
            for (int i = input.size(); i < actionType.inputCount(); i++) {
                input.add(ItemStack.EMPTY);
            }
        }
        if (output.size() < actionType.outputCount()) {
            if (output instanceof ImmutableCollection<?>)
                output = new ArrayList<>(output);
            for (int i = output.size(); i < actionType.outputCount(); i++) {
                output.add(ItemStack.EMPTY);
            }
        }
    }

    public CompoundTag getExtraData() {
        return extraData;
    }

    public void setExtraData(CompoundTag extraData) {
        this.extraData = extraData;
    }
}