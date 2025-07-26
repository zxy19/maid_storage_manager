//package studio.fantasyit.maid_storage_manager.craft.generator.type.botania;
//
//import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
//import com.github.tartaricacid.touhoulittlemaid.entity.passive.MaidPathFindingBFS;
//import net.minecraft.core.BlockPos;
//import net.minecraft.core.Direction;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.network.chat.Component;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.server.level.ServerLevel;
//import net.minecraft.world.SimpleContainer;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.item.crafting.Ingredient;
//import net.minecraft.world.item.crafting.RecipeManager;
//import net.minecraft.world.level.Level;
//import net.minecraft.world.level.block.state.BlockState;
//import org.jetbrains.annotations.NotNull;
//import oshi.util.tuples.Pair;
//import studio.fantasyit.maid_storage_manager.craft.context.common.CommonPickupItemAction;
//import studio.fantasyit.maid_storage_manager.craft.context.common.CommonThrowItemAction;
//import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
//import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
//import studio.fantasyit.maid_storage_manager.craft.generator.algo.ICachableGeneratorGraph;
//import studio.fantasyit.maid_storage_manager.craft.generator.cache.RecipeIngredientCache;
//import studio.fantasyit.maid_storage_manager.craft.generator.config.ConfigTypes;
//import studio.fantasyit.maid_storage_manager.craft.generator.type.base.IAutoCraftGuideGenerator;
//import studio.fantasyit.maid_storage_manager.craft.type.CommonType;
//import studio.fantasyit.maid_storage_manager.data.InventoryItem;
//import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
//import studio.fantasyit.maid_storage_manager.storage.Target;
//import studio.fantasyit.maid_storage_manager.util.StorageAccessUtil;
//import vazkii.botania.api.recipe.ManaInfusionRecipe;
//import vazkii.botania.common.block.mana.ManaPoolBlock;
//import vazkii.botania.common.crafting.BotaniaRecipeTypes;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//public class GeneratorBotaniaManaInfuse implements IAutoCraftGuideGenerator {
//    ConfigTypes.ConfigType<Boolean> ACCEPT_BY_DEFAULT = new ConfigTypes.ConfigType<>(
//            "accept_by_default",
//            false,
//            Component.translatable("config.maid_storage_manager.crafting.generating.botania.mana_infuse.accept_by_default"),
//            ConfigTypes.ConfigTypeEnum.Boolean
//    );
//
//    @Override
//    public boolean positionalAvailable(ServerLevel level, EntityMaid maid, BlockPos pos, MaidPathFindingBFS pathFinding) {
//        return IAutoCraftGuideGenerator.super.positionalAvailable(level, maid, pos, pathFinding) &&
//                (ACCEPT_BY_DEFAULT.getValue() || StorageAccessUtil.getMarksForPosSet(level, Target.virtual(pos, null), List.of(pos))
//                        .stream()
//                        .map(Pair::getB)
//                        .anyMatch(t -> t.is(ItemRegistry.ALLOW_ACCESS.get())));
//    }
//
//
//    @Override
//    public @NotNull ResourceLocation getType() {
//        return ManaInfusionRecipe.TYPE_ID;
//    }
//
//    @Override
//    public Component getConfigName() {
//        return Component.translatable("config.maid_storage_manager.crafting.generating.botania.mana_infuse");
//    }
//
//
//    @Override
//    public boolean isBlockValid(Level level, BlockPos pos) {
//        return level.getBlockState(pos).getBlock() instanceof ManaPoolBlock;
//    }
//
//    @Override
//    public void generate(List<InventoryItem> inventory, Level level, BlockPos pos, ICachableGeneratorGraph graph, Map<ResourceLocation, List<BlockPos>> recognizedTypePositions) {
//        BlockState belowState = level.getBlockState(pos.below());
//        level.getRecipeManager().getAllRecipesFor(BotaniaRecipeTypes.MANA_INFUSION_TYPE)
//                .forEach(t -> {
//                    ItemStack output = t.getResultItem(level.registryAccess());
//                    List<Ingredient> ingredient = t.getIngredients();
//                    if (t.getRecipeCatalyst() == null) {
//                        //当前催化剂为空，而且有其他催化剂配方有相同输入在当前位置满足，则不添加
//                        SimpleContainer ctr = new SimpleContainer(ingredient.size());
//                        for (int i = 0; i < ingredient.size(); i++) {
//                            ctr.setItem(i, ingredient.get(i).getItems()[0]);
//                        }
//                        if (level.getRecipeManager().getRecipesFor(BotaniaRecipeTypes.MANA_INFUSION_TYPE, ctr, level)
//                                .stream().anyMatch(r -> r.getRecipeCatalyst() != null && r.getRecipeCatalyst().test(belowState))) {
//                            return;
//                        }
//                    } else if (!t.getRecipeCatalyst().test(belowState)) {
//                        return;
//                    }
//                    graph.addRecipe(t.getId(), ingredient, ingredient.stream().map(_t -> 1).toList(), output, items -> {
//                        List<CraftGuideStepData> steps = new ArrayList<>();
//                        steps.add(new CraftGuideStepData(
//                                Target.virtual(pos, Direction.UP),
//                                items,
//                                List.of(),
//                                CommonThrowItemAction.TYPE,
//                                false,
//                                new CompoundTag()
//                        ));
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
//        manager.getAllRecipesFor(BotaniaRecipeTypes.MANA_INFUSION_TYPE)
//                .forEach(RecipeIngredientCache::addRecipeCache);
//    }
//
//    @Override
//    public boolean allowMultiPosition() {
//        return true;
//    }
//
//    @Override
//    public boolean canCacheGraph() {
//        return false;
//    }
//
//    @Override
//    public List<ConfigTypes.ConfigType<?>> getConfigurations() {
//        return List.of(ACCEPT_BY_DEFAULT);
//    }
//}
