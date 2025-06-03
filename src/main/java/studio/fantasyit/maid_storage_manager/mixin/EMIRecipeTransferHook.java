package studio.fantasyit.maid_storage_manager.mixin;

import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.widget.RecipeFillButtonWidget;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.widget.RecipeButtonWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import studio.fantasyit.maid_storage_manager.integration.Integrations;
import studio.fantasyit.maid_storage_manager.integration.request.EMIClient;
import studio.fantasyit.maid_storage_manager.integration.request.IngredientRequestClient;

import java.util.List;

@Mixin(RecipeFillButtonWidget.class)
abstract public class EMIRecipeTransferHook extends RecipeButtonWidget {

    public EMIRecipeTransferHook(int x, int y, int u, int v, EmiRecipe recipe) {
        super(x, y, u, v, recipe);
    }

    @ModifyVariable(method = "getTooltip", at = @At("RETURN"), remap = false)
    public List<ClientTooltipComponent> tooltip(List<ClientTooltipComponent> tooltip) {
        if (!Integrations.EMIngredientRequest()) return tooltip;
        if (IngredientRequestClient.keyPressed) {
            tooltip.clear();
            if (IngredientRequestClient.preferMaidId != -1) {
                tooltip.add(ClientTooltipComponent.create(Component.translatable("tooltip.maid_storage_manager.jei_request.from_maid", IngredientRequestClient.preferMaidName).getVisualOrderText()));
            } else {
                tooltip.add(ClientTooltipComponent.create(Component.translatable("tooltip.maid_storage_manager.jei_request.no_maid").getVisualOrderText()));
            }
        }
        return tooltip;
    }

    @Override
    public void render(GuiGraphics raw, int mouseX, int mouseY, float delta) {
        if (Integrations.EMIngredientRequest())
            if (IngredientRequestClient.keyPressed) {
                int offset = super.getTextureOffset(mouseX, mouseY);
                if (IngredientRequestClient.preferMaidId == -1) {
                    offset = 24;
                }
                EmiDrawContext context = EmiDrawContext.wrap(raw);
                context.resetColor();
                context.drawTexture(EmiRenderHelper.BUTTONS,
                        x,
                        y,
                        12,
                        12,
                        72,
                        offset,
                        12,
                        12,
                        256,
                        256);
                IngredientRequestClient.drawIcon(raw, x + 1, y + 1);
                return;
            }
        super.render(raw, mouseX, mouseY, delta);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true, remap = false)
    public void mouseClicked(int mouseX, int mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (!Integrations.EMIngredientRequest()) return;
        if (IngredientRequestClient.keyPressed) {
            if (IngredientRequestClient.preferMaidId != -1) {
                EMIClient.processRequestNearByClient(recipe);
                cir.setReturnValue(true);
            }
        }
    }
}