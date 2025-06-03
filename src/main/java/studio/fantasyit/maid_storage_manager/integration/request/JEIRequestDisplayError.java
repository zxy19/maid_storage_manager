package studio.fantasyit.maid_storage_manager.integration.request;

import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import net.minecraft.network.chat.Component;

public class JEIRequestDisplayError implements IRecipeTransferError {
    @Override
    public Type getType() {
        return Type.COSMETIC;
    }

    @Override
    public int getButtonHighlightColor() {
        return 0x00673ab7;
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip) {
        tooltip.add(Component.translatable("tooltip.maid_storage_manager.jei_request.from_maid", IngredientRequestClient.preferMaidName));
    }

    public static class NoMaid implements IRecipeTransferError {
        @Override
        public Type getType() {
            return Type.COSMETIC;
        }
        @Override
        public void getTooltip(ITooltipBuilder tooltip) {
            tooltip.add(Component.translatable("tooltip.maid_storage_manager.jei_request.no_maid"));
        }
    }

}
