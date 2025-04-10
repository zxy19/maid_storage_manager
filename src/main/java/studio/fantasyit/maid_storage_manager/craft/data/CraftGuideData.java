package studio.fantasyit.maid_storage_manager.craft.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.craft.CraftManager;
import studio.fantasyit.maid_storage_manager.craft.type.CommonType;
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
                            .forGetter(CraftGuideData::getType)
            ).apply(instance, CraftGuideData::new)
    );

    public List<CraftGuideStepData> steps;
    public ResourceLocation type;
    public List<ItemStack> inputs;
    public List<ItemStack> inputsWithOptional;
    public List<ItemStack> outputs;
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
        for (CraftGuideStepData step : steps) {
            List<ItemStack> input = step.getInput();
            for (ItemStack item : input) {
                if (ItemStackUtil.removeIsMatchInList(outputs, item, step.isMatchTag()).isEmpty()) continue;
                ItemStackUtil.addToList(inputs, item, step.isMatchTag());
            }
            for (ItemStack item : step.getOutput()) {
                ItemStackUtil.addToList(outputs, item, step.isMatchTag());
            }
        }
    }

    public static CraftGuideData fromItemStack(ItemStack craftGuide) {
        CompoundTag tag = craftGuide.getOrCreateTag();
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

    public List<ItemStack> getInput() {
        return this.inputs;
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
        return getAllInputItems(null);
    }

    public List<ItemStack> getAllInputItems(Boolean optional) {
        ArrayList<ItemStack> items = new ArrayList<>();
        for (CraftGuideStepData input : steps) {
            if (optional == null || input.isOptional() == optional)
                items.addAll(input.getNonEmptyInput());
        }
        return items;
    }

    public List<ItemStack> getAllOutputItems() {
        return getAllOutputItems(null);
    }

    public List<ItemStack> getAllOutputItems(Boolean optional) {
        ArrayList<ItemStack> items = new ArrayList<>();
        for (CraftGuideStepData output : steps) {
            if (optional == null || output.isOptional() == optional)
                items.addAll(output.getNonEmptyOutput());
        }
        return items;
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
