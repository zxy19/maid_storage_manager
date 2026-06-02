package studio.fantasyit.maid_storage_manager.mixin;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.buttons.IButtonState;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.inputs.IJeiUserInput;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.Rect2i;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import studio.fantasyit.maid_storage_manager.integration.Integrations;
import studio.fantasyit.maid_storage_manager.integration.request.IngredientRequestClient;
import studio.fantasyit.maid_storage_manager.integration.request.JEIClient;
import studio.fantasyit.maid_storage_manager.integration.request.JEIRequestDisplayError;

import javax.annotation.Nullable;

@Mixin(targets = "mezz.jei.gui.recipes.RecipeTransferButtonController", remap = false)
public abstract class JEIRecipeTransferHook {

    @Shadow
    private @Nullable IRecipeTransferError recipeTransferError;

    @Shadow
    @Final
    private IRecipeLayoutDrawable<?> recipeLayout;

    private static IDrawable defaultTransferIcon;

    @Inject(method = "updateState", at = @At("RETURN"))
    public void onUpdateState(IButtonState state, CallbackInfo ci) {
        if (!Integrations.JEIIngredientRequest()) return;
        if (IngredientRequestClient.keyPressed) {
            this.recipeTransferError = IngredientRequestClient.preferMaidId != -1
                    ? new JEIRequestDisplayError()
                    : new JEIRequestDisplayError.NoMaid();
            state.setActive(true);
            state.setVisible(true);
            state.setIcon(JEIClient.icon);
        } else if (defaultTransferIcon != null) {
            state.setIcon(defaultTransferIcon);
        }
    }

    @Inject(method = "initState", at = @At("RETURN"))
    public void onInitState(IButtonState state, CallbackInfo ci) {
        if (defaultTransferIcon == null) {
            try {
                Class<?> internalClass = Class.forName("mezz.jei.common.Internal");
                Object textures = internalClass.getMethod("getTextures").invoke(null);
                defaultTransferIcon = (IDrawable) textures.getClass().getMethod("getRecipeTransfer").invoke(textures);
            } catch (Exception ignored) {
            }
        }
    }

    @Inject(method = "onPress", at = @At("HEAD"), cancellable = true)
    public void onOnPress(IJeiUserInput input, CallbackInfoReturnable<Boolean> cir) {
        if (!Integrations.JEIIngredientRequest()) return;
        if (!input.isSimulate() && IngredientRequestClient.keyPressed) {
            if (IngredientRequestClient.preferMaidId != -1) {
                JEIClient.processRequestNearByClient(this.recipeLayout);
            }
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "drawExtras", at = @At("HEAD"))
    public void onDrawExtras(GuiGraphicsExtractor guiGraphics, Rect2i buttonArea, int mouseX, int mouseY,
                             float partialTicks, CallbackInfo ci) {
        if (Integrations.JEIIngredientRequest()) {
            IngredientRequestClient.hasButton();
        }
    }
}
