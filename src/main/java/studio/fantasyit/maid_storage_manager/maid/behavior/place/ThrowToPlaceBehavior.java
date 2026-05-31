package studio.fantasyit.maid_storage_manager.maid.behavior.place;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.CombinedResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.api.IRequestTaskHandler;
import studio.fantasyit.maid_storage_manager.craft.debug.ProgressDebugContext;
import studio.fantasyit.maid_storage_manager.debug.DebugData;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.util.BehaviorBreath;
import studio.fantasyit.maid_storage_manager.util.Conditions;
import studio.fantasyit.maid_storage_manager.util.InvUtil;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

import java.util.Map;

public class ThrowToPlaceBehavior extends Behavior<EntityMaid> {
    BehaviorBreath breath = new BehaviorBreath();
    int count = 0;

    public ThrowToPlaceBehavior() {
        super(Map.of(), 1200);
    }

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel worldIn, @NotNull EntityMaid owner) {
        if (MemoryUtil.getCurrentlyWorking(owner) != ScheduleBehavior.Schedule.PLACE) return false;
        if (!super.checkExtraStartConditions(worldIn, owner)) return false;
        if (owner.getVehicle() == null) return false;
        return true;
    }

    @Override
    protected boolean canStillUse(ServerLevel p_22545_, EntityMaid maid, long p_22547_) {
        if (Conditions.isWaitingForReturn(maid)) return false;
        if (count >= maid.getAvailableInv(false).size()) {
            return !MemoryUtil.getViewedInventory(maid).getWaitingAdd().isEmpty();
        }
        return true;
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long p_22542_) {
        count = 0;
        MemoryUtil.setWorking(maid, true);
    }

    @Override
    protected void tick(ServerLevel p_22551_, EntityMaid maid, long p_22553_) {
        super.tick(p_22551_, maid, p_22553_);
        if (!breath.breathTick(maid)) return;
        CombinedResourceHandler<ItemResource> inv = maid.getAvailableInv(false);
        if (count >= inv.size()) {
            return;
        }
        ItemStack stackInSlot = ItemUtil.getStack(inv, count);
        if (stackInSlot.isEmpty()) {
            count++;
            return;
        }
        if (stackInSlot.is(ItemRegistry.REQUEST_LIST_ITEM.get())) {
            IRequestTaskHandler handler = IRequestTaskHandler.of(stackInSlot);
            if (handler == null || !handler.isIgnored(stackInSlot)) {
                count++;
                return;
            }
        }
        try (Transaction tx = Transaction.open(null)) {
            int extracted = inv.extract(count, ItemResource.of(stackInSlot), stackInSlot.getCount(), tx);
            tx.commit();
            stackInSlot = stackInSlot.copyWithCount(extracted);
        }
        InvUtil.throwItem(maid, stackInSlot);
        MemoryUtil.getViewedInventory(maid).addWaitingAdd(stackInSlot);

        count++;
        if (count >= inv.size())
            DebugData.sendDebug(maid, ProgressDebugContext.TYPE.WORK, "[THROW]Start waiting");
    }


    @Override
    protected void stop(ServerLevel level, EntityMaid maid, long p_22550_) {
        DebugData.sendDebug(maid, ProgressDebugContext.TYPE.WORK, "[THROW]done");
        MemoryUtil.getViewedInventory(maid).clearWaitingAdd();
        MemoryUtil.setWorking(maid, false);
        MemoryUtil.getCrafting(maid).tryStartIfHasPlan();
    }
}
