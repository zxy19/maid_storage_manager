package studio.fantasyit.maid_storage_manager.integration.kubejs.helper;


import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.generator.algo.ICachableGeneratorGraph;
import studio.fantasyit.maid_storage_manager.craft.generator.algo.node.SpecialCraftNode;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class GraphOperator {
    public ICachableGeneratorGraph graph;

    public GraphOperator(ICachableGeneratorGraph graph) {
        this.graph = graph;
    }

    public void addRecipeObj(Recipe<?> recipe, BiFunction<ItemStack[], CraftGuideOperator, @Nullable CraftGuideData> craftGuideSupplier) {
        graph.addRecipe(recipe, t -> craftGuideSupplier.apply(t.toArray(new ItemStack[0]), CraftGuideOperator.INSTANCE));
    }

    public void addRecipeSingleOutput(ResourceLocation id, Ingredient[] ingredients, Integer[] ingredientCounts, ItemStack output, BiFunction<ItemStack[], CraftGuideOperator, @Nullable CraftGuideData> craftGuideSupplier) {
        List<Integer> counts = Arrays.stream((Object[]) ingredientCounts).map(t -> Integer.parseInt(t.toString())).toList();
        graph.addRecipe(id,
                List.of(ingredients),
                counts,
                output,
                items -> craftGuideSupplier.apply(items.toArray(new ItemStack[0]), CraftGuideOperator.INSTANCE));
    }

    public void addRecipeMultiOutput(ResourceLocation id, Ingredient[] ingredients, Integer[] ingredientCounts, ItemStack[] output, BiFunction<ItemStack[], CraftGuideOperator, @Nullable CraftGuideData> craftGuideSupplier) {
        List<Integer> counts = Arrays.stream((Object[]) ingredientCounts).map(t -> Integer.parseInt(t.toString())).toList();
        graph.addRecipe(id,
                List.of(ingredients),
                counts,
                List.of(output),
                items -> craftGuideSupplier.apply(items.toArray(new ItemStack[0]), CraftGuideOperator.INSTANCE));
    }

    public void blockRecipe(ResourceLocation id) {
        graph.blockRecipe(id);
    }

    public void removeBlockedRecipe(ResourceLocation id) {
        graph.removeBlockedRecipe(id);
    }

    public void blockType(ResourceLocation type) {
        graph.blockType(type);
    }

    public void removeBlockedType(ResourceLocation type) {
        graph.removeBlockedType(type);
    }

    public void addSpecialCraftNode(Consumer<ICachableGeneratorGraph> build, Consumer<ICachableGeneratorGraph> generate, String name) {
        graph.addSpecialCraftNode(t -> new SpecialCraftNode(t) {
            @Override
            public void buildGraph(ICachableGeneratorGraph graph) {
                build.accept(graph);
            }

            @Override
            public void generate(ICachableGeneratorGraph graph) {
                generate.accept(graph);
            }

            @Override
            public String toString() {
                return name;
            }
        });
    }
}