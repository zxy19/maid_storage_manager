package studio.fantasyit.maid_storage_manager.menu.craft.tacz;

import com.tacz.guns.crafting.GunSmithTableRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import net.minecraft.world.item.ItemStack;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.craft.CraftManager;
import studio.fantasyit.maid_storage_manager.craft.context.special.TaczRecipeAction;
import studio.fantasyit.maid_storage_manager.craft.type.TaczType;
import studio.fantasyit.maid_storage_manager.menu.craft.base.handler.EmiRecipeHandler;
import studio.fantasyit.maid_storage_manager.network.CraftGuideGuiPacket;
import studio.fantasyit.maid_storage_manager.network.Network;

import java.util.ArrayList;
import java.util.List;

public class EMITaczRecipeTransfer extends EmiRecipeHandler<TaczCraftMenu, GunSmithTableRecipe> {

    public EMITaczRecipeTransfer() {
        super(null, false);
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return (recipe.getBackingRecipe() instanceof GunSmithTableRecipe);
    }

    @Override
    public boolean canCraft(EmiRecipe recipe, EmiCraftContext<TaczCraftMenu> context) {
        List<Pair<ItemStack, String>> allRecipes = new ArrayList<>();
        context.getScreenHandler().getAllRecipes(allRecipes);
        if (recipe.getBackingRecipe() instanceof GunSmithTableRecipe gstr)
            if (allRecipes.stream().anyMatch(r -> r.getB().equals(gstr.getId().toString()))) return true;
        return false;
    }

    @Override
    public boolean craft(EmiRecipe recipe, EmiCraftContext<TaczCraftMenu> context) {
        List<Pair<ItemStack, String>> allRecipes = new ArrayList<>();
        context.getScreenHandler().getAllRecipes(allRecipes);

        if (!(recipe.getBackingRecipe() instanceof GunSmithTableRecipe gstr))
            return false;
        if (allRecipes.stream().noneMatch(r -> r.getB().equals(gstr.getId().toString())))
            return false;
        Network.INSTANCE.sendToServer(new CraftGuideGuiPacket(
                CraftGuideGuiPacket.Type.OPTION,
                CraftManager.getInstance().getAction(TaczType.TYPE).getOptionIndex(TaczRecipeAction.OPTION_TACZ_RECIPE_ID),
                0,
                CraftGuideGuiPacket.singleValue(gstr.getId().toString())
        ));
        Network.INSTANCE.sendToServer(new CraftGuideGuiPacket(
                CraftGuideGuiPacket.Type.OPTION,
                CraftManager.getInstance().getAction(TaczType.TYPE).getOptionIndex(TaczRecipeAction.OPTION_TACZ_BLOCK_ID),
                0,
                CraftGuideGuiPacket.singleValue( context.getScreenHandler().getBlockId().toString())
        ));
        return true;
    }
}
