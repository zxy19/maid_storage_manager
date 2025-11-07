package studio.fantasyit.maid_storage_manager.craft.generator.type.create;

import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.mutable.MutableInt;
import studio.fantasyit.maid_storage_manager.craft.action.ActionOption;
import studio.fantasyit.maid_storage_manager.craft.action.ActionOptionSet;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonIdleAction;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonPlaceItemAction;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonTakeItemAction;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonUseAction;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.generator.algo.ICachableGeneratorGraph;
import studio.fantasyit.maid_storage_manager.craft.generator.cache.RecipeIngredientCache;
import studio.fantasyit.maid_storage_manager.craft.generator.type.base.IAutoCraftGuideGenerator;
import studio.fantasyit.maid_storage_manager.craft.generator.util.GenerateCondition;
import studio.fantasyit.maid_storage_manager.craft.generator.util.GenerateIngredientUtil;
import studio.fantasyit.maid_storage_manager.craft.generator.util.RecipeUtil;
import studio.fantasyit.maid_storage_manager.craft.type.CommonType;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;
import studio.fantasyit.maid_storage_manager.storage.ItemHandler.ItemHandlerStorage;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.MathUtil;
import studio.fantasyit.maid_storage_manager.util.StorageAccessUtil;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class GeneratorCreate<T extends ProcessingRecipe<C>, R extends RecipeType<T>, C extends Container, S> implements IAutoCraftGuideGenerator {
    protected enum StepGenerateStep {
        INIT,
        INPUT_ITEM,
        INPUT_FLUID,
        OUTPUT_ITEM,
        OUTPUT_FLUID_IDLE,
        OUTPUT_ITEM_SELECTIVE, OUTPUT_FLUID
    }

    protected static boolean isAllFluidHasBucket(List<FluidIngredient> ingredients) {
        if (ingredients.isEmpty())
            return true;
        return ingredients.stream()
                .allMatch(fluidIngredient ->
                        fluidIngredient
                                .getMatchingFluidStacks()
                                .stream()
                                .map(FluidStack::getFluid)
                                .map(Fluid::getBucket)
                                .findAny().isPresent());
    }

    protected static boolean isFluidHasBucket(List<FluidStack> fluidStack) {
        if (fluidStack.isEmpty()) return true;
        return fluidStack.stream()
                .allMatch(fs -> fs.getFluid().getBucket() != Items.AIR);
    }


    protected static Optional<List<Ingredient>> transformFluidIngredient(List<FluidIngredient> ingredients) {
        if (ingredients.isEmpty())
            return Optional.empty();
        return Optional.of(ingredients.stream().map(fluidIngredient -> {
                            ItemStack[] array = fluidIngredient
                                    .getMatchingFluidStacks()
                                    .stream()
                                    .map(FluidStack::getFluid)
                                    .map(Fluid::getBucket)
                                    .map(ItemStack::new)
                                    .toArray(ItemStack[]::new);
                            return Ingredient.of(array);
                        }
                )
                .toList());
    }

    protected static Optional<List<ItemStack>> transformFluidStacks(List<FluidStack> fluidStack, int multiplier) {
        if (fluidStack == null || fluidStack.isEmpty())
            return Optional.empty();
        return Optional.of(fluidStack.stream().map(
                t -> t.getFluid().getBucket().getDefaultInstance().copyWithCount(t.getAmount() * multiplier / 1000)
        ).toList());
    }

    protected static Optional<ItemStack> optionalItemStack(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty())
            return Optional.empty();
        return Optional.of(itemStack);
    }

    protected static Optional<List<ItemStack>> optionalItemStackList(List<ItemStack> itemStackList) {
        if (itemStackList == null || itemStackList.isEmpty())
            return Optional.empty();
        return Optional.of(itemStackList);
    }

    protected static Optional<List<Ingredient>> optionalIngredientList(List<Ingredient> ingredientList) {
        if (ingredientList == null || ingredientList.isEmpty())
            return Optional.empty();
        return Optional.of(ingredientList);
    }

    protected static void each3items(List<ItemStack> itemStackList, Consumer<List<ItemStack>> consumer) {
        GenerateIngredientUtil.each3items(itemStackList, consumer);
    }


    abstract R getRecipeType();

    protected S getState(Level level, BlockPos pos, T recipe, ICachableGeneratorGraph graph) {
        return null;
    }

    protected int getMinFullBucketCount(T recipe) {
        MutableInt minFullBucketCount = new MutableInt(1);
        recipe.getFluidIngredients().forEach(fluidIngredient -> {
            int times = MathUtil.lcm(1000, fluidIngredient.getRequiredAmount()) / fluidIngredient.getRequiredAmount();
            minFullBucketCount.setValue(MathUtil.lcm(minFullBucketCount.getValue(), times));
        });
        recipe.getFluidResults().forEach(fluidStack -> {
            int times = MathUtil.lcm(1000, fluidStack.getAmount()) / fluidStack.getAmount();
            minFullBucketCount.setValue(MathUtil.lcm(minFullBucketCount.getValue(), times));
        });
        return minFullBucketCount.getValue();
    }


    @Override
    public void generate(List<InventoryItem> inventory, Level level, BlockPos pos, ICachableGeneratorGraph graph, Map<ResourceLocation, List<BlockPos>> recognizedTypePositions) {
        addRecipeForPos(level, pos, getRecipeType(), graph, t -> true);
    }

    protected void addRecipeForPos(Level level, BlockPos pos, R type, ICachableGeneratorGraph graph, Predicate<T> predicate) {
        StorageAccessUtil.Filter posFilter = GenerateCondition.getFilterOn(level, pos);
        level.getRecipeManager()
                .getAllRecipesFor(type)
                .stream()
                .filter(predicate)
                .forEach((recipe) -> {
                    int multiplier = getMinFullBucketCount(recipe);

                    if (!isAllFluidHasBucket(recipe.getFluidIngredients()) || !isFluidHasBucket(recipe.getFluidResults())) {
                        return;
                    }

                    //所有输入原材料（流体装桶）
                    List<Ingredient> itemIngredients = optionalIngredientList(recipe.getIngredients()).orElse(List.of());
                    List<Ingredient> fluidBuckets = transformFluidIngredient(recipe.getFluidIngredients()).orElse(List.of());
                    List<Ingredient> all = new ArrayList<>();
                    all.addAll(itemIngredients);
                    all.addAll(fluidBuckets);

                    //计算结果物品序列
                    List<ItemStack> results = new ArrayList<>();
                    optionalItemStack(recipe.getResultItem(level.registryAccess()))
                            .map(i -> i.copyWithCount(i.getCount() * multiplier))
                            .ifPresent(results::add);
                    transformFluidStacks(recipe.getFluidResults(), multiplier)
                            .ifPresent(results::addAll);
                    if (results.isEmpty() || !posFilter.isAvailable(results.get(0)))
                        return;

                    //计算输入原材料数量序列
                    List<Integer> counts = new ArrayList<>();
                    itemIngredients.forEach(ingredient ->
                            counts.add(Arrays.stream(ingredient.getItems()).findFirst().map(ItemStack::getCount).orElse(0) * multiplier)
                    );
                    recipe.getFluidIngredients().forEach(ingredient -> counts.add(ingredient.getRequiredAmount() * multiplier / 1000));

                    transformAllIngredients(recipe, all, counts);

                    S state = getState(level, pos, recipe, graph);

                    graph.addRecipe(wrapId(recipe.getId()),
                            all,
                            counts,
                            results,
                            (items) -> {
                                List<CraftGuideStepData> step = new ArrayList<>();
                                //物品输入
                                optionalItemStackList(items.subList(0, itemIngredients.size()))
                                        .ifPresent(itemStackList ->
                                                each3items(itemStackList, spItems ->
                                                        step.add(new CraftGuideStepData(
                                                                        new Target(ItemHandlerStorage.TYPE, pos),
                                                                        spItems,
                                                                        List.of(),
                                                                        CommonPlaceItemAction.TYPE
                                                                )
                                                        ))
                                        );
                                transformSteps(recipe, items, state, step, StepGenerateStep.INPUT_ITEM);
                                //流体桶输入
                                optionalItemStackList(items.subList(itemIngredients.size(), itemIngredients.size() + fluidBuckets.size()))
                                        .ifPresent(bucketList ->
                                                bucketList.forEach(bucket ->
                                                        step.add(new CraftGuideStepData(
                                                                        new Target(ItemHandlerStorage.TYPE, pos, Direction.UP),
                                                                        List.of(bucket),
                                                                        List.of(Items.BUCKET.getDefaultInstance().copyWithCount(bucket.getCount())),
                                                                        CommonUseAction.TYPE
                                                                )
                                                        )
                                                )
                                        );
                                transformSteps(recipe, items, state, step, StepGenerateStep.INPUT_FLUID);
                                //其他物品输出，1chance部分
                                each3items(
                                        recipe.getRollableResults()
                                                .stream()
                                                .filter(t -> t.getChance() >= 1)
                                                .map(ProcessingOutput::getStack)
                                                .map(i -> i.copyWithCount(i.getCount() * multiplier))
                                                .toList(),
                                        (itemStacks) -> {
                                            step.add(new CraftGuideStepData(
                                                    new Target(ItemHandlerStorage.TYPE, pos),
                                                    List.of(),
                                                    itemStacks,
                                                    CommonTakeItemAction.TYPE
                                            ));
                                        });
                                transformSteps(recipe, items, state, step, StepGenerateStep.OUTPUT_ITEM);
                                //输出物品的可选部分
                                each3items(
                                        recipe.getRollableResults()
                                                .stream()
                                                .filter(t -> t.getChance() < 1)
                                                .map(ProcessingOutput::getStack)
                                                .map(i -> i.copyWithCount(i.getCount() * multiplier))
                                                .toList(),
                                        (itemStacks) -> {
                                            step.add(new CraftGuideStepData(
                                                    new Target(ItemHandlerStorage.TYPE, pos),
                                                    List.of(),
                                                    itemStacks,
                                                    CommonTakeItemAction.TYPE,
                                                    ActionOptionSet.with(ActionOption.OPTIONAL, true)
                                            ));
                                        });
                                transformSteps(recipe, items, state, step, StepGenerateStep.OUTPUT_ITEM_SELECTIVE);
                                if (recipe.getRollableResults().isEmpty()) {
                                    step.add(new CraftGuideStepData(
                                            new Target(ItemHandlerStorage.TYPE, pos),
                                            List.of(),
                                            List.of(),
                                            CommonIdleAction.TYPE,
                                            ActionOptionSet.with(CommonIdleAction.OPTION_WAIT, true, String.valueOf(100 * multiplier))
                                    ));
                                    transformSteps(recipe, items, state, step, StepGenerateStep.OUTPUT_FLUID_IDLE);
                                }
                                //流体输出
                                transformFluidStacks(recipe.getFluidResults(), multiplier)
                                        .ifPresent(outputs -> {
                                            outputs.forEach(output -> {
                                                step.add(new CraftGuideStepData(
                                                        new Target(ItemHandlerStorage.TYPE, pos, Direction.UP),
                                                        List.of(Items.BUCKET.getDefaultInstance()),
                                                        List.of(output),
                                                        CommonUseAction.TYPE
                                                ));
                                            });
                                        });
                                transformSteps(recipe, items, state, step, StepGenerateStep.OUTPUT_FLUID);
                                return new CraftGuideData(
                                        step,
                                        CommonType.TYPE
                                );
                            });
                });
    }

    private ResourceLocation wrapId(ResourceLocation id) {
        return RecipeUtil.wrapLocation(getType(), id);
    }

    public void transformAllIngredients(T recipe, List<Ingredient> all, List<Integer> counts) {
    }

    protected void transformSteps(T recipe, List<ItemStack> items, S state, List<CraftGuideStepData> step, StepGenerateStep generateStep) {
    }

    @Override
    public void onCache(RecipeManager manager) {
        manager.getAllRecipesFor(getRecipeType())
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
    }
}
