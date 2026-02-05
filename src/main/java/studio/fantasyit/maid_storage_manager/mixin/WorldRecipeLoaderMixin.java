package studio.fantasyit.maid_storage_manager.mixin;

import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import studio.fantasyit.maid_storage_manager.craft.generator.cache.GraphCache;
import studio.fantasyit.maid_storage_manager.craft.generator.cache.RecipeIngredientCache;

@Mixin(ReloadableServerResources.class)
public abstract class WorldRecipeLoaderMixin {
    @Shadow
    public abstract RecipeManager getRecipeManager();

    @Inject(method = "updateRegistryTags(Lnet/minecraft/core/RegistryAccess;)V", at = @At("RETURN"))
    private void load(RegistryAccess p_206869_, CallbackInfo ci) {
        RecipeIngredientCache.preFetchCache(this.getRecipeManager());
        GraphCache.invalidateAll();
    }
}
