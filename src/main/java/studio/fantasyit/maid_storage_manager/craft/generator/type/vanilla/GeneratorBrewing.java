package studio.fantasyit.maid_storage_manager.craft.generator.type.vanilla;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.brewing.BrewingRecipe;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import org.apache.commons.lang3.mutable.MutableInt;
import studio.fantasyit.maid_storage_manager.craft.WorkBlockTags;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.generator.algo.GeneratorGraph;
import studio.fantasyit.maid_storage_manager.craft.generator.cache.RecipeIngredientCache;
import studio.fantasyit.maid_storage_manager.craft.generator.type.base.IAutoCraftGuideGenerator;
import studio.fantasyit.maid_storage_manager.craft.type.BrewingType;
import studio.fantasyit.maid_storage_manager.craft.type.CraftingType;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;
import studio.fantasyit.maid_storage_manager.util.RecipeUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class GeneratorBrewing implements IAutoCraftGuideGenerator {
    protected record BrewingData(int index, Ingredient input, Ingredient ingredient, ItemStack output) {
    }

    List<BrewingData> brewingData = new ArrayList<>();

    @Override
    public ResourceLocation getType() {
        return BrewingType.TYPE;
    }

    @Override
    public boolean isBlockValid(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(WorkBlockTags.BREWING_STAND);
    }

    @Override
    public void generate(List<InventoryItem> inventory, Level level, BlockPos pos, GeneratorGraph graph) {
        brewingData.forEach(data -> {
            graph.addRecipe(new ResourceLocation("brewing", String.format("recipe_%d", data.index)),
                    List.of(Ingredient.of(Items.BLAZE_POWDER.getDefaultInstance()), data.input, data.ingredient),
                    List.of(1, 3, 1),
                    data.output,
                    (items) -> {
                        Optional<IBrewingRecipe> brewingRecipe = RecipeUtil.getBrewingRecipe(level, items.get(1), items.get(2));
                        if (brewingRecipe.isEmpty())
                            return null;
                        ItemStack output = brewingRecipe.get().getOutput(items.get(1).copy(), items.get(2).copy())
                                .copyWithCount(items.get(1).getCount());
                        if (!ItemStackUtil.isSameInCrafting(output, data.output))
                            return null;
                        CraftGuideStepData step = new CraftGuideStepData(
                                new Target(CraftingType.TYPE, pos),
                                items,
                                List.of(output),
                                BrewingType.TYPE,
                                false,
                                new CompoundTag()
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
        brewingData.clear();
        forEachRecipeIO((data) -> {
            RecipeIngredientCache.addRecipeCache(
                    new ResourceLocation("brewing", String.format("recipe_%d", data.index)),
                    List.of(Ingredient.of(Items.BLAZE_POWDER.getDefaultInstance()), data.input, data.ingredient)
            );
            brewingData.add(data);
        });
    }


    protected static void forEachRecipeIO(Consumer<BrewingData> io) {
        MutableInt index = new MutableInt();
        PotionBrewing.POTION_MIXES.forEach(potionMix -> {
            HashMap<Item, ItemStack> container2ItemStack = new HashMap<>();
            for (PotionBrewing.Mix<Item> containerMix : PotionBrewing.CONTAINER_MIXES) {
                Item container1 = containerMix.from.get();
                ItemStack from1 = PotionUtils.setPotion(new ItemStack(container1), potionMix.from.get());
                ItemStack to1 = PotionUtils.setPotion(new ItemStack(container1), potionMix.to.get());
                if (!container2ItemStack.containsKey(container1)) {
                    io.accept(new BrewingData(
                            index.getAndIncrement(),
                            Ingredient.of(from1),
                            potionMix.ingredient,
                            to1.copyWithCount(3)
                    ));
                    container2ItemStack.put(container1, to1);
                }

                Item container2 = containerMix.to.get();
                ItemStack from2 = PotionUtils.setPotion(new ItemStack(container2), potionMix.from.get());
                ItemStack to2 = PotionUtils.setPotion(new ItemStack(container2), potionMix.to.get());
                if (!container2ItemStack.containsKey(container2)) {
                    io.accept(new BrewingData(
                            index.getAndIncrement(),
                            Ingredient.of(from2),
                            potionMix.ingredient,
                            to2.copyWithCount(3)
                    ));
                    container2ItemStack.put(container2, to2);
                }
            }
            for (PotionBrewing.Mix<Item> containerMix : PotionBrewing.CONTAINER_MIXES) {
                ItemStack[] ingredients = containerMix.ingredient.getItems();
                if (ingredients.length == 0) continue;
                if (container2ItemStack.containsKey(containerMix.from.get()) && !container2ItemStack.containsKey(containerMix.to.get())) {
                    ItemStack t1 = container2ItemStack.get(containerMix.from.get()).copy();
                    ItemStack t2 = PotionBrewing.mix(ingredients[0], t1);
                    if (t2 != t1) {
                        container2ItemStack.put(containerMix.to.get(), t2);
                        io.accept(new BrewingData(index.getAndIncrement(), Ingredient.of(t1), containerMix.ingredient, t2.copyWithCount(3)));
                    }
                }
            }
        });


        BrewingRecipeRegistry
                .getRecipes()
                .forEach(recipe -> {
                    if (recipe instanceof BrewingRecipe br) {
                        io.accept(new BrewingData(index.getAndIncrement(), br.getInput(), br.getIngredient(), br.getOutput().copyWithCount(3)));
                    }
                });
    }
}
