package studio.fantasyit.maid_storage_manager.craft.generator.algo;

import net.minecraft.resources.ResourceLocation;
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

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public interface ICachableGeneratorGraph {
    void setItems(List<ItemStack> list, List<ItemStack> itemList);

    void setCurrentGeneratorType(ResourceLocation internalType, boolean b);

    void setCurrentGeneratorType(IAutoCraftGuideGenerator generator);

    void addRecipe(RecipeHolder<? extends Recipe<?>> recipe, Function<List<ItemStack>, @Nullable CraftGuideData> craftGuideSupplier);

    void addRecipeWrapId(RecipeHolder<? extends Recipe<?>> recipe, ResourceLocation generator, Function<List<ItemStack>, @Nullable CraftGuideData> craftGuideSupplier);

    void addRecipe(ResourceLocation id, List<Ingredient> ingredients, List<Integer> ingredientCounts, ItemStack output, Function<List<ItemStack>, @Nullable CraftGuideData> craftGuideSupplier);

    void addRecipe(ResourceLocation id, List<Ingredient> ingredients, List<Integer> ingredientCounts, List<ItemStack> output, Function<List<ItemStack>, @Nullable CraftGuideData> craftGuideSupplier);

    void addSpecialCraftNode(Function<Integer, SpecialCraftNode> idToNodeBuilder);

    void clearStates();

    void invalidAllCraftWithType(@NotNull ResourceLocation type);

    void blockType(ResourceLocation type);

    void blockRecipe(ResourceLocation id);

    void removeBlockedRecipe(ResourceLocation id);

    void removeBlockedType(ResourceLocation type);

    List<CraftGuideData> getCraftGuides();

    int getProcessedSteps();

    int getPushedSteps();

    boolean process();

    IngredientNode addOrGetCahcedIngredientNode(Ingredient ingredient, UUID uuid);

    void addRecipeWithIngredients(ResourceLocation id, List<Ingredient> ingredients, List<Integer> ingredientCounts, List<ItemStack> output, List<IngredientNode> ingredientNodes, Function<List<ItemStack>, CraftGuideData> craftGuideSupplier, ResourceLocation type, boolean isOneTime);

    boolean hasCachedIngredientNode(UUID ingredient);

    Node getNode(int a);

    List<Node> getNodes();

    ItemNode getItemNodeOrCreate(ItemStack itemStack, boolean defaultAvailable);

    void addToQueue(Node node);

    void addCraftGuide(CraftGuideData craftGuideData);
}
