package studio.fantasyit.maid_storage_manager.craft.generator.type.create;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.MaidPathFindingBFS;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.kinetics.fan.AirCurrent;
import com.simibubi.create.content.kinetics.fan.EncasedFanBlockEntity;
import com.simibubi.create.content.kinetics.fan.processing.AllFanProcessingTypes;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessingType;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonPlaceItemAction;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonTakeItemAction;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.generator.algo.GeneratorGraph;
import studio.fantasyit.maid_storage_manager.craft.generator.cache.RecipeIngredientCache;
import studio.fantasyit.maid_storage_manager.craft.type.CommonType;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;
import studio.fantasyit.maid_storage_manager.integration.create.CreateIntegration;
import studio.fantasyit.maid_storage_manager.storage.ItemHandler.ItemHandlerStorage;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.MathUtil;

import java.util.ArrayList;
import java.util.List;

public class GeneratorCreateFanRecipes extends GeneratorCreate<ProcessingRecipe<RecipeWrapper>, RecipeType<ProcessingRecipe<RecipeWrapper>>, RecipeWrapper> {
    @Override
    public ResourceLocation getType() {
        return new ResourceLocation(MaidStorageManager.MODID, "create_fan");
    }

    @Override
    public boolean allowMultiPosition() {
        return true;
    }

    @Override
    public boolean positionalAvailable(ServerLevel level, EntityMaid maid, BlockPos pos, MaidPathFindingBFS pathFinding) {
        return true;
    }

    @Override
    public boolean isBlockValid(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(AllBlocks.ENCASED_FAN.get());
    }

    @Override
    protected int getMinFullBucketCount(ProcessingRecipe<RecipeWrapper> recipe) {
        if (recipe.getResultItem(RegistryAccess.EMPTY).getMaxStackSize() == 1)
            return super.getMinFullBucketCount(recipe);
        return MathUtil.lcm(super.getMinFullBucketCount(recipe), 8);
    }

    @Deprecated
    @Override
    RecipeType<ProcessingRecipe<RecipeWrapper>> getRecipeType() {
        return null;
    }

    @Override
    public void generate(List<InventoryItem> inventory, Level level, BlockPos pos, GeneratorGraph graph) {
        BlockEntity _be = level.getBlockEntity(pos);
        if (_be instanceof EncasedFanBlockEntity be) {
            AirCurrent airCurrent = be.getAirCurrent();
            int dv = airCurrent.pushing ? 1 : -1;
            Direction dir = airCurrent.direction;
            for (int i = 1; i <= airCurrent.maxDistance; i++) {
                BlockPos test = pos.relative(dir, i * dv);
                if (level.getBlockState(test).is(AllBlocks.DEPOT.get())) {
                    FanProcessingType typeAt = airCurrent.getTypeAt(i);
                    if (typeAt != null) {
                        generateFor(typeAt, level, test, graph);
                    }
                } else if (level.getBlockState(test.below()).is(AllBlocks.DEPOT.get())) {
                    FanProcessingType typeAt = airCurrent.getTypeAt(i);
                    if (typeAt != null) {
                        generateFor(typeAt, level, test.below(), graph);
                    }
                }
            }
        }
    }

    private static boolean isProcessingRecipe(RecipeType<?> recipe) {
        return recipe == AllRecipeTypes.HAUNTING.getType() || recipe == AllRecipeTypes.SPLASHING.getType();
    }

    private static RecipeType<?> getRecipeType(FanProcessingType typeAt) {
        if (typeAt instanceof AllFanProcessingTypes.BlastingType)
            return RecipeType.BLASTING;
        else if (typeAt instanceof AllFanProcessingTypes.SmokingType)
            return RecipeType.SMOKING;
        else if (typeAt instanceof AllFanProcessingTypes.HauntingType)
            return AllRecipeTypes.HAUNTING.getType();
        else if (typeAt instanceof AllFanProcessingTypes.SplashingType)
            return AllRecipeTypes.SPLASHING.getType();
        return null;
    }

    private void generateFor(FanProcessingType typeAt, Level level, BlockPos test, GeneratorGraph graph) {
        RecipeType<?> type = getRecipeType(typeAt);
        if (type != null) {
            if (isProcessingRecipe(type))
                addRecipeForPos(level, test, (RecipeType<ProcessingRecipe<RecipeWrapper>>) type, graph, recipe -> true);
            else
                level.getRecipeManager()
                        .getAllRecipesFor((RecipeType<Recipe<Container>>) type)
                        .forEach((recipe) -> {
                            graph.addRecipe(recipe, items -> {
                                List<CraftGuideStepData> steps = new ArrayList<>();
                                each3items(items, t -> steps.add(new CraftGuideStepData(
                                        new Target(ItemHandlerStorage.TYPE, test),
                                        t,
                                        List.of(),
                                        CommonPlaceItemAction.TYPE,
                                        false,
                                        new CompoundTag()
                                )));
                                steps.add(new CraftGuideStepData(
                                        new Target(ItemHandlerStorage.TYPE, test),
                                        List.of(),
                                        List.of(recipe.getResultItem(level.registryAccess())),
                                        CommonTakeItemAction.TYPE,
                                        false,
                                        new CompoundTag()
                                ));
                                return new CraftGuideData(
                                        steps,
                                        CommonType.TYPE
                                );
                            });
                        });
        }
    }

    @Override
    public void onCache(RecipeManager manager) {
        CreateIntegration.getFanProcessingTypes().forEach(typeAt -> {
            RecipeType<?> type = getRecipeType(typeAt);
            if (type == null) return;
            if (isProcessingRecipe(type)) {
                manager.getAllRecipesFor((RecipeType<ProcessingRecipe<RecipeWrapper>>) type)
                        .stream()
                        .forEach(processingRecipe -> {
                            List<Ingredient> itemIngredients = optionalIngredientList(processingRecipe.getIngredients()).orElse(List.of());
                            List<Ingredient> fluidBuckets = transformFluidIngredient(processingRecipe.getFluidIngredients()).orElse(List.of());
                            List<Ingredient> all = new ArrayList<>();
                            all.addAll(itemIngredients);
                            all.addAll(fluidBuckets);
                            List<Integer> counts = new ArrayList<>();
                            all.forEach(t -> counts.add(0));
                            transformAllIngredients(processingRecipe, all, counts);
                            RecipeIngredientCache.addRecipeCache(
                                    processingRecipe.getId(),
                                    all
                            );
                        });
            } else {
                manager.getAllRecipesFor((RecipeType<Recipe<Container>>) type).forEach(RecipeIngredientCache::addRecipeCache);
            }
        });
    }
}
