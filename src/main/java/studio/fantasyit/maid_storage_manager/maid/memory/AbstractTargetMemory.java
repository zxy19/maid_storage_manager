package studio.fantasyit.maid_storage_manager.maid.memory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.storage.Storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class AbstractTargetMemory {
    private int failCount;

    public static class TargetData {
        public static final Codec<TargetData> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.list(Storage.CODEC)
                                .fieldOf("visitedPos")
                                .forGetter(TargetData::getVisitedPos),
                        Storage.CODEC.fieldOf("target")
                                .forGetter(TargetData::getTarget),
                        ItemStack.CODEC.optionalFieldOf("check")
                                .forGetter(TargetData::getCheckItem)
                ).apply(instance, TargetData::new));
        public static ResourceLocation NO_TARGET = new ResourceLocation(MaidStorageManager.MODID, "no_target");
        public List<Storage> visitedPos;
        public Storage target;
        @Nullable
        public ItemStack checkItem;

        public TargetData(List<Storage> visitedPos, Storage target) {
            this(visitedPos, target, Optional.empty());
        }

        public TargetData(List<Storage> visitedPos, Storage target, Optional<ItemStack> checkItem) {
            this.visitedPos = new ArrayList<>(visitedPos);
            this.target = target;
            this.checkItem = checkItem.orElse(null);
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

    public void removeVisitedPos(Storage pos) {
        targetData.visitedPos.remove(pos);
    }

    public boolean isVisitedPos(Storage pos) {
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

    public void setCheckItem(ItemStack checkItem) {
        targetData.checkItem = checkItem;
    }

    public void clearCheckItem() {
        targetData.checkItem = null;
    }

    public ItemStack getCheckItem() {
        return targetData.checkItem;
    }
}
