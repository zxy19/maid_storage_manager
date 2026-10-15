package studio.fantasyit.maid_storage_manager.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.gui.elements.GuiIconButton;
import mezz.jei.gui.elements.GuiIconToggleButton;
import mezz.jei.gui.recipes.RecipeTransferButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import studio.fantasyit.maid_storage_manager.integration.Integrations;
import studio.fantasyit.maid_storage_manager.integration.request.IngredientRequestClient;
import studio.fantasyit.maid_storage_manager.integration.request.JEIClient;

/**
 * JEI's toggle button picks its icon from its own on/off drawables right before rendering.
 * Instead of redirecting the whole draw call - which also swallowed the button whenever the
 * integration was disabled - this only wraps the setIcon call and swaps in the request icon
 * while the request key is held. JEI's own rendering stays untouched.
 *
 * <p>
 * The hook lives on the shared superclass because the setIcon call is made there, so it is
 * explicitly scoped to the recipe transfer button, which is the only button the old redirect
 * affected.
 * </p>
 */
@Mixin(GuiIconToggleButton.class)
public abstract class JeiGuiIconToggleButtonHook {

    @WrapWithCondition(method = "draw", remap = false,
            at = @At(value = "INVOKE",
                    target = "Lmezz/jei/gui/elements/GuiIconButton;setIcon(Lmezz/jei/api/gui/drawable/IDrawable;)V"))
    private boolean msm$useRequestIcon(GuiIconButton button, IDrawable icon) {
        if (!Integrations.JEIIngredientRequest()) return true;
        if (!(((Object) this) instanceof RecipeTransferButton)) return true;
        IngredientRequestClient.hasButton();
        if (!IngredientRequestClient.keyPressed) return true;
        button.setIcon(JEIClient.icon);
        return false;
    }
}
