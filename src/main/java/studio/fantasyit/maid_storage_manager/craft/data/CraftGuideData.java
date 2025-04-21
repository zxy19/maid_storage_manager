package studio.fantasyit.maid_storage_manager.craft.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.craft.CraftManager;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonPlaceItemAction;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonTakeItemAction;
import studio.fantasyit.maid_storage_manager.craft.type.CommonType;
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
                            .forGetter(CraftGuideData::getType)
            ).apply(instance, CraftGuideData::new)
    );

    public List<CraftGuideStepData> steps;
    public ResourceLocation type;
    public List<ItemStack> inputs;
    public List<ItemStack> inputsWithOptional;
    public List<ItemStack> outputs;
    public List<ItemStack> outputsWithOptional;
    public Integer selecting;

    public CraftGuideData(List<CraftGuideStepData> steps, ResourceLocation type) {
        this.steps = steps;
        this.type = type;
        this.buildInputAndOutputs();
    }

    /**
     * 根据步骤，构建出具体的输入和输出物品及其数量
     */
    private void buildInputAndOutputs() {
        inputs = new ArrayList<>();
        outputs = new ArrayList<>();
        inputsWithOptional = new ArrayList<>();
        outputsWithOptional = new ArrayList<>();
        for (CraftGuideStepData step : getTransformedSteps()) {
            List<ItemStack> input = step.getInput();
            for (ItemStack _item : input) {
                if (_item.isEmpty()) continue;
                ItemStack item = _item.copy();
                if (!step.isOptional()) {
                    if (ItemStackUtil.removeIsMatchInList(outputs, item, step.isMatchTag()).isEmpty()) continue;
                    ItemStackUtil.addToList(inputs, item, step.isMatchTag());
                }

                if (ItemStackUtil.removeIsMatchInList(outputsWithOptional, item, step.isMatchTag()).isEmpty())
                    continue;
                ItemStackUtil.addToList(inputsWithOptional, item, step.isMatchTag());
            }
            for (ItemStack _item : step.getOutput()) {
                if (_item.isEmpty()) continue;
                ItemStack item = _item.copy();
                if (!step.isOptional())
                    ItemStackUtil.addToList(outputs, item, step.isMatchTag());
                ItemStackUtil.addToList(outputsWithOptional, item, step.isMatchTag());
            }
        }
    }

    public static CraftGuideData fromItemStack(ItemStack craftGuide) {
        CompoundTag tag = craftGuide.getOrCreateTag();
        if (tag.contains("input1") || tag.contains("input2") || tag.contains("output")) {
            CraftGuideData data = compatibleToV1Type(tag);
            tag.remove("input1");
            tag.remove("input2");
            tag.remove("output");
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
        CraftGuideData craftGuideData = new CraftGuideData(step, type);
        craftGuideData.selecting = 0;
        if (tag.contains(CraftGuide.TAG_SELECTING))
            craftGuideData.selecting = tag.getInt(CraftGuide.TAG_SELECTING);
        if (craftGuideData.selecting > craftGuideData.steps.size())
            craftGuideData.selecting = craftGuideData.steps.size();
        return craftGuideData;
    }

    private static void compatibleToV1TypeAddStep(CompoundTag compound, CraftGuideData craftGuideData, boolean optional, ResourceLocation type) {
        if (compound.contains(CraftGuide.TAG_OP_STORAGE)) {
            List<ItemStack> items = new ArrayList<>();
            Target target = Target.fromNbt(compound.getCompound(CraftGuide.TAG_OP_STORAGE));
            ListTag list = compound.getList("input1", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                items.add(
                        ItemStack.of(list.getCompound(i).getCompound(CraftGuide.TAG_ITEMS_ITEM))
                                .copyWithCount(list.getCompound(i).getInt(CraftGuide.TAG_ITEMS_COUNT))
                );
            }
            craftGuideData.steps.add(new CraftGuideStepData(target, items, new ArrayList<>(), type, optional, false));
        }
    }

    /**
     * 旧版本数据兼容
     */
    private static CraftGuideData compatibleToV1Type(CompoundTag tag) {
        CraftGuideData craftGuideData = new CraftGuideData(new ArrayList<>(), CommonType.TYPE);
        if (tag.contains("input1")) {
            compatibleToV1TypeAddStep(tag.getCompound("input1"), craftGuideData, false, CommonPlaceItemAction.TYPE);
        }
        if (tag.contains("input2")) {
            compatibleToV1TypeAddStep(tag.getCompound("input2"), craftGuideData, true, CommonPlaceItemAction.TYPE);
        }
        if (tag.contains("output")) {
            compatibleToV1TypeAddStep(tag.getCompound("output"), craftGuideData, true, CommonTakeItemAction.TYPE);
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

    public List<ItemStack> getInput() {
        return this.inputs;
    }

    public ItemStack getFirstOutput() {
        return outputs.isEmpty() ? ItemStack.EMPTY : outputs.get(0);
    }

    public List<ItemStack> getOutput() {
        return this.outputs;
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
}
