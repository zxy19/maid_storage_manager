package studio.fantasyit.maid_storage_manager.craft.generator.type.create;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.kinetics.mixer.MixingRecipe;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonUseAction;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.generator.algo.GeneratorGraph;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;
import studio.fantasyit.maid_storage_manager.storage.Target;

import java.util.List;

public class GeneratorCreateMix extends GeneratorCreate<MixingRecipe, RecipeType<MixingRecipe>, Container> {
    @Override
    public ResourceLocation getType() {
        return new ResourceLocation(MaidStorageManager.MODID, "create_mixing");
    }

    @Override
    public boolean isBlockValid(Level level, BlockPos pos) {
        if (level.getBlockState(pos).is(AllBlocks.BASIN.get()) && level.getBlockState(pos.above().above()).is(AllBlocks.MECHANICAL_MIXER.get())) {
            return true;
        }
        return false;
    }

    @Override
    public boolean allowMultiPosition() {
        return true;
    }

    @Override
    RecipeType<MixingRecipe> getRecipeType() {
        return AllRecipeTypes.MIXING.getType();
    }

    @Override
    public void transformAllIngredients(MixingRecipe recipe, List<Ingredient> all, List<Integer> counts) {
        if (recipe.getRequiredHeat() == HeatCondition.HEATED) {
            all.add(Ingredient.of(ItemTags.COALS));
            counts.add(1);
        } else if (recipe.getRequiredHeat() == HeatCondition.SUPERHEATED) {
            all.add(Ingredient.of(AllItems.BLAZE_CAKE));
            counts.add(1);
        }
    }

    @Override
    protected void transformSteps(MixingRecipe recipe, List<ItemStack> items, Level level, BlockPos pos, List<CraftGuideStepData> step, StepGenerateStep generateStep) {
        if (recipe.getRequiredHeat() != HeatCondition.NONE && generateStep == StepGenerateStep.INPUT_ITEM) {
            step.add(new CraftGuideStepData(
                    Target.virtual(pos.below(), null),
                    List.of(items.get(items.size() - 1)),
                    List.of(),
                    CommonUseAction.TYPE_R,
                    true,
                    new CompoundTag()
            ));
        }
    }


    @Override
    public void generate(List<InventoryItem> inventory, Level level, BlockPos pos, GeneratorGraph graph) {
        addRecipeForPos(
                level,
                pos,
                getRecipeType(),
                graph,
                t -> t.getRequiredHeat() == HeatCondition.NONE || level.getBlockState(pos.below()).is(AllBlocks.BLAZE_BURNER.get())
        );
    }
}