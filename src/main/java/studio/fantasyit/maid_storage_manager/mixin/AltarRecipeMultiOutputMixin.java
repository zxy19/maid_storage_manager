package studio.fantasyit.maid_storage_manager.mixin;

import com.github.tartaricacid.touhoulittlemaid.crafting.AltarRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import studio.fantasyit.maid_storage_manager.registry.DataComponentRegistry;

import java.util.List;

@Mixin(AltarRecipe.class)
public abstract class AltarRecipeMultiOutputMixin extends ShapelessRecipe {
    @Shadow
    @Final
    private ItemStack result;

    public AltarRecipeMultiOutputMixin(String p_249640_, CraftingBookCategory p_249390_, ItemStack p_252071_, NonNullList<Ingredient> p_250689_) {
        super(p_249640_, p_249390_, p_252071_, p_250689_);
    }

    @Inject(method = "spawnItem", at = @At("HEAD"), cancellable = true)
    public void spawnItem(ServerLevel world, BlockPos pos, CallbackInfo ci) {
        if (this.result.has(DataComponentRegistry.TO_SPAWN_ITEMS)) {
            List<ItemStack> list = this.result.get(DataComponentRegistry.TO_SPAWN_ITEMS);
            if (list != null) {
                for (ItemStack itemStack : list) {
                    ItemEntity itemEntity = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), itemStack.copy());
                    world.addFreshEntity(itemEntity);
                }
                ci.cancel();
            }
        }
    }
}
