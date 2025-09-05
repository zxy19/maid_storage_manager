package studio.fantasyit.maid_storage_manager.craft.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.craft.CraftManager;
import studio.fantasyit.maid_storage_manager.craft.type.ICraftType;
import studio.fantasyit.maid_storage_manager.items.CraftGuide;
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
                            .orElse(false)
                            .forGetter(CraftGuideData::isMergeable),
                    Codec.BOOL.fieldOf(CraftGuide.TAG_MARK_NO_OCCUPY)
                            .orElse(false)
                            .forGetter(CraftGuideData::isNoOccupy)
            ).apply(instance, CraftGuideData::new)
    );
    public static StreamCodec<RegistryFriendlyByteBuf, CraftGuideData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, CraftGuideStepData.STREAM_CODEC),
            CraftGuideData::getSteps,
            ResourceLocation.STREAM_CODEC,
            CraftGuideData::getType,
            ByteBufCodecs.BOOL,
            CraftGuideData::isMergeable,
            ByteBufCodecs.BOOL,
            CraftGuideData::isNoOccupy,
            CraftGuideData::new
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
        this.steps = new ArrayList<>(steps);
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
                    if (ItemStackUtil.removeIsMatchInList(outputs, item, ItemStackUtil::isSameInCrafting).isEmpty())
                        continue;
                    ItemStackUtil.addToList(inputs, item.copy(), ItemStackUtil::isSameInCrafting);
                }

                if (ItemStackUtil.removeIsMatchInList(outputsWithOptional, item, ItemStackUtil::isSameInCrafting).isEmpty())
                    continue;
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
        getInput().forEach(itemStack -> builder.append(itemStack.getItem()).append(","));
        builder.append("->");
        getOutput().forEach(itemStack -> builder.append(itemStack.getItem()).append(","));
        builder.append("}");
        return builder.toString();
    }

    public CraftGuideData copy() {
        return new CraftGuideData(
                steps.stream().map(CraftGuideStepData::copy).toList(),
                type
        );
    }

    @Override
    public int hashCode() {
        return steps.hashCode() * 31 + type.hashCode() * 17;
    }
}
