//package studio.fantasyit.maid_storage_manager.craft.generator.type.botania;
//
//import net.minecraft.core.BlockPos;
//import net.minecraft.core.Direction;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.network.chat.Component;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.item.Items;
//import net.minecraft.world.item.crafting.Ingredient;
//import net.minecraft.world.item.crafting.RecipeManager;
//import net.minecraft.world.level.Level;
//import org.jetbrains.annotations.NotNull;
//import studio.fantasyit.maid_storage_manager.craft.context.common.CommonPickupItemAction;
//import studio.fantasyit.maid_storage_manager.craft.context.common.CommonThrowItemAction;
//import studio.fantasyit.maid_storage_manager.craft.context.common.CommonUseAction;
//import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
//import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
//import studio.fantasyit.maid_storage_manager.craft.generator.algo.ICachableGeneratorGraph;
//import studio.fantasyit.maid_storage_manager.craft.generator.cache.RecipeIngredientCache;
//import studio.fantasyit.maid_storage_manager.craft.generator.type.base.IAutoCraftGuideGenerator;
//import studio.fantasyit.maid_storage_manager.craft.generator.util.GenerateIngredientUtil;
//import studio.fantasyit.maid_storage_manager.craft.type.CommonType;
//import studio.fantasyit.maid_storage_manager.data.InventoryItem;
//import studio.fantasyit.maid_storage_manager.storage.Target;
//import vazkii.botania.common.block.PetalApothecaryBlock;
//import vazkii.botania.common.crafting.BotaniaRecipeTypes;
//import vazkii.botania.common.crafting.PetalsRecipe;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//public class GeneratorBotaniaApothecary implements IAutoCraftGuideGenerator {
//    @Override
//    public @NotNull ResourceLocation getType() {
//        return PetalsRecipe.TYPE_ID;
//    }
//
//    @Override
//    public boolean isBlockValid(Level level, BlockPos pos) {
//        return level.getBlockState(pos).getBlock() instanceof PetalApothecaryBlock;
//    }
//
//    @Override
//    public void generate(List<InventoryItem> inventory, Level level, BlockPos pos, ICachableGeneratorGraph graph, Map<ResourceLocation, List<BlockPos>> recognizedTypePositions) {
//        level.getRecipeManager().getAllRecipesFor(BotaniaRecipeTypes.PETAL_TYPE)
//                .forEach(t -> {
//                    List<Ingredient> ingredients = new ArrayList<>(t.getIngredients());
//                    ingredients.add(0, Ingredient.of(Items.WATER_BUCKET));
//                    ingredients.add(t.getReagent());
//                    ItemStack output = t.getResultItem(level.registryAccess());
//                    graph.addRecipe(t.getId(), ingredients, ingredients.stream().map(_t -> 1).toList(), output, items -> {
//                        List<CraftGuideStepData> steps = new ArrayList<>();
//                        steps.add(new CraftGuideStepData(
//                                Target.virtual(pos, Direction.UP),
//                                List.of(items.get(0)),
//                                List.of(),
//                                CommonUseAction.TYPE,
//                                false,
//                                new CompoundTag()
//                        ));
//                        GenerateIngredientUtil.each3items(items.subList(1, items.size()), subItemList -> {
//                            steps.add(new CraftGuideStepData(
//                                    Target.virtual(pos, Direction.UP),
//                                    subItemList,
//                                    List.of(),
//                                    CommonThrowItemAction.TYPE,
//                                    false,
//                                    new CompoundTag()
//                            ));
//                        });
//                        steps.add(new CraftGuideStepData(
//                                Target.virtual(pos, Direction.UP),
//                                List.of(),
//                                List.of(output),
//                                CommonPickupItemAction.TYPE,
//                                false,
//                                new CompoundTag()
//                        ));
//                        return new CraftGuideData(steps, CommonType.TYPE);
//                    });
//                });
//    }
//
//    @Override
//    public void onCache(RecipeManager manager) {
//        manager.getAllRecipesFor(BotaniaRecipeTypes.PETAL_TYPE)
//                .forEach(t -> {
//                    List<Ingredient> ingredients = new ArrayList<>(t.getIngredients());
//                    ingredients.add(0, Ingredient.of(Items.WATER_BUCKET));
//                    ingredients.add(t.getReagent());
//                    RecipeIngredientCache.addRecipeCache(t.getId(), ingredients);
//                });
//    }
//
//    @Override
//    public Component getConfigName() {
//        return Component.translatable("config.maid_storage_manager.crafting.generating.botania.petal_apothecary");
//    }
//
//    @Override
//    public boolean canCacheGraph() {
//        return false;
//    }
//}
