package studio.fantasyit.maid_storage_manager.craft.context.special;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.tacz.guns.crafting.GunSmithTableIngredient;
import com.tacz.guns.crafting.GunSmithTableRecipe;
import com.tacz.guns.init.ModRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import studio.fantasyit.maid_storage_manager.craft.context.AbstractCraftActionContext;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.work.CraftLayer;
import studio.fantasyit.maid_storage_manager.integration.tacz.TaczRecipe;
import studio.fantasyit.maid_storage_manager.util.InvUtil;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TaczRecipeAction extends AbstractCraftActionContext {
    public static final ActionOption<Boolean> OPTION_TACZ_RECIPE_ID = new ActionOption<>(
            new ResourceLocation(MaidStorageManager.MODID, "recipe_id"), "", new ActionOption.BiConverter<>(i -> i != 0, t -> t ? 1 : 0), ActionOption.ValuePredicatorOrGetter.predicator(ResourceLocation::isValidResourceLocation)
    );

    public static final ActionOption<Boolean> OPTION_TACZ_BLOCK_ID = new ActionOption<>(
            new ResourceLocation(MaidStorageManager.MODID, "block_id"), "", new ActionOption.BiConverter<>(i -> i != 0, t -> t ? 1 : 0), ActionOption.ValuePredicatorOrGetter.predicator(ResourceLocation::isValidResourceLocation)
    );

    public TaczRecipeAction(EntityMaid maid, CraftGuideData craftGuideData, CraftGuideStepData craftGuideStepData, CraftLayer layer) {
        super(maid, craftGuideData, craftGuideStepData, layer);
    }

    @Override
    public Result start() {
        if (craftGuideStepData.getStorage() == null)
            return Result.FAIL;
        return Result.CONTINUE;
    }

    @Override
    public Result tick() {
        Level level = maid.level();
        String wantedBlockId = craftGuideStepData.getOptionValue(OPTION_TACZ_BLOCK_ID);
        String recipeId = craftGuideStepData.getOptionValue(OPTION_TACZ_RECIPE_ID);
        if (!TaczRecipe.getBlockId(level, craftGuideStepData.storage.pos).toString().equals(wantedBlockId))
            return Result.NOT_DONE;
        Optional<GunSmithTableRecipe> recipe = level
                .getRecipeManager()
                .getAllRecipesFor(ModRecipe.GUN_SMITH_TABLE_CRAFTING.get())
                .stream().filter(r -> r.id().toString().equals(recipeId))
                .map(RecipeHolder::value)
                .findFirst();
        CombinedInvWrapper inv = maid.getAvailableInv(false);
        List<ItemStack> input = craftGuideStepData.getInput();
        List<ItemStack> output = craftGuideStepData.getOutput();
        List<ItemStack> taken = new ArrayList<>();
        if (recipe.isEmpty())
            return Result.FAIL;

        //配方合法性检查
        List<GunSmithTableIngredient> recipeInput = recipe.get().getInputs();
        for (int i = 0; i < Math.max(input.size(), recipeInput.size()); i++) {
            if (recipeInput.size() <= i) {
                if (!input.get(i).isEmpty())
                    return Result.FAIL;
                else
                    continue;
            }
            if (input.size() <= i)
                return Result.FAIL;
            if (!recipeInput.get(i).getIngredient().test(input.get(i)))
                return Result.FAIL;
        }

        boolean allMatch = true;
        for (ItemStack i : input) {
            ItemStack extracted = InvUtil.tryExtractForCrafting(inv, i);
            taken.add(extracted);
            if (extracted.getCount() != i.getCount()) {
                allMatch = false;
                break;
            }
        }
        if (!allMatch) {
            for (ItemStack i : taken)
                InvUtil.tryPlace(inv, i);
            return Result.FAIL;
        } else {
            ItemStack result = recipe.get().getResultItem(level.registryAccess());
            if (ItemStackUtil.isSameInCrafting(result, output.get(0))) {
                craftLayer.addCurrentStepPlacedCounts(0, result.getCount());
            }

            int maxCanPlace = InvUtil.maxCanPlace(inv, result);
            if (maxCanPlace >= result.getCount()) {
                InvUtil.tryPlace(inv, result);
                return Result.SUCCESS;
            } else {
                return Result.FAIL;
            }
        }
    }

    @Override
    public void stop() {

    }
}
