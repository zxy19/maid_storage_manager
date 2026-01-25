package studio.fantasyit.maid_storage_manager.event;

import com.github.tartaricacid.touhoulittlemaid.api.event.MaidPickupEvent;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.capability.CraftBlockOccupyDataProvider;
import studio.fantasyit.maid_storage_manager.craft.debug.ProgressDebugContext;
import studio.fantasyit.maid_storage_manager.debug.DebugData;
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
        if (MemoryUtil.canPickUpItemTemp(maid, entityItem.getUUID())) return;
        if (MemoryUtil.isWorking(maid)) {
            event.setCanceled(true);
            return;
        }
        switch (MemoryUtil.getCurrentlyWorking(maid)) {
            case PLACE -> {
                if (Conditions.shouldStopAndPickUpItems(maid)) {
                    event.setCanceled(true);
                }
            }
            case VIEW, NO_SCHEDULE, RESORT, SORTING, MEAL -> {
            }
            case CO_WORK -> {
                if (event.isSimulate())
                    event.setCanceled(true);
            }
            default -> event.setCanceled(true);
        }
        if (event.isCanceled())
            return;
        if (!maid.level().isClientSide) {
            CraftBlockOccupyDataProvider.CraftBlockOccupy occupation = CraftBlockOccupyDataProvider.get(maid.level());
            if (BlockPos.betweenClosedStream(
                    entityItem.getBoundingBox().inflate(3.5)
            ).anyMatch(occupation::isOccupiedByAny)) {
                event.setCanceled(true);
                return;
            }
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
                DebugData.sendDebug(maid, ProgressDebugContext.TYPE.STATUS, "Placing Inv Reset(Picking)");
            }
        }
    }
}
