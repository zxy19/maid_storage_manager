package studio.fantasyit.maid_storage_manager.maid.memory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractTargetMemory {
    public static class TargetData {
        public static final Codec<TargetData> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.list(BlockPos.CODEC)
                                .fieldOf("visitedPos")
                                .forGetter(TargetData::getVisitedPos),
                        ResourceLocation.CODEC.fieldOf("targetType")
                                .forGetter(TargetData::getTargetType),
                        BlockPos.CODEC.fieldOf("targetPos")
                                .forGetter(TargetData::getTargetPos)
                ).apply(instance, TargetData::new));
        public static ResourceLocation NO_TARGET = new ResourceLocation(MaidStorageManager.MODID, "no_target");
        public List<BlockPos> visitedPos;
        public ResourceLocation targetType;
        public BlockPos targetPos;

        public TargetData(List<BlockPos> visitedPos, ResourceLocation targetType, BlockPos targetPos) {
            this.visitedPos = new ArrayList<>(visitedPos);
            this.targetType = targetType;
            this.targetPos = targetPos;
        }

        public List<BlockPos> getVisitedPos() {
            return visitedPos;
        }

        public ResourceLocation getTargetType() {
            return targetType;
        }

        public BlockPos getTargetPos() {
            return targetPos;
        }
    }

    public TargetData targetData;

    public TargetData getTargetData() {
        return targetData;
    }

    public AbstractTargetMemory(TargetData targetData) {
        this.targetData = targetData;
    }

    public AbstractTargetMemory() {
        targetData = new TargetData(new ArrayList<>(), TargetData.NO_TARGET, BlockPos.ZERO);
    }

    public ResourceLocation getTargetType() {
        return targetData.targetType;
    }

    public BlockPos getTargetPos() {
        return targetData.targetPos;
    }


    public List<BlockPos> getVisitedPos() {
        return targetData.visitedPos;
    }

    public void addVisitedPos(BlockPos pos) {
        targetData.visitedPos.add(pos);
    }

    public void resetVisitedPos() {
        targetData.visitedPos.clear();
    }

    public boolean isVisitedPos(BlockPos pos) {
        return targetData.visitedPos.contains(pos);
    }

    public void setTarget(ResourceLocation a, BlockPos b) {
        targetData.targetType = a;
        targetData.targetPos = b;
    }

    public void clearTarget() {
        targetData.targetType = TargetData.NO_TARGET;
        targetData.targetPos = BlockPos.ZERO;
    }

    public boolean hasTarget() {
        return !targetData.targetType.equals(TargetData.NO_TARGET);
    }
}
