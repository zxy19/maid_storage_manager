package studio.fantasyit.maid_storage_manager.maid.behavior.view;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidCheckRateTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.maid.memory.ViewedInventoryMemory;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.util.*;

import java.util.Map;
import java.util.UUID;

public class ViewStorageAndStoreBehavior extends MaidCheckRateTask {

    private SimulateTargetInteractHelper helper = null;
    private boolean isViewing = false;

    public ViewStorageAndStoreBehavior() {
        super(Map.of());
        this.setMaxCheckRate(5);
    }

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel worldIn, @NotNull EntityMaid owner) {
        if (!super.checkExtraStartConditions(worldIn, owner)) return false;
        if (MemoryUtil.isWorkingRequest(owner)) return false;

        return Conditions.hasReachedValidTargetOrReset(owner);
    }

    @Override
    protected boolean canStillUse(ServerLevel p_22545_, EntityMaid maid, long p_22547_) {
        if(Conditions.isWaitingForReturn(maid))return false;
        if (isViewing && helper.doneViewing()) return false;
        return true;
    }

    @Override
    protected void tick(ServerLevel p_22551_, EntityMaid maid, long p_22553_) {
        super.tick(p_22551_, maid, p_22553_);
        if (helper != null) {
            if (isViewing) {
                ViewedInventoryMemory viewedInventory = MemoryUtil.getViewedInventory(maid);
                helper.viewItemTick(itemStack -> {
                    viewedInventory.addItem(helper.target, itemStack);
                });
            } else {
                helper.placeItemTick((itemStack, targetInv, maxStore) -> {
                    if (itemStack.is(ItemRegistry.REQUEST_LIST_ITEM.get())) {
                        if (RequestListItem.isIgnored(itemStack)) {
                            CompoundTag tag = itemStack.getOrCreateTag();
                            tag.putBoolean(RequestListItem.TAG_IGNORE_TASK, false);
                            itemStack.setTag(tag);
                            return Math.min(itemStack.getCount(), maxStore);
                        }
                        return 0;
                    }
                    return Math.min(itemStack.getCount(), maxStore);
                });
                if (helper.donePlacing()) {
        isViewing = true;
        helper.reset();
    }
}
}
    }

@Override
protected void start(ServerLevel level, EntityMaid maid, long p_22542_) {
        BlockPos pos = MemoryUtil.getCurrentChestPos(maid);
        if (pos == null) return;
        helper = new SimulateTargetInteractHelper(maid, pos, level);
        helper.open();
        MemoryUtil.getViewedInventory(maid).resetViewedInvForPos(pos);
        isViewing = false;
    }

    @Override
    protected void stop(ServerLevel level, EntityMaid maid, long p_22550_) {
        if (helper != null) {
            if (isViewing && helper.doneViewing()) {
                MemoryUtil.getViewedInventory(maid).addVisitedPos(PosUtil.getEntityPos(level, helper.target));
                if (helper.itemHandler != null) {
                    InvUtil.checkNearByContainers(level, helper.target, helper.itemHandler, pos -> {
                        MemoryUtil.getViewedInventory(maid).resetViewedInvForPos(pos);
                        MemoryUtil.getViewedInventory(maid).addVisitedPos(pos);
                    });
                }
                MemoryUtil.clearCurrentChestPos(maid);
                MemoryUtil.clearPosition(maid);
            }
            helper.stop();
        }
    }

}
