package studio.fantasyit.maid_storage_manager.maid.behavior.request.terminal;

import appeng.api.parts.IPart;
import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.parts.AEBasePart;
import appeng.parts.reporting.AbstractTerminalPart;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidMoveToBlockTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.util.Conditions;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;
import studio.fantasyit.maid_storage_manager.util.PosUtil;

import java.util.Arrays;

/**
 * 手上持有物品清单，尝试前往附近所有的箱子
 */
public class GoToTerminalMoveBehavior extends MaidMoveToBlockTask {
    public GoToTerminalMoveBehavior() {
        super((float) Config.collectSpeed, 3);
        this.verticalSearchStart = 1;
    }

    BlockPos terminalPos = null;

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel worldIn, @NotNull EntityMaid owner) {
        if (!ModList.get().isLoaded("ae2")) return false;
        if(!Config.enableAe2Sup) return false;
        if (!MemoryUtil.isWorkingRequest(owner)) return false;

        if (!Conditions.takingRequestList(owner)) return false;
        if (Conditions.isTryToReturnStorage(owner)) return false;
        if (Conditions.alreadyArriveTarget(owner)) return false;

        return Conditions.inventoryNotFull(owner) && !Conditions.listAllDone(owner);
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long p_22542_) {
        super.start(level, maid, p_22542_);
        this.searchForDestination(level, maid);
        if (!maid.getBrain().hasMemoryValue(InitEntities.TARGET_POS.get())) {
            MemoryUtil.clearCurrentTerminalPos(maid);
            MemoryUtil.finishTerminal(maid);
        } else {
            if (terminalPos != null) {
                MemoryUtil.setCurrentTerminalPos(maid, terminalPos);
                maid.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(terminalPos));
            }
        }
    }

    @Override
    protected boolean shouldMoveTo(@NotNull ServerLevel serverLevel,
                                   EntityMaid entityMaid,
                                   @NotNull BlockPos blockPos) {
        if (blockPos.equals(RequestListItem.getStorageBlock(entityMaid.getMainHandItem())))
            return false;

        //寻找当前格子能触碰的箱子
        BlockPos canTouchChest = PosUtil.findAroundFromStandPos(blockPos, (pos) -> {
            BlockEntity blockEntity = serverLevel.getBlockEntity(pos);

            if (blockEntity == null)
                return false;

            if (MemoryUtil.getVisitedPos(entityMaid.getBrain()).contains(blockEntity.getBlockPos()))
                return false;

            if (blockEntity instanceof CableBusBlockEntity cbb) {
                return Arrays.stream(Direction
                                .orderedByNearest(entityMaid))
                        .anyMatch(direction -> {
                            IPart part = cbb.getCableBus().getPart(direction);
                            if (part instanceof AbstractTerminalPart atp) {
                                return true;
                            }
                            return false;
                        });
            }

            return false;
        });
        if (canTouchChest != null) {
            terminalPos = canTouchChest;
            return true;
        }
        return false;
    }
}
