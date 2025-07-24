package studio.fantasyit.maid_storage_manager.craft.generator.type.ars;

import com.hollingsworth.arsnouveau.common.block.tile.ArcanePedestalTile;
import com.hollingsworth.arsnouveau.common.block.tile.ImbuementTile;
import com.hollingsworth.arsnouveau.common.crafting.recipes.ImbuementRecipe;
import com.hollingsworth.arsnouveau.setup.registry.BlockRegistry;
import com.hollingsworth.arsnouveau.setup.registry.RecipeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonPlaceItemAction;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonTakeItemAction;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.generator.algo.ICachableGeneratorGraph;
import studio.fantasyit.maid_storage_manager.craft.generator.cache.RecipeIngredientCache;
import studio.fantasyit.maid_storage_manager.craft.generator.type.base.IAutoCraftGuideGenerator;
import studio.fantasyit.maid_storage_manager.craft.type.CommonType;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;
import studio.fantasyit.maid_storage_manager.storage.ItemHandler.ItemHandlerStorage;
import studio.fantasyit.maid_storage_manager.storage.Target;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GeneratorArsNouveauImbuement implements IAutoCraftGuideGenerator {
    @Override
    public @NotNull ResourceLocation getType() {
        assert RecipeRegistry.IMBUEMENT_TYPE.getId() != null;
        return RecipeRegistry.IMBUEMENT_TYPE.getId();
    }

    @Override
    public boolean isBlockValid(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(BlockRegistry.IMBUEMENT_BLOCK.get());
    }

    @Override
    public boolean allowMultiPosition() {
        return true;
    }

    @Override
    public boolean canCacheGraph() {
        return false;
    }

    public ResourceLocation subIdGenerator(ResourceLocation id) {
        return ResourceLocation.fromNamespaceAndPath(id.getNamespace(), id.getPath() + "_sub");
    }

    @Override
    public void generate(List<InventoryItem> inventory, Level level, BlockPos pos, ICachableGeneratorGraph graph, Map<ResourceLocation, List<BlockPos>> recognizedTypePositions) {
        if (level.getBlockEntity(pos) instanceof ImbuementTile tile) {
            List<BlockPos> blockPos = tile.pedestalList(pos, 1, level);
            boolean allEmpty = blockPos.stream()
                    .map(t -> (level.getBlockEntity(t) instanceof ArcanePedestalTile p) ? p : null)
                    .filter(Objects::nonNull)
                    .allMatch(t -> t.getItem(0).isEmpty());
            int slots = blockPos.size();
            List<ImbuementRecipe> recipes = level.getRecipeManager()
                    .getAllRecipesFor(RecipeRegistry.IMBUEMENT_TYPE.get());
            List<ImbuementRecipe> matchRecipe = recipes.stream().filter(t -> t.isMatch(tile) || (slots == 0 && t.pedestalItems.isEmpty())).toList();

            if (slots > 0 && allEmpty) {
                recipes.forEach(recipe -> {
                    if (slots == recipe.pedestalItems.size()) {
                        List<Ingredient> ingredients = new ArrayList<>(recipe.pedestalItems);
                        ingredients.add(recipe.input);
                        graph.addRecipe(
                                recipe.getId(),
                                ingredients,
                                ingredients.stream().map(t -> 1).toList(),
                                recipe.output,
                                items -> {
                                    List<CraftGuideStepData> steps = new ArrayList<>();
                                    for (int i = 0; i < slots; i++) {
                                        steps.add(new CraftGuideStepData(
                                                new Target(ItemHandlerStorage.TYPE, blockPos.get(i)),
                                                List.of(items.get(i)),
                                                List.of(),
                                                CommonPlaceItemAction.TYPE,
                                                false,
                                                new CompoundTag()
                                        ));
                                    }
                                    for (int i = slots; i < items.size(); i++) {
                                        steps.add(new CraftGuideStepData(
                                                new Target(ItemHandlerStorage.TYPE, pos, Direction.UP),
                                                List.of(items.get(i)),
                                                List.of(),
                                                CommonPlaceItemAction.TYPE,
                                                false,
                                                new CompoundTag()
                                        ));
                                    }
                                    steps.add(new CraftGuideStepData(
                                            new Target(ItemHandlerStorage.TYPE, pos, Direction.DOWN),
                                            List.of(),
                                            List.of(recipe.output),
                                            CommonTakeItemAction.TYPE,
                                            false,
                                            new CompoundTag()
                                    ));
                                    for (int i = 0; i < slots; i++) {
                                        steps.add(new CraftGuideStepData(
                                                new Target(ItemHandlerStorage.TYPE, blockPos.get(i)),
                                                List.of(),
                                                List.of(items.get(i)),
                                                CommonTakeItemAction.TYPE,
                                                false,
                                                new CompoundTag()
                                        ));
                                    }
                                    return new CraftGuideData(
                                            steps,
                                            CommonType.TYPE
                                    );
                                }
                        );
                    }
                });
            } else if (!matchRecipe.isEmpty()) {

                matchRecipe.forEach(recipe -> {
                    graph.blockRecipe(recipe.getId());
                    graph.addRecipe(
                            subIdGenerator(recipe.getId()),
                            List.of(recipe.input),
                            List.of(1),
                            recipe.output,
                            items -> {
                                List<CraftGuideStepData> steps = new ArrayList<>();
                                for (int i = 0; i < items.size(); i++) {
                                    steps.add(new CraftGuideStepData(
                                            new Target(ItemHandlerStorage.TYPE, pos, Direction.UP),
                                            List.of(items.get(i)),
                                            List.of(),
                                            CommonPlaceItemAction.TYPE,
                                            false,
                                            new CompoundTag()
                                    ));
                                }
                                steps.add(new CraftGuideStepData(
                                        new Target(ItemHandlerStorage.TYPE, pos, Direction.DOWN),
                                        List.of(),
                                        List.of(recipe.output),
                                        CommonTakeItemAction.TYPE,
                                        false,
                                        new CompoundTag()
                                ));
                                return new CraftGuideData(
                                        steps,
                                        CommonType.TYPE
                                );
                            }
                    );
                });
            }


        }
    }

    @Override
    public void onCache(RecipeManager manager) {
        manager.getAllRecipesFor(RecipeRegistry.IMBUEMENT_TYPE.get())
                .forEach(t -> {
                    ResourceLocation id = t.getId();
                    ResourceLocation subId = subIdGenerator(id);

                    List<Ingredient> ingredients = new ArrayList<>(t.pedestalItems);
                    ingredients.add(t.input);
                    RecipeIngredientCache.addRecipeCache(id, ingredients);

                    RecipeIngredientCache.addRecipeCache(subId, List.of(t.input));
                });
    }

    @Override
    public Component getConfigName() {
        return Component.translatable("config.maid_storage_manager.crafting.generating.ars.imbuement");
    }
}
