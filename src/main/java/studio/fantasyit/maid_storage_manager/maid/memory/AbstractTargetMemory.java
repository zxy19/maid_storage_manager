package studio.fantasyit.maid_storage_manager.maid.memory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.storage.Target;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class AbstractTargetMemory {
    private int failCount;
    private int pathFindingFailCount;


    public static class TargetData {
        public static final Codec<TargetData> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.list(Target.CODEC)
                                .fieldOf("visitedPos")
                                .forGetter(TargetData::getVisitedPos),
                        Target.CODEC.fieldOf("target")
                                .forGetter(TargetData::getTarget),
                        ItemStack.CODEC.optionalFieldOf("check")
                                .forGetter(TargetData::getCheckItem)
                ).apply(instance, TargetData::new));
        public static ResourceLocation NO_TARGET = ResourceLocation.fromNamespaceAndPath(MaidStorageManager.MODID, "no_target");
        public List<Target> visitedPos;
        public Target target;
        @Nullable
        public ItemStack checkItem;

        public TargetData(List<Target> visitedPos, Target target) {
            this(visitedPos, target, Optional.empty());
        }

        public TargetData(List<Target> visitedPos, Target target, Optional<ItemStack> checkItem) {
            this.visitedPos = new ArrayList<>(visitedPos);
            this.target = target;
            this.checkItem = checkItem.orElse(null);
            if (target == null) this.target = new Target(TargetData.NO_TARGET, BlockPos.ZERO, Optional.empty());
        }

        public List<Target> getVisitedPos() {
            return visitedPos;
        }


        public Target getTarget() {
            return target;
        }

        public void setTarget(Target target) {
            this.target = target;
        }

        public Optional<ItemStack> getCheckItem() {
            return Optional.ofNullable(checkItem);
        }
    }

    public TargetData targetData;

    public TargetData getTargetData() {
        return targetData;
    }

    public AbstractTargetMemory(TargetData targetData) {
        this.targetData = targetData;
        failCount = 0;
    }

    public AbstractTargetMemory() {
        targetData = new TargetData(new ArrayList<>(), new Target(TargetData.NO_TARGET, BlockPos.ZERO, Optional.empty()));
    }

    public List<Target> getVisitedPos() {
        return targetData.visitedPos;
    }

    public void addVisitedPos(Target pos) {
        if (isVisitedPos(pos)) return;
        targetData.visitedPos.add(pos);
    }

    public void resetVisitedPos() {
        targetData.visitedPos.clear();
    }

    public void removeVisitedPos(Target pos) {
        targetData.visitedPos.remove(pos);
    }

    public boolean isVisitedPos(Target pos) {
        return targetData.visitedPos.contains(pos);
    }

    public int getFailCount() {
        return this.failCount;
    }

    public void addFailCount() {
        this.failCount++;
    }

    public void resetFailCount() {
        this.failCount = 0;
    }

    public boolean confirmNoTarget() {
        return confirmNoTarget(30);
    }

    public boolean confirmNoTarget(int threshold) {
        addFailCount();
        return this.failCount >= threshold;
    }

    public Target getTarget() {
        return targetData.getTarget();
    }

    public void setTarget(Target target) {
        targetData.setTarget(target);
    }

    public void clearTarget() {
        targetData.setTarget(new Target(TargetData.NO_TARGET, BlockPos.ZERO, Optional.empty()));
    }

    public boolean hasTarget() {
        return !targetData.getTarget().type.equals(TargetData.NO_TARGET);
    }

    public void setCheckItem(ItemStack checkItem) {
        targetData.checkItem = checkItem;
    }

    public void clearCheckItem() {
        targetData.checkItem = null;
    }

    public ItemStack getCheckItem() {
        return targetData.checkItem != null ? targetData.checkItem : ItemStack.EMPTY;
    }
}