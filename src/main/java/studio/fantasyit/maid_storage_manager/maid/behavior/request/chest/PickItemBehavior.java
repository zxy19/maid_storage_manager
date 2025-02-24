package studio.fantasyit.maid_storage_manager.maid.behavior.request.chest;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.registry.MemoryModuleRegistry;
import studio.fantasyit.maid_storage_manager.util.*;

import java.util.*;

public class PickItemBehavior extends Behavior<EntityMaid> {

    @Nullable SimulateTargetInteractHelper helper;

    public PickItemBehavior() {
        super(Map.of(), 10000000);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, EntityMaid maid, long p_22547_) {
        if (!Conditions.hasCurrentChestPos(maid)) return false;
        if (!Conditions.takingRequestList(maid)) return false;
        if (!Conditions.inventoryNotFull(maid)) return false;
        if (!Conditions.listNotDone(maid)) return false;
        return helper != null && !helper.doneTaking();
    }

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel level, @NotNull EntityMaid maid) {
        if (!MemoryUtil.isWorkingRequest(maid)) return false;

        if (!Conditions.hasCurrentChestPos(maid)) return false;
        if (!Conditions.takingRequestList(maid)) return false;
        if (!Conditions.inventoryNotFull(maid)) return false;
        if (!Conditions.listNotDone(maid)) return false;
        if (Conditions.alreadyArriveTarget(maid)) return true;
        return Conditions.hasReachedValidTargetOrReset(maid);
    }


    @Override
    protected void start(@NotNull ServerLevel level, @NotNull EntityMaid maid, long gameTimeIn) {
        BlockPos target = MemoryUtil.getCurrentChestPos(maid);
        if (target == null)
            return;
        MemoryUtil.arriveTarget(maid);
        helper = new SimulateTargetInteractHelper(maid, target, level);
        helper.open();
    }

    @Override
    protected void stop(ServerLevel level, EntityMaid maid, long p_22550_) {
        super.stop(level, maid, p_22550_);
        if (helper != null) {
            if (helper.doneTaking()) {
                Set<BlockPos> visitedPos = MemoryUtil.getVisitedPos(maid.getBrain());
                visitedPos.add(PosUtil.getEntityPos(level, helper.target));
                if (helper.itemHandler != null)
                    InvUtil.checkNearByContainers(level, helper.target, helper.itemHandler, pos -> {
                        visitedPos.add(PosUtil.getEntityPos(level, pos));
                    });
                maid.getBrain().setMemory(MemoryModuleRegistry.MAID_VISITED_POS.get(), visitedPos);
            }
            helper.stop();
        }
        helper = null;
        MemoryUtil.clearArriveTarget(maid);
        MemoryUtil.clearPosition(maid);
        MemoryUtil.clearCurrentChestPos(maid);
    }

    @Override
    protected void tick(ServerLevel level, EntityMaid maid, long p_22553_) {
        if (helper != null) {
            helper.takeItemTick((i, targetInv, maxStore) -> RequestListItem
                    .updateCollectedItem(maid.getMainHandItem(), i, maxStore)
                    .getCount());
        }
    }
}
