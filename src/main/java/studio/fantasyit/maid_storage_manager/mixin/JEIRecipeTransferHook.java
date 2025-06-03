package studio.fantasyit.maid_storage_manager.mixin;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.gui.elements.GuiIconToggleButton;
import mezz.jei.gui.input.UserInput;
import mezz.jei.gui.recipes.RecipeTransferButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import studio.fantasyit.maid_storage_manager.api.IJEIButtonGetter;
import studio.fantasyit.maid_storage_manager.integration.Integrations;
import studio.fantasyit.maid_storage_manager.integration.request.IngredientRequestClient;
import studio.fantasyit.maid_storage_manager.integration.request.JEIClient;
import studio.fantasyit.maid_storage_manager.integration.request.JEIRequestDisplayError;

@Mixin(RecipeTransferButton.class)
abstract public class JEIRecipeTransferHook extends GuiIconToggleButton {

    @Shadow(remap = false)
    private @Nullable IRecipeTransferError recipeTransferError;

    @Shadow(remap = false)
    @Final
    private IRecipeLayoutDrawable<?> recipeLayout;

    @Shadow(remap = false)
    @Final
    private Runnable onClose;

    public JEIRecipeTransferHook(IDrawable offIcon, IDrawable onIcon) {
        super(offIcon, onIcon);
    }

    @Inject(method = "update", at = @At(value = "RETURN"), remap = false)
    public void update(AbstractContainerMenu parentContainer, Player player, CallbackInfo ci) {
        if (!Integrations.JEIIngredientRequest()) return;
        if (IngredientRequestClient.keyPressed) {
            if (IngredientRequestClient.preferMaidId != -1)
                this.recipeTransferError = new JEIRequestDisplayError();
            else
                this.recipeTransferError = new JEIRequestDisplayError.NoMaid();
            this.button.active = true;
            this.button.visible = true;
        }
    }

    @Redirect(method = "draw", at = @At(value = "INVOKE", target = "Lmezz/jei/gui/elements/GuiIconToggleButton;draw(Lnet/minecraft/client/gui/GuiGraphics;IIF)V"), remap = false)
    public void draw(GuiIconToggleButton instance, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (!Integrations.JEIIngredientRequest()) return;
        if (instance instanceof IJEIButtonGetter ja) {
            if (ja.getArea().isEmpty())
                return;

            boolean iconToggledOn = isIconToggledOn();
            IDrawable icon = iconToggledOn ? ja.getOnIcon() : ja.getOffIcon();
            if (IngredientRequestClient.keyPressed) {
                icon = JEIClient.icon;
            }
            this.button.setForcePressed(iconToggledOn);
            this.button.setIcon(icon);
            this.button.render(guiGraphics, mouseX, mouseY, partialTicks);
        }
    }

    @Inject(method = "onMouseClicked", at = @At(value = "HEAD"), cancellable = true, remap = false)
    public void onMouseClicked(UserInput input, CallbackInfoReturnable<Boolean> cir) {
        if (!Integrations.JEIIngredientRequest()) return;
        if (!input.isSimulate())
            if (IngredientRequestClient.keyPressed) {
                if (IngredientRequestClient.preferMaidId != -1) {
                    JEIClient.processRequestNearByClient(this.recipeLayout);
                }
                cir.setReturnValue(true);
            }
    }
}
