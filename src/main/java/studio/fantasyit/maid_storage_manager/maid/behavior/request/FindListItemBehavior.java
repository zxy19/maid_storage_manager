package studio.fantasyit.maid_storage_manager.maid.behavior.request;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.transfer.CombinedResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import studio.fantasyit.maid_storage_manager.advancement.AdvancementTypes;
import studio.fantasyit.maid_storage_manager.api.IRequestTaskHandler;
import studio.fantasyit.maid_storage_manager.api.event.RequestListStatusChangeEvent;
import studio.fantasyit.maid_storage_manager.craft.debug.ProgressDebugContext;
import studio.fantasyit.maid_storage_manager.debug.DebugData;
import studio.fantasyit.maid_storage_manager.maid.ChatTexts;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.Conditions;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;
import studio.fantasyit.maid_storage_manager.util.RequestItemUtil;
import studio.fantasyit.maid_storage_manager.util.StorageAccessUtil;

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
            ItemStack stack = maid.getMainHandItem();
            IRequestTaskHandler handler = IRequestTaskHandler.of(stack);
            return handler == null || !lastWorkUUID.equals(handler.getWorkUUID(stack));
        }
        CombinedResourceHandler<ItemResource> maidInv = maid.getAvailableInv(false);
        for (int i = 0; i < maidInv.size(); i++) {
            ItemStack item = ItemUtil.getStack(maidInv, i);
            if (item.is(ItemRegistry.REQUEST_LIST_ITEM.get())) {
                IRequestTaskHandler itemHandler = IRequestTaskHandler.of(item);
                if (itemHandler != null) itemHandler.tickCoolingDown(item);
                if (itemHandler != null && !itemHandler.isIgnored(item) && !itemHandler.isCoolingDown(item))
                    return true;
            }
        }
        return false;
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long p_22557_) {
        if (!Conditions.takingRequestList(maid)) {
            CombinedResourceHandler<ItemResource> maidInv = maid.getAvailableInv(false);
            for (int i = 0; i < maidInv.size(); i++) {
                ItemStack item = ItemUtil.getStack(maidInv, i);
                if (item.is(ItemRegistry.REQUEST_LIST_ITEM.get())) {
                    IRequestTaskHandler itemHandler = IRequestTaskHandler.of(item);
                    if (itemHandler != null && !itemHandler.isIgnored(item) && !itemHandler.isCoolingDown(item)) {
                        ItemStack itemstack = ItemUtil.getStack(maidInv, i).copyWithCount(1);
                        ItemStack itemInHand = maid.getMainHandItem();
                        try (Transaction tx = Transaction.open(null)) {
                            maidInv.extract(i, ItemResource.of(itemstack), 1, tx);
                            if (!itemInHand.isEmpty()) {
                                maidInv.insert(i, ItemResource.of(itemInHand), itemInHand.getCount(), tx);
                            }
                            tx.commit();
                        }
                        maid.setItemInHand(InteractionHand.MAIN_HAND, itemstack);
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

        ItemStack stack = maid.getMainHandItem();
        IRequestTaskHandler handler = IRequestTaskHandler.of(stack);

        //记忆：开始新的工作
        if (handler != null) {
            NeoForge.EVENT_BUS.post(new RequestListStatusChangeEvent(RequestListStatusChangeEvent.Status.START, maid, handler.getWorkUUID(stack), stack));
            MemoryUtil.getRequestProgress(maid).newWork(handler.getWorkUUID(stack));
        }
        MemoryUtil.clearReturnWorkSchedule(maid);
        MemoryUtil.getCrafting(maid).clearCraftGuides();
        MemoryUtil.getCrafting(maid).clearPlan();

        //标黑存储箱子相连的所有箱子
        Target storageBlock = handler != null ? handler.getStorageBlock(stack) : null;
        if (storageBlock != null) {
            MemoryUtil.getRequestProgress(maid).addVisitedPos(storageBlock);
            DebugData.sendDebug(maid, ProgressDebugContext.TYPE.WORK, "[REQUEST]initial vis %s", storageBlock);
            StorageAccessUtil.checkNearByContainers(level, storageBlock.getPos(), pos -> {
                MemoryUtil.getRequestProgress(maid).addVisitedPos(storageBlock.sameType(pos, null));
                DebugData.sendDebug(maid, ProgressDebugContext.TYPE.WORK, "[REQUEST]initial vis %s", pos.toShortString());
            });
        }

        ChatTexts.send(maid, ChatTexts.CHAT_REQUEST_START);
        AdvancementTypes.triggerForMaid(maid, AdvancementTypes.REQUEST_LIST_GOT);
        if (handler != null && handler.getRepeatInterval(stack) > 0) {
            AdvancementTypes.triggerForMaid(maid, AdvancementTypes.REQUEST_LIST_REPEAT_GOT);
        }
        MemoryUtil.resetParallelWorking(maid);
    }
}