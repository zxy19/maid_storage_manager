package studio.fantasyit.maid_storage_manager.craft.generator.type.vanilla;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.brewing.BrewingRecipe;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.craft.WorkBlockTags;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.generator.algo.ICachableGeneratorGraph;
import studio.fantasyit.maid_storage_manager.craft.generator.cache.RecipeIngredientCache;
import studio.fantasyit.maid_storage_manager.craft.generator.config.ConfigTypes;
import studio.fantasyit.maid_storage_manager.craft.generator.type.base.IAutoCraftGuideGenerator;
import studio.fantasyit.maid_storage_manager.craft.generator.util.GenerateCondition;
import studio.fantasyit.maid_storage_manager.craft.type.BrewingType;
import studio.fantasyit.maid_storage_manager.craft.type.CraftingType;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.StorageAccessUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class GeneratorBrewing implements IAutoCraftGuideGenerator {
    protected record BrewingData(int index, Ingredient input, Ingredient ingredient, ItemStack output) {
    }

    ConfigTypes.ConfigType<Integer> COUNT = new ConfigTypes.ConfigType<>(
            "count",
            3,
            Component.translatable("config.maid_storage_manager.crafting.generating.maid_storage_manager.brewing.count"),
            ConfigTypes.ConfigTypeEnum.Integer
    );
    List<BrewingData> brewingData = null;

    @Override
    public @NotNull ResourceLocation getType() {
        return BrewingType.TYPE;
    }

    @Override
    public boolean isBlockValid(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(WorkBlockTags.BREWING_STAND);
    }

    @Override
    public void generate(List<InventoryItem> inventory, Level level, BlockPos pos, ICachableGeneratorGraph graph, Map<ResourceLocation, List<BlockPos>> recognizedTypePositions) {
        StorageAccessUtil.Filter posFilter = GenerateCondition.getFilterOn(level, pos);

        if (brewingData == null) {
            brewingData = new ArrayList<>();
            forEachRecipeIO(level, (data) -> {
                RecipeIngredientCache.addRecipeCache(
                        ResourceLocation.fromNamespaceAndPath("brewing", String.format("recipe_%d", data.index)),
                        List.of(Ingredient.of(Items.BLAZE_POWDER.getDefaultInstance()), data.input, data.ingredient)
                );
                brewingData.add(data);
            });
        }
        brewingData.forEach(data -> {
            if (!posFilter.isAvailable(data.output))
                return;
            graph.addRecipe(ResourceLocation.fromNamespaceAndPath("brewing", String.format("recipe_%d", data.index)),
                    List.of(Ingredient.of(Items.BLAZE_POWDER.getDefaultInstance()), data.input, data.ingredient),
                    List.of(1, COUNT.getValue(), 1),
                    data.output,
                    (items) -> {
                        CraftGuideStepData step = new CraftGuideStepData(
                                new Target(CraftingType.TYPE, pos),
                                items,
                                List.of(data.output),
                                BrewingType.TYPE
                        );
                        return new CraftGuideData(
                                List.of(step),
                                BrewingType.TYPE
                        );
                    });
        });
    }

    @Override
    public void onCache(RecipeManager manager) {
        brewingData = null;
    }


    protected void forEachRecipeIO(Level level, Consumer<BrewingData> io) {
        MutableInt index = new MutableInt();
        PotionBrewing potionBrewing = level.potionBrewing();
        List<PotionBrewing.Mix<Potion>> potionMixes = level.potionBrewing().potionMixes;
        potionMixes.forEach(potionMix -> {
            HashMap<Item, ItemStack> container2ItemStack = new HashMap<>();
            for (PotionBrewing.Mix<Item> containerMix : potionBrewing.containerMixes) {
                Item container1 = containerMix.from().value();
                ItemStack from1 = PotionContents.createItemStack(container1, potionMix.from());
                ItemStack to1 = PotionContents.createItemStack(container1, potionMix.to());
                if (!container2ItemStack.containsKey(container1)) {
                    io.accept(new BrewingData(
                            index.getAndIncrement(),
                            Ingredient.of(from1),
                            potionMix.ingredient(),
                            to1.copyWithCount(COUNT.getValue())
                    ));
                    container2ItemStack.put(container1, to1);
                }

                Item container2 = containerMix.to().value();
                ItemStack from2 = PotionContents.createItemStack(container2, potionMix.from());
                ItemStack to2 = PotionContents.createItemStack(container2, potionMix.to());
                if (!container2ItemStack.containsKey(container2)) {
                    io.accept(new BrewingData(
                            index.getAndIncrement(),
                            Ingredient.of(from2),
                            potionMix.ingredient(),
                            to2.copyWithCount(COUNT.getValue())
                    ));
                    container2ItemStack.put(container2, to2);
                }
            }
            for (PotionBrewing.Mix<Item> containerMix : potionBrewing.containerMixes) {
                ItemStack[] ingredients = containerMix.ingredient().getItems();
                if (ingredients.length == 0) continue;
                if (container2ItemStack.containsKey(containerMix.from().value()) && !container2ItemStack.containsKey(containerMix.to().value())) {
                    ItemStack t1 = container2ItemStack.get(containerMix.from().value()).copy();
                    ItemStack t2 = potionBrewing.mix(ingredients[0], t1);
                    if (t2 != t1) {
                        container2ItemStack.put(containerMix.to().value(), t2);
                        io.accept(new BrewingData(index.getAndIncrement(), Ingredient.of(t1), containerMix.ingredient(), t2.copyWithCount(COUNT.getValue())));
                    }
                }
            }
        });


        potionBrewing.getRecipes()
                .forEach(recipe -> {
                    if (recipe instanceof BrewingRecipe br) {
                        io.accept(new BrewingData(index.getAndIncrement(), br.getInput(), br.getIngredient(), br.getOutput().copyWithCount(COUNT.getValue())));
                    }
                });
    }

    @Override
    public Component getConfigName() {
        return Component.translatable("config.maid_storage_manager.crafting.generating.maid_storage_manager.brewing");
    }

    @Override
    public List<ConfigTypes.ConfigType<?>> getConfigurations() {
        return List.of(COUNT);
    }
}
