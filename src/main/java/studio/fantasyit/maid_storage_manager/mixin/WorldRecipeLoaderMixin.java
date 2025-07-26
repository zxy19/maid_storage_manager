package studio.fantasyit.maid_storage_manager.mixin;

import net.minecraft.server.ReloadableServerResources;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import studio.fantasyit.maid_storage_manager.craft.generator.cache.GraphCache;
import studio.fantasyit.maid_storage_manager.craft.generator.cache.RecipeIngredientCache;

@Mixin(value = ReloadableServerResources.class,remap = false)
public abstract class WorldRecipeLoaderMixin {
    @Shadow
    public abstract RecipeManager getRecipeManager();

    @Inject(method = "updateRegistryTags()V", at = @At("RETURN"))
    private void load(CallbackInfo ci) {
        RecipeIngredientCache.preFetchCache(this.getRecipeManager());
        GraphCache.invalidateAll();
    }
}
