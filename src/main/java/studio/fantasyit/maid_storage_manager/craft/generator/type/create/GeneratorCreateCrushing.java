package studio.fantasyit.maid_storage_manager.craft.generator.type.create;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.kinetics.crusher.AbstractCrushingRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonPickupItemAction;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonTakeItemAction;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.generator.algo.GeneratorGraph;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.MathUtil;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class GeneratorCreateCrushing extends GeneratorCreate<AbstractCrushingRecipe, RecipeType<AbstractCrushingRecipe>, RecipeWrapper> {
    @Override
    public ResourceLocation getType() {
        return new ResourceLocation(MaidStorageManager.MODID, "create_crushing");
    }

    @Override
    public boolean isBlockValid(Level level, BlockPos pos) {
        if (level.getBlockState(pos).is(AllBlocks.CRUSHING_WHEEL_CONTROLLER.get())) {
            return true;
        }
        return false;
    }

    //至少输入16个，保证效率
    @Override
    protected int getMinFullBucketCount(AbstractCrushingRecipe recipe) {
        if (recipe.getResultItem(RegistryAccess.EMPTY).getMaxStackSize() == 1)
            return super.getMinFullBucketCount(recipe);
        return MathUtil.lcm(super.getMinFullBucketCount(recipe), 16);
    }

    @Override
    public void generate(List<InventoryItem> inventory, Level level, BlockPos pos, GeneratorGraph graph) {
        super.generate(inventory, level, pos, graph);
        addRecipeForPos(level, pos, AllRecipeTypes.MILLING.getType(), graph, t -> {
            ItemStackHandler itemStackHandler = new ItemStackHandler(
                    NonNullList.of(
                            ItemStack.EMPTY,
                            t
                                    .getIngredients()
                                    .stream()
                                    .map(Ingredient::getItems)
                                    .map(Arrays::stream)
                                    .map(Stream::findFirst)
                                    .map(t1 -> t1.orElse(ItemStack.EMPTY))
                                    .toArray(ItemStack[]::new)
                    )
            );
            return level.getRecipeManager()
                    .getRecipeFor(AllRecipeTypes.CRUSHING.getType(), new RecipeWrapper(itemStackHandler), level)
                    .isEmpty();
        });
    }

    @Override
    protected void transformSteps(AbstractCrushingRecipe recipe, List<ItemStack> items, Level level, BlockPos pos, List<CraftGuideStepData> step, StepGenerateStep generateStep) {
        if (generateStep == StepGenerateStep.OUTPUT_ITEM_SELECTIVE) {
            BlockPos testPos = pos.below();
            ResourceLocation action;
            if (level.getBlockState(testPos).isAir()) {
                while (level.getBlockState(testPos).isAir()) {
                    testPos = testPos.below();
                }
                testPos = testPos.above();
                action = CommonPickupItemAction.TYPE;
            } else {
                testPos = pos;
                while (level.getBlockEntity(testPos.below()).getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent()) {
                    testPos = testPos.below();
                }
                action = CommonTakeItemAction.TYPE;
            }

            for (CraftGuideStepData pStep : step) {
                if (pStep.getActionType().equals(CommonTakeItemAction.TYPE)) {
                    pStep.setAction(action);
                    pStep.storage = Target.virtual(testPos, null);
                }
            }
        }
    }

    @Override
    RecipeType<AbstractCrushingRecipe> getRecipeType() {
        return AllRecipeTypes.CRUSHING.getType();
    }
}