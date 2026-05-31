package studio.fantasyit.maid_storage_manager.craft.generator.algo;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.generator.algo.node.IngredientNode;
import studio.fantasyit.maid_storage_manager.craft.generator.algo.node.ItemNode;
import studio.fantasyit.maid_storage_manager.craft.generator.algo.node.Node;
import studio.fantasyit.maid_storage_manager.craft.generator.algo.node.SpecialCraftNode;
import studio.fantasyit.maid_storage_manager.craft.generator.type.base.IAutoCraftGuideGenerator;
import studio.fantasyit.maid_storage_manager.craft.generator.util.GenerateIngredientUtil;
import studio.fantasyit.maid_storage_manager.craft.generator.util.RecipeUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public class DummyCollector implements ICachableGeneratorGraph {
    private final RegistryAccess registryAccess;
    public List<List<Ingredient>> ingredients;
    public List<List<Integer>> counts;
    public List<List<ItemStack>> outputs;
    public List<Function<List<ItemStack>, @Nullable CraftGuideData>> craftGuideSuppliers;

    public DummyCollector(RegistryAccess registryAccess) {
        this.craftGuideSuppliers = new ArrayList<>();
        this.ingredients = new ArrayList<>();
        this.counts = new ArrayList<>();
        this.outputs = new ArrayList<>();
        this.registryAccess = registryAccess;
    }

    public void syncFromServer(List<List<Ingredient>> ingredients, List<List<ItemStack>> outputs) {
        this.ingredients = ingredients;
        this.outputs = outputs;
    }

    public void clear() {
        this.ingredients.clear();
        this.outputs.clear();
        this.counts.clear();
        this.craftGuideSuppliers.clear();
    }

    @Override
    public void setItems(List<ItemStack> list, List<ItemStack> itemList) {
    }

    @Override
    public void setCurrentGeneratorType(Identifier internalType, boolean b) {

    }

    @Override
    public void setCurrentGeneratorType(IAutoCraftGuideGenerator generator) {

    }

    @Override
    public void addRecipe(RecipeHolder<? extends Recipe<?>> recipe, Function<List<ItemStack>, @Nullable CraftGuideData> craftGuideSupplier) {
        List<Ingredient> ingredients = recipe.value().placementInfo().ingredients();
        List<Integer> ingredientCounts = ingredients.stream().map(GenerateIngredientUtil::getIngredientCount).toList();
        addRecipe(
                recipe.id().identifier(),
                ingredients,
                ingredientCounts,
                ItemStack.EMPTY,
                craftGuideSupplier
        );
    }

    @Override
    public void addRecipeWrapId(RecipeHolder<? extends Recipe<?>> recipe, Identifier generator, Function<List<ItemStack>, @Nullable CraftGuideData> craftGuideSupplier) {
        List<Ingredient> ingredients = recipe.value().placementInfo().ingredients();
        List<Integer> ingredientCounts = ingredients
                .stream()
                .map(GenerateIngredientUtil::getIngredientCount)
                .toList();
        addRecipe(
                RecipeUtil.wrapLocation(generator, recipe.id().identifier()),
                ingredients,
                ingredientCounts,
                ItemStack.EMPTY,
                craftGuideSupplier
        );
    }

    @Override
    public void addRecipe(Identifier id, List<Ingredient> ingredients, List<Integer> ingredientCounts, ItemStack output, Function<List<ItemStack>, @Nullable CraftGuideData> craftGuideSupplier) {
        addRecipe(id, ingredients, ingredientCounts, List.of(output), craftGuideSupplier);
    }

    @Override
    public void addRecipe(Identifier id, List<Ingredient> ingredients, List<Integer> ingredientCounts, List<ItemStack> output, Function<List<ItemStack>, @Nullable CraftGuideData> craftGuideSupplier) {
        this.craftGuideSuppliers.add(craftGuideSupplier);
        this.counts.add(ingredientCounts);
        this.ingredients.add(ingredients);
        this.outputs.add(output);
    }

    @Override
    public void addSpecialCraftNode(Function<Integer, SpecialCraftNode> idToNodeBuilder) {
    }

    @Override
    public void clearStates() {
    }

    @Override
    public void invalidAllCraftWithType(@NotNull Identifier type) {
    }

    @Override
    public void blockType(Identifier type) {
    }

    @Override
    public void blockRecipe(Identifier id) {

    }

    @Override
    public void removeBlockedRecipe(Identifier id) {

    }

    @Override
    public void removeBlockedType(Identifier type) {

    }

    @Override
    public List<CraftGuideData> getCraftGuides() {
        return List.of();
    }

    @Override
    public int getProcessedSteps() {
        return 0;
    }

    @Override
    public int getPushedSteps() {
        return 0;
    }

    @Override
    public boolean process() {
        return false;
    }

    @Override
    public IngredientNode addOrGetCahcedIngredientNode(Ingredient ingredient, UUID uuid) {
        return null;
    }

    @Override
    public void addRecipeWithIngredients(Identifier id, List<Ingredient> ingredients, List<Integer> ingredientCounts, List<ItemStack> output, List<IngredientNode> ingredientNodes, Function<List<ItemStack>, CraftGuideData> craftGuideSupplier, Identifier type, boolean isOneTime) {

    }

    @Override
    public boolean hasCachedIngredientNode(UUID ingredient) {
        return false;
    }

    @Override
    public Node getNode(int a) {
        return null;
    }

    @Override
    public List<Node> getNodes() {
        return List.of();
    }

    @Override
    public ItemNode getItemNodeOrCreate(ItemStack itemStack, boolean defaultAvailable) {
        return null;
    }

    @Override
    public void addToQueue(Node node) {

    }

    @Override
    public void addCraftGuide(CraftGuideData craftGuideData) {

    }
}
