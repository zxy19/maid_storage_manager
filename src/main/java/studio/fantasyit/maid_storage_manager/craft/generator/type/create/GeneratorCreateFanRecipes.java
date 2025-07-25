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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonPlaceItemAction;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonTakeItemAction;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.generator.algo.ICachableGeneratorGraph;
import studio.fantasyit.maid_storage_manager.craft.generator.cache.RecipeIngredientCache;
import studio.fantasyit.maid_storage_manager.craft.generator.config.ConfigTypes;
import studio.fantasyit.maid_storage_manager.craft.generator.util.GenerateCondition;
import studio.fantasyit.maid_storage_manager.craft.type.CommonType;
import studio.fantasyit.maid_storage_manager.craft.type.FurnaceType;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;
import studio.fantasyit.maid_storage_manager.integration.create.CreateIntegration;
import studio.fantasyit.maid_storage_manager.storage.ItemHandler.ItemHandlerStorage;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.MathUtil;
import studio.fantasyit.maid_storage_manager.util.StorageAccessUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GeneratorCreateFanRecipes extends GeneratorCreate<ProcessingRecipe<RecipeWrapper>, RecipeType<ProcessingRecipe<RecipeWrapper>>, RecipeWrapper, BlockPos> {
    ConfigTypes.ConfigType<Integer> COUNT = new ConfigTypes.ConfigType<>(
            "count",
            8,
            Component.translatable("config.maid_storage_manager.crafting.generating.create.fan.count"),
            ConfigTypes.ConfigTypeEnum.Integer
    );
    ConfigTypes.ConfigType<Boolean> BLASTING = new ConfigTypes.ConfigType<>(
            "blasting",
            true,
            Component.translatable("config.maid_storage_manager.crafting.generating.create.fan.blasting"),
            ConfigTypes.ConfigTypeEnum.Boolean
    );
    ConfigTypes.ConfigType<Boolean> SMOKING = new ConfigTypes.ConfigType<>(
            "smoking",
            true,
            Component.translatable("config.maid_storage_manager.crafting.generating.create.fan.smoking"),
            ConfigTypes.ConfigTypeEnum.Boolean
    );
    ConfigTypes.ConfigType<Boolean> HAUNTING = new ConfigTypes.ConfigType<>(
            "haunting",
            true,
            Component.translatable("config.maid_storage_manager.crafting.generating.create.fan.haunting"),
            ConfigTypes.ConfigTypeEnum.Boolean
    );
    ConfigTypes.ConfigType<Boolean> SPLASHING = new ConfigTypes.ConfigType<>(
            "splashing",
            true,
            Component.translatable("config.maid_storage_manager.crafting.generating.create.fan.splashing"),
            ConfigTypes.ConfigTypeEnum.Boolean
    );
    ConfigTypes.ConfigType<Boolean> REPLACE_FURNACE = new ConfigTypes.ConfigType<>(
            "replace_furnace",
            true,
            Component.translatable("config.maid_storage_manager.crafting.generating.create.fan.replace_furnace"),
            ConfigTypes.ConfigTypeEnum.Boolean
    );

    @Override
    public @NotNull ResourceLocation getType() {
        return new ResourceLocation("create", "fan");
    }

    @Override
    public boolean allowMultiPosition() {
        return true;
    }

    @Override
    public boolean canCacheGraph() {
        return false;
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
        return MathUtil.lcm(super.getMinFullBucketCount(recipe), COUNT.getValue());
    }

    @Deprecated
    @Override
    RecipeType<ProcessingRecipe<RecipeWrapper>> getRecipeType() {
        return null;
    }

    @Override
    public void generate(List<InventoryItem> inventory, Level level, BlockPos pos, ICachableGeneratorGraph graph, Map<ResourceLocation, List<BlockPos>> recognizedTypePositions) {
        Pair<MutableBoolean, MutableBoolean> furnaceReplace = new Pair<>(new MutableBoolean(), new MutableBoolean());
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
                        generateFor(typeAt, level, test, graph, furnaceReplace);
                    }
                } else if (level.getBlockState(test.below()).is(AllBlocks.DEPOT.get())) {
                    FanProcessingType typeAt = airCurrent.getTypeAt(i);
                    if (typeAt != null) {
                        generateFor(typeAt, level, test.below(), graph, furnaceReplace);
                    }
                }
            }
            if (furnaceReplace.getA().getValue() && furnaceReplace.getB().getValue() && REPLACE_FURNACE.getValue())
                graph.blockType(FurnaceType.TYPE);
        }
    }

    private static boolean isProcessingRecipe(RecipeType<?> recipe) {
        return recipe == AllRecipeTypes.HAUNTING.getType() || recipe == AllRecipeTypes.SPLASHING.getType();
    }

    private RecipeType<?> getRecipeType(FanProcessingType typeAt) {
        if (typeAt instanceof AllFanProcessingTypes.BlastingType) {
            if (BLASTING.getValue())
                return RecipeType.BLASTING;
        } else if (typeAt instanceof AllFanProcessingTypes.SmokingType) {
            if (SMOKING.getValue())
                return RecipeType.SMOKING;
        } else if (typeAt instanceof AllFanProcessingTypes.HauntingType) {
            if (HAUNTING.getValue())
                return AllRecipeTypes.HAUNTING.getType();
        } else if (typeAt instanceof AllFanProcessingTypes.SplashingType) {
            if (SPLASHING.getValue())
                return AllRecipeTypes.SPLASHING.getType();
        }
        return null;
    }

    private void generateFor(FanProcessingType typeAt, Level level, BlockPos test, ICachableGeneratorGraph graph, Pair<MutableBoolean, MutableBoolean> furnaceReplace) {
        RecipeType<?> type = getRecipeType(typeAt);
        if (type != null) {
            if (typeAt instanceof AllFanProcessingTypes.BlastingType)
                furnaceReplace.getA().setTrue();
            if (typeAt instanceof AllFanProcessingTypes.SmokingType)
                furnaceReplace.getB().setTrue();
            if (isProcessingRecipe(type))
                addRecipeForPos(level, test, (RecipeType<ProcessingRecipe<RecipeWrapper>>) type, graph, recipe -> true);
            else {
                StorageAccessUtil.Filter posFilter = GenerateCondition.getFilterOn(level, test);
                level.getRecipeManager()
                        .getAllRecipesFor((RecipeType<Recipe<Container>>) type)
                        .forEach((recipe) -> {
                            ItemStack resultItem = recipe.getResultItem(level.registryAccess());
                            if (!posFilter.isAvailable(resultItem))
                                return;
                            graph.addRecipe(recipe, items -> {
                                List<CraftGuideStepData> steps = new ArrayList<>();
                                each3items(items, t -> steps.add(new CraftGuideStepData(
                                        new Target(ItemHandlerStorage.TYPE, test),
                                        t.stream().map(
                                                i -> i.copyWithCount(i.getCount() * COUNT.getValue())
                                        ).toList(),
                                        List.of(),
                                        CommonPlaceItemAction.TYPE,
                                        false,
                                        new CompoundTag()
                                )));
                                steps.add(new CraftGuideStepData(
                                        new Target(ItemHandlerStorage.TYPE, test),
                                        List.of(),
                                        List.of(resultItem.copyWithCount(resultItem.getCount() * COUNT.getValue())),
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

    @Override
    public Component getConfigName() {
        return Component.translatable("config.maid_storage_manager.crafting.generating.create.fan");
    }

    @Override
    public List<ConfigTypes.ConfigType<?>> getConfigurations() {
        return List.of(
                COUNT,
                SPLASHING,
                HAUNTING,
                SMOKING,
                BLASTING,
                REPLACE_FURNACE
        );
    }
}
