package studio.fantasyit.maid_storage_manager.mixin;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperCategoryMenu;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import studio.fantasyit.maid_storage_manager.api.mixin.ICreateStockKeeperMaidChecker;
import studio.fantasyit.maid_storage_manager.integration.Integrations;

@Mixin(value = StockKeeperCategoryMenu.class,remap = false)
public abstract class CreateStockKeeperMenuMixin implements ICreateStockKeeperMaidChecker {
    @Unique
    private EntityMaid maid_storage_manager$maid = null;

    @Inject(method = "stillValid", at = @At("RETURN"), cancellable = true)
    public void stillValid(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (Integrations.createStockManager() && maid_storage_manager$maid != null && !cir.getReturnValue()) {
            if (maid_storage_manager$maid.distanceTo(player) <= player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE)) {
                cir.setReturnValue(true);
            }
        }
    }

    @Override
    public void maid_storage_manager$setMaid(EntityMaid maid) {
        this.maid_storage_manager$maid = maid;
    }
}
