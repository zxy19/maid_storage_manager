package studio.fantasyit.maid_storage_manager.craft.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.items.CraftGuide;

import java.util.ArrayList;
import java.util.List;

public class CraftGuideData {
    public static Codec<CraftGuideData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    CraftGuideStepData.CODEC.listOf().fieldOf(CraftGuide.TAG_INPUT)
                            .forGetter(CraftGuideData::getInput),
                    CraftGuideStepData.CODEC.listOf().fieldOf(CraftGuide.TAG_OUTPUT)
                            .forGetter(CraftGuideData::getOutput),
                    ResourceLocation.CODEC.fieldOf(CraftGuide.TAG_TYPE)
                            .forGetter(CraftGuideData::getType)
            ).apply(instance, CraftGuideData::new)
    );

    public List<CraftGuideStepData> inputs;
    public List<CraftGuideStepData> outputs;
    public ResourceLocation type;

    public CraftGuideData(List<CraftGuideStepData> inputs, List<CraftGuideStepData> outputs, ResourceLocation type) {
        this.inputs = inputs;
        this.outputs = outputs;
        this.type = type;
    }

    public static CraftGuideData fromItemStack(ItemStack craftGuide) {
        CompoundTag tag = craftGuide.getOrCreateTag();
        ListTag inputs = tag.getList(CraftGuide.TAG_INPUT, Tag.TAG_COMPOUND);
        ResourceLocation type = null;
        ArrayList<CraftGuideStepData> input = new ArrayList<>();
        for (int i = 0; i < inputs.size(); i++) {
            input.add(CraftGuideStepData.fromCompound(inputs.getCompound(i)));
        }
        ListTag outputs = tag.getList(CraftGuide.TAG_OUTPUT, Tag.TAG_COMPOUND);
        ArrayList<CraftGuideStepData> output = new ArrayList<>();
        for (int i = 0; i < outputs.size(); i++) {
            output.add(CraftGuideStepData.fromCompound(outputs.getCompound(i)));
        }
        if (tag.contains(CraftGuide.TAG_TYPE)) {
            type = ResourceLocation.tryParse(tag.getString(CraftGuide.TAG_TYPE));
        }
        return new CraftGuideData(input, output, type);
    }

    public List<CraftGuideStepData> getInput() {
        return inputs;
    }

    public List<CraftGuideStepData> getOutput() {
        return outputs;
    }

    public CraftGuideStepData getStepByIdx(int idx) {
        if (idx < inputs.size())
            return inputs.get(idx);
        return outputs.get(idx - inputs.size());
    }

    public ResourceLocation getType() {
        return type;
    }


    public List<ItemStack> getAllInputItems() {
        return getAllInputItems(null);
    }

    public List<ItemStack> getAllInputItems(Boolean optional) {
        ArrayList<ItemStack> items = new ArrayList<>();
        for (CraftGuideStepData input : inputs) {
            if (optional == null || input.isOptional() == optional)
                items.addAll(input.getNonEmptyItems());
        }
        return items;
    }

    public List<ItemStack> getAllOutputItems() {
        return getAllOutputItems(null);
    }

    public List<ItemStack> getAllOutputItems(Boolean optional) {
        ArrayList<ItemStack> items = new ArrayList<>();
        for (CraftGuideStepData output : outputs) {
            if (optional == null || output.isOptional() == optional)
                items.addAll(output.getNonEmptyItems());
        }
        return items;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CraftGuideData craftGuideData) {
            if (craftGuideData.inputs.size() != this.inputs.size())
                return false;
            if (craftGuideData.outputs.size() != this.outputs.size())
                return false;
            for (int i = 0; i < craftGuideData.inputs.size(); i++) {
                if (!craftGuideData.inputs.get(i).equals(this.inputs.get(i))) return false;
            }
            for (int i = 0; i < craftGuideData.outputs.size(); i++) {
                if (!craftGuideData.outputs.get(i).equals(this.outputs.get(i))) return false;
            }
            return true;
        }
        return false;
    }
}
