package studio.fantasyit.maid_storage_manager.menu.craft.tacz;

import com.tacz.guns.crafting.GunSmithTableRecipe;
import com.tacz.guns.init.ModRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.jemi.JemiRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.network.PacketDistributor;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.craft.CraftManager;
import studio.fantasyit.maid_storage_manager.craft.context.special.TaczRecipeAction;
import studio.fantasyit.maid_storage_manager.craft.type.TaczType;
import studio.fantasyit.maid_storage_manager.menu.craft.base.handler.EmiRecipeHandler;
import studio.fantasyit.maid_storage_manager.network.CraftGuideGuiPacket;

import java.util.ArrayList;
import java.util.List;

public class EMITaczRecipeTransfer extends EmiRecipeHandler<TaczCraftMenu, GunSmithTableRecipe> {

    public EMITaczRecipeTransfer() {
        super(null, false);
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return (recipe instanceof JemiRecipe<?> jemiRecipe && jemiRecipe.recipe instanceof GunSmithTableRecipe);
    }

    private RecipeHolder<GunSmithTableRecipe> getHolderOrNull(EmiRecipe recipe, EmiCraftContext<TaczCraftMenu> context) {
        if (recipe instanceof JemiRecipe<?> jemiRecipe && jemiRecipe.recipe instanceof GunSmithTableRecipe gstr) {
            return context
                    .getScreenHandler()
                    .recipeManager()
                    .getAllRecipesFor(ModRecipe.GUN_SMITH_TABLE_CRAFTING.get())
                    .stream()
                    .filter(t -> t.value() == gstr)
                    .findAny()
                    .orElse(null);
        }
        return null;
    }

    @Override
    public boolean canCraft(EmiRecipe recipe, EmiCraftContext<TaczCraftMenu> context) {
        List<Pair<ItemStack, String>> allRecipes = new ArrayList<>();
        context.getScreenHandler().getAllRecipes(allRecipes);
        RecipeHolder<GunSmithTableRecipe> holder = getHolderOrNull(recipe, context);
        if (holder != null && allRecipes.stream().anyMatch(r -> r.getB().equals(holder.id().toString()))) return true;
        return false;
    }

    @Override
    public boolean craft(EmiRecipe recipe, EmiCraftContext<TaczCraftMenu> context) {
        List<Pair<ItemStack, String>> allRecipes = new ArrayList<>();
        context.getScreenHandler().getAllRecipes(allRecipes);
        RecipeHolder<GunSmithTableRecipe> holder = getHolderOrNull(recipe, context);
        if (holder == null)
            return false;
        if (allRecipes.stream().noneMatch(r -> r.getB().equals(holder.id().toString())))
            return false;
        PacketDistributor.sendToServer(new CraftGuideGuiPacket(
                CraftGuideGuiPacket.Type.OPTION,
                CraftManager.getInstance().getAction(TaczType.TYPE).getOptionIndex(TaczRecipeAction.OPTION_TACZ_RECIPE_ID),
                0,
                CraftGuideGuiPacket.singleValue(holder.id().toString())
        ));
        PacketDistributor.sendToServer(new CraftGuideGuiPacket(
                CraftGuideGuiPacket.Type.OPTION,
                CraftManager.getInstance().getAction(TaczType.TYPE).getOptionIndex(TaczRecipeAction.OPTION_TACZ_BLOCK_ID),
                0,
                CraftGuideGuiPacket.singleValue( context.getScreenHandler().getBlockId().toString())
        ));
        return true;
    }
}
