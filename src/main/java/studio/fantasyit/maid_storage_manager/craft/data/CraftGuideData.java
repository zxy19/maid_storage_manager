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
import studio.fantasyit.maid_storage_manager.craft.action.ActionOption;
import studio.fantasyit.maid_storage_manager.craft.action.ActionOptionSet;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonPlaceItemAction;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonTakeItemAction;
import studio.fantasyit.maid_storage_manager.craft.type.CommonType;
import studio.fantasyit.maid_storage_manager.craft.type.CraftingType;
import studio.fantasyit.maid_storage_manager.craft.type.ICraftType;
import studio.fantasyit.maid_storage_manager.items.CraftGuide;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;

public class CraftGuideData {
    public static Codec<CraftGuideData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    CraftGuideStepData.CODEC.listOf().fieldOf(CraftGuide.TAG_STEPS)
                            .forGetter(CraftGuideData::getSteps),
                    ResourceLocation.CODEC.fieldOf(CraftGuide.TAG_TYPE)
                            .forGetter(CraftGuideData::getType),
                    Codec.BOOL.fieldOf(CraftGuide.TAG_MARK_MERGEABLE)
                            .forGetter(CraftGuideData::isMergeable),
                    Codec.BOOL.fieldOf(CraftGuide.TAG_MARK_NO_OCCUPY)
                            .forGetter(CraftGuideData::isNoOccupy)
            ).apply(instance, CraftGuideData::new)
    );

    public List<CraftGuideStepData> steps;
    public ResourceLocation type;
    public List<ItemStack> inputs;
    public List<ItemStack> inputsWithOptional;

    public List<ItemStack> inputsNoCircular;
    public List<ItemStack> outputs;
    public List<ItemStack> outputsWithOptional;
    public List<ItemStack> outputsNoCircular;
    public Integer selecting;

    boolean mergeable;
    boolean noOccupy;

    public int extraSlotConsume = 0;

    public CraftGuideData(List<CraftGuideStepData> steps, ResourceLocation type) {
        this(steps, type, false, false);
    }

    public CraftGuideData(List<CraftGuideStepData> steps, ResourceLocation type, boolean mergeable, boolean noOccupy) {
        this.steps = steps;
        this.type = type;
        this.mergeable = mergeable;
        this.noOccupy = noOccupy;
        this.buildInputAndOutputs();
    }

    /**
     * 根据步骤，构建出具体的输入和输出物品及其数量
     */
    public void buildInputAndOutputs() {
        inputs = new ArrayList<>();
        outputs = new ArrayList<>();
        inputsWithOptional = new ArrayList<>();
        outputsWithOptional = new ArrayList<>();
        extraSlotConsume = 0;
        for (CraftGuideStepData step : getTransformedSteps()) {
            List<ItemStack> input = step.getInput();
            for (ItemStack _item : input) {
                if (_item.isEmpty()) continue;

                ItemStack item = _item.copy();
                if (!step.isOptional()) {
                    if (!ItemStackUtil.removeIsMatchInList(outputs, item, ItemStackUtil::isSameInCrafting).isEmpty())
                        ItemStackUtil.addToList(inputs, item.copy(), ItemStackUtil::isSameInCrafting);
                }

                item = _item.copy();
                if (!ItemStackUtil.removeIsMatchInList(outputsWithOptional, item, ItemStackUtil::isSameInCrafting).isEmpty())
                    ItemStackUtil.addToList(inputsWithOptional, item.copy(), ItemStackUtil::isSameInCrafting);
            }
            for (ItemStack _item : step.getOutput()) {
                if (_item.isEmpty()) continue;
                ItemStack item = _item.copy();
                if (!step.isOptional())
                    ItemStackUtil.addToList(outputs, item.copy(), ItemStackUtil::isSameInCrafting);
                ItemStackUtil.addToList(outputsWithOptional, item.copy(), ItemStackUtil::isSameInCrafting);
            }
            if (step.getExtraSlotConsume() > extraSlotConsume)
                extraSlotConsume = step.getExtraSlotConsume();
        }
    }

    public static CraftGuideData fromItemStack(ItemStack craftGuide) {
        CompoundTag tag = craftGuide.getOrCreateTag();
        if (tag.contains("input1") || tag.contains("input2") || tag.contains("output")) {
            CraftGuideData data = compatibleToV1Type(tag);
            tag.remove("input1");
            tag.remove("input2");
            tag.remove("output");
            data.buildInputAndOutputs();
            data.saveToItemStack(craftGuide);
            CraftGuideRenderData.recalculateItemStack(craftGuide);
            return data;
        }
        ListTag inputs = tag.getList(CraftGuide.TAG_STEPS, Tag.TAG_COMPOUND);
        ResourceLocation type = null;
        ArrayList<CraftGuideStepData> step = new ArrayList<>();
        for (int i = 0; i < inputs.size(); i++) {
            step.add(CraftGuideStepData.fromCompound(inputs.getCompound(i)));
        }
        if (tag.contains(CraftGuide.TAG_TYPE)) {
            type = ResourceLocation.tryParse(tag.getString(CraftGuide.TAG_TYPE));
        } else {
            type = CommonType.TYPE;
        }
        boolean mergeable = false;
        if (tag.contains(CraftGuide.TAG_MARK_MERGEABLE))
            mergeable = tag.getBoolean(CraftGuide.TAG_MARK_MERGEABLE);
        boolean noOccupy = false;
        if (tag.contains(CraftGuide.TAG_MARK_NO_OCCUPY))
            noOccupy = tag.getBoolean(CraftGuide.TAG_MARK_NO_OCCUPY);
        CraftGuideData craftGuideData = new CraftGuideData(step, type, mergeable, noOccupy);
        craftGuideData.selecting = 0;
        if (tag.contains(CraftGuide.TAG_SELECTING))
            craftGuideData.selecting = tag.getInt(CraftGuide.TAG_SELECTING);
        if (craftGuideData.selecting > craftGuideData.steps.size())
            craftGuideData.selecting = craftGuideData.steps.size();
        return craftGuideData;
    }

    private static void compatibleToV1TypeAddStep(CompoundTag compound, CraftGuideData craftGuideData, boolean optional, ResourceLocation type, boolean output) {
        if (compound.contains("side")) {
            List<ItemStack> items = new ArrayList<>();
            Target target = Target.fromNbt(compound.getCompound("side"));
            ListTag list = compound.getList("items", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                items.add(
                        ItemStackUtil.parseStack(list.getCompound(i).getCompound(CraftGuide.TAG_ITEMS_ITEM))
                                .copyWithCount(list.getCompound(i).getInt(CraftGuide.TAG_ITEMS_COUNT))
                );
            }
            if (output)
                craftGuideData.steps.add(new CraftGuideStepData(target,
                        new ArrayList<>(),
                        items,
                        type,
                        ActionOptionSet.with(ActionOption.OPTIONAL, optional)));
            else
                craftGuideData.steps.add(new CraftGuideStepData(target,
                        items,
                        new ArrayList<>(),
                        type,
                        ActionOptionSet.with(ActionOption.OPTIONAL, optional)));
        }
    }

    private static void compatibleToV1TypeAddCraftStep(CraftGuideData craftGuideData, CompoundTag input1, CompoundTag input2) {
        if (input1.contains("side") && input2.contains("side")) {
            List<ItemStack> inputs = new ArrayList<>();
            List<ItemStack> outputs = new ArrayList<>();
            Target target = Target.fromNbt(input1.getCompound("side"));
            ListTag inputsList = input1.getList("items", Tag.TAG_COMPOUND);
            for (int i = 0; i < inputsList.size(); i++) {
                inputs.add(
                        ItemStackUtil.parseStack(inputsList.getCompound(i).getCompound(CraftGuide.TAG_ITEMS_ITEM))
                                .copyWithCount(inputsList.getCompound(i).getInt(CraftGuide.TAG_ITEMS_COUNT))
                );
            }
            ListTag outputsList = input2.getList("items", Tag.TAG_COMPOUND);
            for (int i = 0; i < outputsList.size(); i++) {
                outputs.add(
                        ItemStackUtil.parseStack(outputsList.getCompound(i).getCompound(CraftGuide.TAG_ITEMS_ITEM))
                                .copyWithCount(outputsList.getCompound(i).getInt(CraftGuide.TAG_ITEMS_COUNT))
                );
            }
            craftGuideData.type = CraftingType.TYPE;
            craftGuideData.steps.add(new CraftGuideStepData(target,
                    inputs,
                    outputs,
                    CraftingType.TYPE));
        }
    }

    /**
     * 旧版本数据兼容
     */
    private static CraftGuideData compatibleToV1Type(CompoundTag tag) {
        CraftGuideData craftGuideData = new CraftGuideData(new ArrayList<>(), CommonType.TYPE);
        if (tag.contains("input1")) {
            if (tag.getCompound("input1").contains("side")) {
                Target target = Target.fromNbt(tag.getCompound("input1").getCompound("side"));
                if (target.getType().equals(new ResourceLocation(MaidStorageManager.MODID, "crafting"))) {
                    compatibleToV1TypeAddCraftStep(craftGuideData, tag.getCompound("input1"), tag.getCompound("output"));
                    return craftGuideData;
                }
            }
        }
        if (tag.contains("input1")) {
            compatibleToV1TypeAddStep(tag.getCompound("input1"), craftGuideData, false, CommonPlaceItemAction.TYPE, false);
        }
        if (tag.contains("input2")) {
            compatibleToV1TypeAddStep(tag.getCompound("input2"), craftGuideData, true, CommonPlaceItemAction.TYPE, false);
        }
        if (tag.contains("output")) {
            compatibleToV1TypeAddStep(tag.getCompound("output"), craftGuideData, false, CommonTakeItemAction.TYPE, true);
        }
        return craftGuideData;
    }

    public void saveToItemStack(ItemStack itemStack) {
        CompoundTag tag = itemStack.getOrCreateTag();
        ListTag inputs = new ListTag();
        for (CraftGuideStepData step : steps) {
            inputs.add(step.toCompound());
        }
        tag.put(CraftGuide.TAG_STEPS, inputs);
        tag.putString(CraftGuide.TAG_TYPE, type.toString());
        if (selecting != null)
            tag.putInt(CraftGuide.TAG_SELECTING, selecting);
        tag.putBoolean(CraftGuide.TAG_MARK_MERGEABLE, mergeable);
        tag.putBoolean(CraftGuide.TAG_MARK_NO_OCCUPY, noOccupy);
        itemStack.setTag(tag);
    }

    public List<CraftGuideStepData> getSteps() {
        return steps;
    }

    public List<CraftGuideStepData> getTransformedSteps() {
        ICraftType type1 = CraftManager.getInstance().getType(type);
        if (type1 == null) return steps;
        return type1.transformSteps(steps);
    }

    public boolean isMergeable() {
        return mergeable;
    }

    public void isMergeable(boolean b) {
        mergeable = b;
    }

    public boolean isNoOccupy() {
        return noOccupy;
    }

    public void isNoOccupy(boolean b) {
        noOccupy = b;
    }

    public int getExtraSlotConsume() {
        return extraSlotConsume;
    }

    public List<ItemStack> getInput() {
        return this.inputs;
    }

    public ItemStack getFirstOutput() {
        return outputs.isEmpty() ? ItemStack.EMPTY : outputs.get(0);
    }

    public List<ItemStack> getOutput() {
        return this.outputs;
    }

    protected void calculateNoCircular() {
        if (inputsNoCircular != null && outputsNoCircular != null)
            return;
        inputsNoCircular = inputs.stream().map(ItemStack::copy).toList();
        outputsNoCircular = outputs.stream().map(ItemStack::copy).toList();
        for (ItemStack input : inputsNoCircular) {
            if (input.isEmpty()) continue;
            for (ItemStack output : outputsNoCircular) {
                if (output.isEmpty()) continue;
                if (ItemStackUtil.isSame(input, output, false)) {
                    int count = Math.min(input.getCount(), output.getCount());
                    input.shrink(count);
                    output.shrink(count);
                }
                if (input.isEmpty()) break;
            }
        }
        inputsNoCircular = inputsNoCircular.stream().filter(itemStack -> !itemStack.isEmpty()).toList();
        outputsNoCircular = outputsNoCircular.stream().filter(itemStack -> !itemStack.isEmpty()).toList();
    }

    public List<ItemStack> getInputsNoCircular() {

        return inputsNoCircular;
    }

    public List<ItemStack> getOutputsNoCircular() {
        return outputsNoCircular;
    }

    public CraftGuideStepData getStepByIdx(int idx) {
        return steps.get(idx);
    }

    public ResourceLocation getType() {
        return type;
    }


    public List<ItemStack> getAllInputItems() {
        return inputs;
    }

    public List<ItemStack> getAllInputItemsWithOptional() {
        return inputsWithOptional;
    }

    public List<ItemStack> getAllOutputItems() {
        return outputs;
    }

    public List<ItemStack> getAllOutputItemsWithOptional() {
        return outputsWithOptional;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CraftGuideData craftGuideData) {
            if (craftGuideData.steps.size() != this.steps.size()) return false;
            for (int i = 0; i < craftGuideData.steps.size(); i++) {
                if (!craftGuideData.steps.get(i).equals(this.steps.get(i))) return false;
            }
            return true;
        }
        return false;
    }

    public boolean available() {
        if (steps.size() == 0 || type == null) return false;
        ICraftType type1 = CraftManager.getInstance().getType(type);
        if (type1 == null) return false;
        return type1.available(this);
    }

    public boolean isCircular() {
        List<ItemStack> allInputs = getInput();
        List<ItemStack> allOutputs = getOutput();

        for (CraftGuideStepData step : steps) {
            List<ItemStack> output = step.getOutput();
            for (ItemStack outputItem : output) {
                if (allInputs.stream().anyMatch(item -> ItemStackUtil.isSameInCrafting(item, outputItem))) {
                    if (allOutputs.stream().anyMatch(item -> ItemStackUtil.isSameInCrafting(item, outputItem))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CraftGuideData{");
        if (isNoOccupy()) {
            builder.append("[NO]");
        }
        if (isMergeable()) {
            builder.append("[ME]");
        }
        getInput().forEach(itemStack -> builder.append(itemStack.getItem()).append(","));
        builder.append("->");
        getOutput().forEach(itemStack -> builder.append(itemStack.getItem()).append(","));
        builder.append("}");
        return builder.toString();
    }

}
