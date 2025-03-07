package studio.fantasyit.maid_storage_manager.maid.behavior.request.craft.gather;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidMoveToBlockTask;
import com.github.tartaricacid.touhoulittlemaid.entity.chatbubble.ChatBubbleManger;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.debug.DebugData;
import studio.fantasyit.maid_storage_manager.maid.ChatTexts;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.maid.memory.ViewedInventoryMemory;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;
import studio.fantasyit.maid_storage_manager.storage.Storage;
import studio.fantasyit.maid_storage_manager.util.Conditions;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;
import studio.fantasyit.maid_storage_manager.util.MoveUtil;
import studio.fantasyit.maid_storage_manager.util.PosUtil;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 手上持有物品清单，尝试前往附近所有的箱子
 */
public class RequestCraftGatherMoveBehavior extends MaidMoveToBlockTask {
    public RequestCraftGatherMoveBehavior() {
        super((float) Config.collectSpeed, 3);
        this.verticalSearchStart = 1;
    }

    Storage chestPos = null;

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel worldIn, @NotNull EntityMaid owner) {
        if (MemoryUtil.getCurrentlyWorking(owner) != ScheduleBehavior.Schedule.REQUEST) return false;
        if (!Conditions.takingRequestList(owner)) return false;
        if (MemoryUtil.getCrafting(owner).hasStartWorking()) return false;
        if (!MemoryUtil.getCrafting(owner).hasCurrent()) return false;
        return true;
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long p_22542_) {
        super.start(level, maid, p_22542_);
        if (MemoryUtil.getCrafting(maid).getCurrentLayer().hasCollectedAll()) {
            MemoryUtil.getCrafting(maid).finishGathering(maid);
            return;
        }
        if (!this.priorityTarget(level, maid))
            this.searchForDestination(level, maid);
        if (!maid.getBrain().hasMemoryValue(InitEntities.TARGET_POS.get())) {
            DebugData.getInstance().sendMessage("[REQUEST_CRAFT_GATHER] No More Target");
            MemoryUtil.getCrafting(maid).finishGathering(maid);
        } else {
            if (chestPos != null) {
                MemoryUtil.getCrafting(maid).setTarget(chestPos);
                MemoryUtil.setLookAt(maid, chestPos.getPos());
                DebugData.getInstance().sendMessage("[REQUEST_CRAFT_GATHER] Target %s", chestPos);
            }
            ChatTexts.send(maid, ChatTexts.CHAT_CRAFT_GATHER);
        }
    }

    private boolean priorityTarget(ServerLevel level, EntityMaid maid) {
        List<ItemStack> targets = Objects.requireNonNull(MemoryUtil.getCrafting(maid).getCurrentLayer()).getItems();
        if (targets.isEmpty()) return false;
        Map<Storage, List<ViewedInventoryMemory.ItemCount>> viewed = MemoryUtil.getViewedInventory(maid).positionFlatten();
        for (Map.Entry<Storage, List<ViewedInventoryMemory.ItemCount>> blockPos : viewed.entrySet()) {
            if (MemoryUtil.getCrafting(maid).isVisitedPos(blockPos.getKey())) continue;
            if (blockPos
                    .getValue()
                    .stream()
                    .noneMatch(itemCount ->
                            targets
                                    .stream()
                                    .anyMatch(i2 ->
                                            ItemStack.isSameItemSameTags(i2, itemCount.getItem())
                                    )
                    )
            ) {
                continue;
            }

            @Nullable BlockPos targetPos = MoveUtil.selectPosForTarget(level, maid, blockPos.getKey().getPos());
            if (targetPos != null) {
                @Nullable Storage storage = MaidStorage.getInstance().isValidTarget(level,
                        maid,
                        blockPos.getKey().getPos(),
                        blockPos.getKey().side);
                if (storage != null) {
                    if (!MoveUtil.isValidTarget(level, maid, storage)) continue;
                    chestPos = storage;
                    MemoryUtil.setTarget(maid, targetPos, (float) Config.placeSpeed);
                    DebugData.getInstance().sendMessage("[REQUEST_CRAFT_GATHER]Priority By Content %s", storage);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean shouldMoveTo(@NotNull ServerLevel serverLevel,
                                   EntityMaid entityMaid,
                                   @NotNull BlockPos blockPos) {
        if (!PosUtil.isSafePos(serverLevel, blockPos)) return false;
        //寻找当前格子能触碰的箱子
        @Nullable Storage canTouchChest = MoveUtil.findTargetForPos(serverLevel, entityMaid, blockPos, MemoryUtil.getCrafting(entityMaid));
        if (canTouchChest != null) {
            chestPos = canTouchChest;
            DebugData.getInstance().sendMessage("[REQUEST_CRAFT_GATHER]Target %s", canTouchChest);
        }
        return canTouchChest != null;
    }
}
