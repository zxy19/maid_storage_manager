package studio.fantasyit.maid_storage_manager.craft.generator.type.botania;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.MaidPathFindingBFS;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.craft.action.ActionOptionSet;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonAttackAction;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonIdleAction;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonUseAction;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.generator.algo.ICachableGeneratorGraph;
import studio.fantasyit.maid_storage_manager.craft.generator.cache.RecipeIngredientCache;
import studio.fantasyit.maid_storage_manager.craft.generator.type.base.IAutoCraftGuideGenerator;
import studio.fantasyit.maid_storage_manager.craft.generator.util.GenerateIngredientUtil;
import studio.fantasyit.maid_storage_manager.craft.type.CommonType;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;
import studio.fantasyit.maid_storage_manager.storage.Target;
import vazkii.botania.api.recipe.PureDaisyRecipe;
import vazkii.botania.common.block.BotaniaFlowerBlocks;
import vazkii.botania.common.crafting.BotaniaRecipeTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GeneratorBotaniaDaisy implements IAutoCraftGuideGenerator {
    protected static final Vec3i[] offsets = new Vec3i[]{
            new Vec3i(1, 0, 0),
            new Vec3i(-1, 0, 0),
            new Vec3i(0, 0, 1),
            new Vec3i(0, 0, -1),
            new Vec3i(1, 0, 1),
            new Vec3i(-1, 0, 1),
            new Vec3i(1, 0, -1),
            new Vec3i(-1, 0, -1)
    };

    @Override
    public @NotNull ResourceLocation getType() {
        return PureDaisyRecipe.TYPE_ID;
    }

    @Override
    public boolean isBlockValid(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(BotaniaFlowerBlocks.pureDaisy);
    }

    @Override
    public boolean positionalAvailable(ServerLevel level, EntityMaid maid, BlockPos pos, MaidPathFindingBFS pathFinding) {
        if (IAutoCraftGuideGenerator.super.positionalAvailable(level, maid, pos, pathFinding)) {
            for (Vec3i offsetPos : offsets) {
                if (!level.getBlockState(pos.offset(offsetPos)).isAir())
                    return false;
                if (!level.getBlockState(pos.offset(offsetPos).below()).isCollisionShapeFullBlock(level, pos.offset(offsetPos).below()))
                    return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public void generate(List<InventoryItem> inventory, Level level, BlockPos pos, ICachableGeneratorGraph graph, Map<ResourceLocation, List<BlockPos>> recognizedTypePositions) {
        level.getRecipeManager()
                .getAllRecipesFor(BotaniaRecipeTypes.PURE_DAISY_TYPE)
                .forEach(recipe -> {
                    ArrayList<Ingredient> ingredients = new ArrayList<>(List.of(Ingredient.of(recipe.getInput().getDisplayedStacks().stream())));
                    ArrayList<Integer> counts = new ArrayList<>(List.of(8));
                    ItemStack output = recipe.getOutputState().getBlock().asItem().getDefaultInstance();
                    Optional<Ingredient> toolOptional = GenerateIngredientUtil.optionalIngredient(
                            GenerateIngredientUtil.getIngredientForDestroyBlockItem(output)
                    );
                    toolOptional.ifPresent(ingredients::add);
                    toolOptional.ifPresent(t -> counts.add(2));
                    graph.addRecipe(
                            recipe.getId(),
                            ingredients,
                            counts,
                            output,
                            items -> {
                                List<CraftGuideStepData> steps = new ArrayList<>();
                                for (Vec3i offset : offsets) {
                                    steps.add(new CraftGuideStepData(
                                            Target.virtual(pos.offset(offset).below(), Direction.UP),
                                            List.of(items.get(0).copyWithCount(1)),
                                            List.of(),
                                            CommonUseAction.TYPE
                                    ));
                                }
                                steps.add(new CraftGuideStepData(
                                        Target.virtual(pos.above(), null),
                                        List.of(),
                                        List.of(),
                                        CommonIdleAction.TYPE,
                                        ActionOptionSet.with(CommonIdleAction.OPTION_WAIT, false, String.valueOf(recipe.getTime() * 8))
                                ));
                                int i = 0;
                                for (Vec3i offset : offsets) {
                                    steps.add(new CraftGuideStepData(
                                            Target.virtual(pos.offset(offset), null),
                                            List.of(items.get(1).copyWithCount(1)),
                                            List.of(output),
                                            CommonAttackAction.TYPE
                                    ));
                                    if ((++i) % 2 == 0)
                                        steps.add(new CraftGuideStepData(
                                                Target.virtual(pos.offset(offset).below(), Direction.UP),
                                                List.of(),
                                                List.of(items.get(1).copyWithCount(2)),
                                                CommonIdleAction.TYPE
                                        ));
                                }
                                return new CraftGuideData(steps, CommonType.TYPE);
                            });
                });
    }

    @Override
    public void onCache(RecipeManager manager) {
        manager.getAllRecipesFor(BotaniaRecipeTypes.PURE_DAISY_TYPE)
                .forEach(recipe -> {
                    ArrayList<Ingredient> ingredients = new ArrayList<>(List.of(Ingredient.of(recipe.getInput().getDisplayedStacks().stream())));
                    ItemStack output = recipe.getOutputState().getBlock().asItem().getDefaultInstance();
                    Optional<Ingredient> toolOptional = GenerateIngredientUtil.optionalIngredient(
                            GenerateIngredientUtil.getIngredientForDestroyBlockItem(output)
                    );
                    toolOptional.ifPresent(ingredients::add);
                    RecipeIngredientCache.addRecipeCache(
                            recipe.getId(),
                            ingredients
                    );
                });
    }

    @Override
    public Component getConfigName() {
        return Component.translatable("config.maid_storage_manager.crafting.generating.botania.daisy");
    }
}
