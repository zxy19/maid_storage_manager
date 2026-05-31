package studio.fantasyit.maid_storage_manager.mixin;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.integration.Integrations;
import studio.fantasyit.maid_storage_manager.maid.task.StorageManageTask;

import java.util.List;

@Mixin(StockTickerBlockEntity.class)
public abstract class CreateStockTickerBEMixin extends BlockEntity {
    public CreateStockTickerBEMixin(BlockEntityType<?> p_155228_, BlockPos p_155229_, BlockState p_155230_) {
        super(p_155228_, p_155229_, p_155230_);
    }

    @Inject(method = "isKeeperPresent", at = @At("RETURN"), cancellable = true, remap = false)
    public void isKeeperPresent(CallbackInfoReturnable<Boolean> cir) {
        if (!Integrations.createStockManager())
            return;
        if (!cir.getReturnValue() && level != null) {
            List<EntityMaid> entities = level.getEntities(
                    EntityTypeTest.forClass(EntityMaid.class),
                    new AABB(getBlockPos())
                            .inflate(Config.createStockKeeperRangeV,
                                    Config.createStockKeeperRangeH,
                                    Config.createStockKeeperRangeV),
                    t -> t.getTask().getUid().equals(StorageManageTask.TASK_ID)
            );
            if (entities.size() > 0)
                cir.setReturnValue(true);
        }
    }
}
