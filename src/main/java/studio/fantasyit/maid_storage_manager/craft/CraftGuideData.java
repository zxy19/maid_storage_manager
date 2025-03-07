package studio.fantasyit.maid_storage_manager.craft;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.items.CraftGuide;

import java.util.Optional;

public class CraftGuideData {
    public static Codec<CraftGuideData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    CraftGuideStepData.CODEC.optionalFieldOf(CraftGuide.TAG_INPUT_1)
                            .forGetter(CraftGuideData::getInput1O),
                    CraftGuideStepData.CODEC.optionalFieldOf(CraftGuide.TAG_INPUT_2)
                            .forGetter(CraftGuideData::getInput2O),
                    CraftGuideStepData.CODEC.optionalFieldOf(CraftGuide.TAG_OUTPUT)
                            .forGetter(CraftGuideData::getOutputO)
            ).apply(instance, CraftGuideData::new)
    );

    public CraftGuideStepData input1;
    public CraftGuideStepData input2;
    public CraftGuideStepData output;

    public CraftGuideData(Optional<CraftGuideStepData> input1, Optional<CraftGuideStepData> input2, Optional<CraftGuideStepData> output) {
        this.input1 = input1.orElse(null);
        this.input2 = input2.orElse(null);
        this.output = output.orElse(null);
    }

    public CraftGuideData(CraftGuideStepData input1, CraftGuideStepData input2, CraftGuideStepData output) {
        this(
                Optional.ofNullable(input1),
                Optional.ofNullable(input2),
                Optional.ofNullable(output)
        );
    }

    public static CraftGuideData fromItemStack(ItemStack craftGuide) {
        CompoundTag tag = craftGuide.getOrCreateTag();
        return new CraftGuideData(
                CraftGuideStepData.fromCompound(tag.getCompound(CraftGuide.TAG_INPUT_1)),
                CraftGuideStepData.fromCompound(tag.getCompound(CraftGuide.TAG_INPUT_2)),
                CraftGuideStepData.fromCompound(tag.getCompound(CraftGuide.TAG_OUTPUT))
        );
    }

    public CraftGuideStepData getInput1() {
        return input1;
    }

    public CraftGuideStepData getInput2() {
        return input2;
    }

    public CraftGuideStepData getOutput() {
        return output;
    }

    public Optional<CraftGuideStepData> getInput1O() {
        return Optional.ofNullable(input1);
    }

    public Optional<CraftGuideStepData> getInput2O() {
        return Optional.ofNullable(input2);
    }

    public Optional<CraftGuideStepData> getOutputO() {
        return Optional.ofNullable(output);
    }


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CraftGuideData craftGuideData) {
            return craftGuideData.input1.equals(this.input1) &&
                    craftGuideData.input2.equals(this.input2) &&
                    craftGuideData.output.equals(this.output);
        }
        return false;
    }
}
