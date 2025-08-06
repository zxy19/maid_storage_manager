package studio.fantasyit.maid_storage_manager.mixin;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.MaidPathFindingBFS;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = MaidPathFindingBFS.class)
public abstract class TLMFeatureCenterSidePathFinding {
    @Redirect(
            method = "<init>(Lnet/minecraft/world/level/pathfinder/NodeEvaluator;Lnet/minecraft/server/level/ServerLevel;Lcom/github/tartaricacid/touhoulittlemaid/entity/passive/EntityMaid;FI)V",
            at = @At(value = "INVOKE", target = "Lcom/github/tartaricacid/touhoulittlemaid/entity/passive/EntityMaid;blockPosition()Lnet/minecraft/core/BlockPos;", remap = true),
            remap = false
    )
    public BlockPos modify(EntityMaid maid) {
        return maid.hasRestriction() ? maid.getRestrictCenter() : maid.blockPosition();
    }
}
