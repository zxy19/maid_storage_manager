package studio.fantasyit.maid_storage_manager.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.buttons.IButtonState;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.inputs.IJeiUserInput;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import org.jetbrains.annotations.Nullable;
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

/**
 * JEI 19.57 removed GuiIconToggleButton and RecipeTransferButton and replaced them with the
 * IconButton / IIconButtonController pair, so the old widget mixin no longer had a target.
 * The controller is the new hook point: updateState() runs every tick and decides icon/active/visible,
 * onPress() handles the click and getTooltips() delegates to the stored recipeTransferError.
 * Writing our own error into that slot makes JEI draw the highlight and tooltip for us.
 * targets= is used on purpose so this class keeps no compile time dependency on JEI internals.
 */
@Mixin(targets = "mezz.jei.gui.recipes.RecipeTransferButtonController", remap = false)
public abstract class JEIRecipeTransferHook {

    @Shadow
    private @Nullable IRecipeTransferError recipeTransferError;

    @Shadow
    @Final
    private IRecipeLayoutDrawable<?> recipeLayout;

    private static IDrawable defaultTransferIcon;

    /**
     * Records the icon JEI installs in initState() so the button can be restored to its normal look
     * once the request key is released. Returning true leaves JEI's own call untouched, and unlike
     * @Redirect this chains cleanly with other mods targeting the same call.
     */
    @WrapWithCondition(method = "initState", remap = false, at = @At(value = "INVOKE",
            target = "Lmezz/jei/api/gui/buttons/IButtonState;setIcon(Lmezz/jei/api/gui/drawable/IDrawable;)V"))
    private boolean msm$captureTransferIcon(IButtonState state, IDrawable icon) {
        defaultTransferIcon = icon;
        return true;
    }

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
    public void onDrawExtras(GuiGraphics guiGraphics, Rect2i buttonArea, int mouseX, int mouseY,
                             float partialTicks, CallbackInfo ci) {
        if (Integrations.JEIIngredientRequest()) {
            IngredientRequestClient.hasButton();
        }
    }
}
