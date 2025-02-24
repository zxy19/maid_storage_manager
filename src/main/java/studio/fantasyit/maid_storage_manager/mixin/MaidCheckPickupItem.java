package studio.fantasyit.maid_storage_manager.mixin;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraftforge.items.wrapper.RangedWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.util.InvUtil;

@Mixin(EntityMaid.class)
public abstract class MaidCheckPickupItem {
    @Shadow
    public abstract RangedWrapper getAvailableBackpackInv();

    @Inject(method = "pickupItem", at = @At("HEAD"), cancellable = true, remap = false)
    public void maid_storage_manager$pickupItem(ItemEntity entityItem, boolean simulate, CallbackInfoReturnable<Boolean> cir) {
        if (entityItem.getItem().is(ItemRegistry.WRITTEN_INVENTORY_LIST.get())) {
            cir.setReturnValue(false);
        }
        if (entityItem.getItem().is(ItemRegistry.REQUEST_LIST_ITEM.get())) {
            if (entityItem.getItem().getOrCreateTag().getBoolean(RequestListItem.TAG_IGNORE_TASK)) {
                if (!InvUtil.hasAnyFree(getAvailableBackpackInv()))
                    cir.setReturnValue(false);
            }
        }
    }
}
