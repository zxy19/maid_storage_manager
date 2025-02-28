package studio.fantasyit.maid_storage_manager.maid.behavior.request;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidCheckRateTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.debug.DebugData;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.maid.memory.RequestProgressMemory;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.registry.MemoryModuleRegistry;
import studio.fantasyit.maid_storage_manager.util.*;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * 闲置，在背包查找请求清单，找到则放到主手上
 */
public class FindListItemBehavior extends MaidCheckRateTask {
    public FindListItemBehavior() {
        super(Map.of());
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel p_22538_, EntityMaid entityMaid) {
        if (Conditions.takingRequestList(entityMaid)) {
            UUID lastWorkUUID = MemoryUtil.getRequestProgress(entityMaid).getWorkUUID();
            return !lastWorkUUID.equals(RequestListItem.getUUID(entityMaid.getMainHandItem()));
        }
        IItemHandler maidInv = entityMaid.getAvailableBackpackInv();
        for (int i = 0; i < maidInv.getSlots(); i++) {
            ItemStack item = maidInv.getStackInSlot(i);
            if (item.is(ItemRegistry.REQUEST_LIST_ITEM.get()))
                if (!RequestListItem.isIgnored(item))
                    return true;
        }
        return false;
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long p_22557_) {
        //获取请求清单，将其交换到主手
        if (!Conditions.takingRequestList(maid)) {
            IItemHandler maidInv = maid.getAvailableBackpackInv();
            for (int i = 0; i < maidInv.getSlots(); i++) {
                ItemStack item = maidInv.getStackInSlot(i);
                if (maidInv.getStackInSlot(i).is(ItemRegistry.REQUEST_LIST_ITEM.get())) {
                    if (!RequestListItem.isIgnored(item)) {
                        @NotNull ItemStack itemstack = maidInv.extractItem(i, 1, false);
                        maidInv.insertItem(i, maid.getMainHandItem(), false);
                        maid.setItemInHand(InteractionHand.MAIN_HAND, itemstack);
                        break;
                    }
                }
            }
        }
        //背包已满，停止工作，将清单丢掉
        if (Conditions.inventoryFull(maid)) {
            RequestItemUtil.stopJobAndStoreOrThrowItem(maid, null);
            return;
        }

        //记忆：开始新的工作
        MemoryUtil.getRequestProgress(maid).newWork(RequestListItem.getUUID(maid.getMainHandItem()));
        MemoryUtil.clearReturnWorkSchedule(maid);

        //标黑存储箱子相连的所有箱子
        BlockPos storageBlock = RequestListItem.getStorageBlock(maid.getMainHandItem());
        if (storageBlock != null) {
            MemoryUtil.getRequestProgress(maid).addVisitedPos(storageBlock);
            DebugData.getInstance().sendMessage("[REQUEST]initial vis %s", storageBlock.toShortString());
            InvUtil.checkNearByContainers(level, storageBlock, pos -> {
                MemoryUtil.getRequestProgress(maid).addVisitedPos(pos);
                DebugData.getInstance().sendMessage("[REQUEST]initial vis %s", pos.toShortString());
            });
        }
    }
}