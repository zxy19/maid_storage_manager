package studio.fantasyit.maid_storage_manager.mixin;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import studio.fantasyit.maid_storage_manager.debug.DebugData;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.util.Conditions;
import studio.fantasyit.maid_storage_manager.util.InvUtil;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

@Mixin(EntityMaid.class)
public abstract class MaidCheckPickupItem {
    @Shadow(remap = false)
    public abstract CombinedInvWrapper getAvailableInv(boolean handsFirst);


    @Inject(method = "pickupItem", at = @At("HEAD"), cancellable = true, remap = false)
    public void maid_storage_manager$pickupItem(ItemEntity entityItem, boolean simulate, CallbackInfoReturnable<Boolean> cir) {
        if (Conditions.canTempPickUp((EntityMaid) (Object) this, entityItem.getItem())) {
            Conditions.clearTempPickUp((EntityMaid) (Object) this);
            return;
        }
        if (Conditions.takingRequestList((EntityMaid) (Object) this)) {
            cir.setReturnValue(false);
            return;
        }
        if (entityItem.getItem().is(ItemRegistry.WRITTEN_INVENTORY_LIST.get())) {
            cir.setReturnValue(false);
            return;
        }
        if (entityItem.getItem().is(ItemRegistry.REQUEST_LIST_ITEM.get())) {
            if (!InvUtil.hasAnyFree(getAvailableInv(false))) {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "pickupItem", at = @At("RETURN"), remap = false)
    public void maid_storage_manager$afterPickupItem(ItemEntity entityItem, boolean simulate, CallbackInfoReturnable<Boolean> cir) {
        if (!simulate && cir.getReturnValue())
            if (!entityItem.getItem().is(ItemRegistry.REQUEST_LIST_ITEM.get())
                    &&
                    !entityItem.getItem().is(ItemRegistry.INVENTORY_LIST.get())) {
                MemoryUtil.getPlacingInv((EntityMaid) (Object) this).resetVisitedPos();
                if (MemoryUtil.getPlacingInv((EntityMaid) (Object) this).hasTarget()) {
                    MemoryUtil.getPlacingInv((EntityMaid) (Object) this).clearTarget();
                    MemoryUtil.clearTarget((EntityMaid) (Object) this);
                }
                DebugData.getInstance().sendMessage("Placing Inv Reset(Picking)");
            }
    }
}