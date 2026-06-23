package studio.fantasyit.maid_storage_manager.mixin;

import com.github.tartaricacid.touhoulittlemaid.crafting.AltarRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import studio.fantasyit.maid_storage_manager.items.data.ItemStackData;
import studio.fantasyit.maid_storage_manager.registry.DataComponentRegistry;

import java.util.List;

@Mixin(AltarRecipe.class)
public abstract class AltarRecipeMultiOutputMixin {

    @Shadow
    @Final
    private ItemStackTemplate result;

    @Inject(method = "spawnItem", at = @At("HEAD"), cancellable = true)
    public void spawnItem(ServerLevel world, BlockPos pos, CallbackInfo ci) {
        if (this.result.has(DataComponentRegistry.TO_SPAWN_ITEMS)) {
            List<ItemStackData> list = this.result.get(DataComponentRegistry.TO_SPAWN_ITEMS);
            if (list != null) {
                for (ItemStackData itemStack : list) {
                    ItemStack copy = itemStack.itemStack().copy();
                    if (copy.has(DataComponentRegistry.TO_SPAWN_ITEMS))
                        copy.remove(DataComponentRegistry.TO_SPAWN_ITEMS);
                    ItemEntity itemEntity = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), copy);
                    world.addFreshEntity(itemEntity);
                }
                ci.cancel();
            }
        }
    }
}
