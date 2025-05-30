package studio.fantasyit.maid_storage_manager.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import studio.fantasyit.maid_storage_manager.storage.ItemHandler.SimulateTargetInteractHelper;

@Mixin(ContainerOpenersCounter.class)
public class ContainerOpenersCounterPatch {
    @Inject(method = "getOpenCount", at = @At("RETURN"), cancellable = true)
    private void getOpenCount(Level p_155458_, BlockPos p_155459_, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(
                SimulateTargetInteractHelper.openCount(p_155459_)
                        + cir.getReturnValue()
        );
    }
}