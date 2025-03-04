package studio.fantasyit.maid_storage_manager.maid.memory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.storage.Storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class AbstractTargetMemory {

    public static class TargetData {
        public static final Codec<TargetData> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.list(Storage.CODEC)
                                .fieldOf("visitedPos")
                                .forGetter(TargetData::getVisitedPos),
                        Storage.CODEC.fieldOf("target")
                                .forGetter(TargetData::getTarget)
                ).apply(instance, TargetData::new));
        public static ResourceLocation NO_TARGET = new ResourceLocation(MaidStorageManager.MODID, "no_target");
        public List<Storage> visitedPos;
        public Storage target;

        public TargetData(List<Storage> visitedPos, Storage target) {
            this.visitedPos = new ArrayList<>(visitedPos);
            this.target = target;
        }

        public List<Storage> getVisitedPos() {
            return visitedPos;
        }


        public Storage getTarget() {
            return target;
        }

        public void setTarget(Storage target) {
            this.target = target;
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
        targetData = new TargetData(new ArrayList<>(), new Storage(TargetData.NO_TARGET, BlockPos.ZERO, Optional.empty()));
    }

    public List<Storage> getVisitedPos() {
        return targetData.visitedPos;
    }

    public void addVisitedPos(Storage pos) {
        targetData.visitedPos.add(pos);
    }

    public void resetVisitedPos() {
        targetData.visitedPos.clear();
    }

    public boolean isVisitedPos(Storage pos) {
        return targetData.visitedPos.contains(pos);
    }


    public Storage getTarget() {
        return targetData.getTarget();
    }
    public void setTarget(Storage target) {
        targetData.setTarget(target);
    }

    public void clearTarget() {
        targetData.setTarget(new Storage(TargetData.NO_TARGET, BlockPos.ZERO, Optional.empty()));
    }

    public boolean hasTarget() {
        return !targetData.getTarget().type.equals(TargetData.NO_TARGET);
    }
}
