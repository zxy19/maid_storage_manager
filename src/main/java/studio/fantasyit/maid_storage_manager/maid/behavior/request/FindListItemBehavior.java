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
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.registry.MemoryModuleRegistry;
import studio.fantasyit.maid_storage_manager.util.Conditions;
import studio.fantasyit.maid_storage_manager.util.InvUtil;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;
import studio.fantasyit.maid_storage_manager.util.PosUtil;

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
        if (Conditions.inventoryFull(entityMaid)) return false;
        if (Conditions.takingRequestList(entityMaid)) {
            Optional<UUID> lastWorkUUID = MemoryUtil.getLastWorkUUID(entityMaid);
            return lastWorkUUID.map(uuid -> {
                if (RequestListItem.getUUID(entityMaid.getMainHandItem()).equals(uuid))
                    return false;
                return true;
            }).orElse(true);
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

        MemoryUtil.setLastWorkUUID(maid, RequestListItem.getUUID(maid.getMainHandItem()));
        MemoryUtil.clearArriveTarget(maid);
        MemoryUtil.clearReturnToStorage(maid);
        MemoryUtil.clearVisitedPos(maid.getBrain());
        MemoryUtil.clearPosition(maid);
        MemoryUtil.clearCurrentChestPos(maid);
        MemoryUtil.clearCurrentTerminalPos(maid);
        MemoryUtil.clearFinish(maid);
        MemoryUtil.setWorkingRequest(maid, true);
        MemoryUtil.clearReturnWorkSchedule(maid);

        BlockPos storageBlock = RequestListItem.getStorageBlock(maid.getMainHandItem());
        if (storageBlock != null) {
            BlockEntity blockEntity = level.getBlockEntity(storageBlock);
            if (blockEntity != null)
                blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER)
                        .ifPresent(itemHandler -> {
                            InvUtil.checkNearByContainers(level, storageBlock, itemHandler, pos -> {
                                Set<BlockPos> visitedPos = MemoryUtil.getVisitedPos(maid.getBrain());
                                visitedPos.add(PosUtil.getEntityPos(level, pos));
                                maid.getBrain().setMemory(MemoryModuleRegistry.MAID_VISITED_POS.get(), visitedPos);
                            });
                        });
        }
    }
}
