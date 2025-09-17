package studio.fantasyit.maid_storage_manager.maid.behavior.request;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.advancement.AdvancementTypes;
import studio.fantasyit.maid_storage_manager.debug.DebugData;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.maid.ChatTexts;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.Conditions;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;
import studio.fantasyit.maid_storage_manager.util.RequestItemUtil;
import studio.fantasyit.maid_storage_manager.util.StorageAccessUtil;
import studio.fantasyit.tour_guide.api.TourGuideTrigger;

import java.util.Map;
import java.util.UUID;

/**
 * 闲置，在背包查找请求清单，找到则放到主手上
 */
public class FindListItemBehavior extends Behavior<EntityMaid> {
    public FindListItemBehavior() {
        super(Map.of());
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        if (MemoryUtil.getCrafting(maid).isSwappingHandWhenCrafting())
            return false;
        if (Conditions.takingRequestList(maid)) {
            UUID lastWorkUUID = MemoryUtil.getRequestProgress(maid).getWorkUUID();
            return !lastWorkUUID.equals(RequestListItem.getUUID(maid.getMainHandItem()));
        }
        IItemHandler maidInv = maid.getAvailableInv(false);
        for (int i = 0; i < maidInv.getSlots(); i++) {
            ItemStack item = maidInv.getStackInSlot(i);
            if (item.is(ItemRegistry.REQUEST_LIST_ITEM.get())) {
                RequestListItem.tickCoolingDown(item);
                if (!RequestListItem.isIgnored(item) && !RequestListItem.isCoolingDown(item))
                    return true;
            }
        }
        return false;
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long p_22557_) {
        //获取请求清单，将其交换到主手
        if (!Conditions.takingRequestList(maid)) {
            IItemHandler maidInv = maid.getAvailableInv(false);
            for (int i = 0; i < maidInv.getSlots(); i++) {
                ItemStack item = maidInv.getStackInSlot(i);
                if (maidInv.getStackInSlot(i).is(ItemRegistry.REQUEST_LIST_ITEM.get())) {
                    if (!RequestListItem.isIgnored(item) && !RequestListItem.isCoolingDown(item)) {
                        @NotNull ItemStack itemstack = maidInv.extractItem(i, 1, false);
                        maidInv.insertItem(i, maid.getMainHandItem(), false);
                        maid.setItemInHand(InteractionHand.MAIN_HAND, itemstack);
                        if(maid.getOwner() instanceof ServerPlayer sp)
                            TourGuideTrigger.trigger(sp, "request_list_take");
                        break;
                    }
                }
            }
        }
        //背包已满，停止工作，将清单丢掉
        if (Conditions.inventoryFull(maid)) {
            RequestItemUtil.stopJobAndStoreOrThrowItem(maid, null, null);
            return;
        }

        //记忆：开始新的工作
        MemoryUtil.getRequestProgress(maid).newWork(RequestListItem.getUUID(maid.getMainHandItem()));
        MemoryUtil.clearReturnWorkSchedule(maid);
        MemoryUtil.getCrafting(maid).clearCraftGuides();
        MemoryUtil.getCrafting(maid).clearPlan();

        //标黑存储箱子相连的所有箱子
        Target storageBlock = RequestListItem.getStorageBlock(maid.getMainHandItem());
        if (storageBlock != null) {
            MemoryUtil.getRequestProgress(maid).addVisitedPos(storageBlock);
            DebugData.sendDebug("[REQUEST]initial vis %s", storageBlock);
            StorageAccessUtil.checkNearByContainers(level, storageBlock.getPos(), pos -> {
                MemoryUtil.getRequestProgress(maid).addVisitedPos(storageBlock.sameType(pos, null));
                DebugData.sendDebug("[REQUEST]initial vis %s", pos.toShortString());
            });
        }

        ChatTexts.send(maid, ChatTexts.CHAT_REQUEST_START);
        AdvancementTypes.triggerForMaid(maid, AdvancementTypes.REQUEST_LIST_GOT);
        if (RequestListItem.getRepeatInterval(maid.getMainHandItem()) > 0) {
            AdvancementTypes.triggerForMaid(maid, AdvancementTypes.REQUEST_LIST_REPEAT_GOT);
        }
        MemoryUtil.resetParallelWorking(maid);
    }
}