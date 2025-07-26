package studio.fantasyit.maid_storage_manager.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import studio.fantasyit.maid_storage_manager.storage.ItemHandler.SimulateTargetInteractHelper;

@Mixin(value = ContainerOpenersCounter.class,remap = false)
public class ContainerOpenersCounterPatch {
    @ModifyVariable(method = "recheckOpeners", at = @At(value = "INVOKE_ASSIGN", target = "Ljava/util/List;size()I"))
    private int getOpenCount(int i, @Local(argsOnly = true) BlockPos pos) {
        return SimulateTargetInteractHelper.openCount(pos) + i;
    }
}