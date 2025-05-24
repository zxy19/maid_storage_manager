package studio.fantasyit.maid_storage_manager.event;

import com.github.tartaricacid.touhoulittlemaid.api.event.MaidPickupEvent;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.debug.DebugData;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.util.Conditions;
import studio.fantasyit.maid_storage_manager.util.InvUtil;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

@Mod.EventBusSubscriber(modid = MaidStorageManager.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MaidItemPickupEvent {
    @SubscribeEvent
    public static void onItemPickup(MaidPickupEvent.ItemResultPre event) {
        EntityMaid maid = event.getMaid();
        ItemEntity entityItem = event.getEntityItem();
        if (Conditions.takingRequestList(maid)) {
            event.setCanceled(true);
            return;
        }
        if (entityItem.getItem().is(ItemRegistry.WRITTEN_INVENTORY_LIST.get())) {
            event.setCanceled(true);
            return;
        }
        if (entityItem.getItem().is(ItemRegistry.REQUEST_LIST_ITEM.get())) {
            if (!(maid).level().isClientSide) {
                int tickCount = (maid).level().getServer().getTickCount();
                Integer restartAt = MemoryUtil.getReturnToScheduleAt(maid);
                if (restartAt != null && tickCount < restartAt) {
                    event.setCanceled(true);
                    return;
                }
                if (!InvUtil.hasAnyFree(maid.getAvailableInv(false))) {
                    event.setCanceled(true);
                    return;
                }
            }
        }
        if (MemoryUtil.getCurrentlyWorking(maid) == ScheduleBehavior.Schedule.PLACE) {
            CombinedInvWrapper inv = maid.getAvailableInv(false);
            if (InvUtil.freeSlots(inv) >= inv.getSlots() * Config.pickupRequireWhenPlace) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onItemPickup(MaidPickupEvent.ItemResultPost event) {
        if (!event.isSimulate()) {
            ItemStack pickupItem = event.getPickupItem();
            EntityMaid maid = event.getMaid();
            if (!pickupItem.is(ItemRegistry.REQUEST_LIST_ITEM.get()) && !pickupItem.is(ItemRegistry.INVENTORY_LIST.get())) {
                MemoryUtil.getPlacingInv(maid).resetVisitedPos();
                if (MemoryUtil.getPlacingInv(maid).hasTarget()) {
                    MemoryUtil.getPlacingInv(maid).clearTarget();
                    MemoryUtil.clearTarget(maid);
                }
                DebugData.sendDebug("Placing Inv Reset(Picking)");
            }
        }
    }
}
