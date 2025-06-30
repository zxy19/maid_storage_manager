package studio.fantasyit.maid_storage_manager.craft.generator.type.botania;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.MaidPathFindingBFS;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonIdleAction;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonPickupItemAction;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonUseAction;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.generator.algo.ICachableGeneratorGraph;
import studio.fantasyit.maid_storage_manager.craft.generator.cache.RecipeIngredientCache;
import studio.fantasyit.maid_storage_manager.craft.generator.config.ConfigTypes;
import studio.fantasyit.maid_storage_manager.craft.generator.type.base.IAutoCraftGuideGenerator;
import studio.fantasyit.maid_storage_manager.craft.type.CommonType;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;
import studio.fantasyit.maid_storage_manager.storage.Target;
import vazkii.botania.api.recipe.RunicAltarRecipe;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.block.block_entity.mana.ManaSpreaderBlockEntity;
import vazkii.botania.common.crafting.BotaniaRecipeTypes;
import vazkii.botania.common.item.BotaniaItems;

import java.util.*;

public class GeneratorBotaniaRunicAltar implements IAutoCraftGuideGenerator {
    ConfigTypes.ConfigType<Double> WAIT_TIME_SCALE = new ConfigTypes.ConfigType<>(
            "wait_time_scale",
            1.0,
            Component.translatable("config.maid_storage_manager.crafting.generating.botania.runic_altar.wait_time"),
            ConfigTypes.ConfigTypeEnum.Double
    );

    @Override
    public @NotNull ResourceLocation getType() {
        return RunicAltarRecipe.TYPE_ID;
    }

    @Override
    public boolean isBlockValid(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(BotaniaBlocks.runeAltar);
    }

    @Override
    public boolean positionalAvailable(ServerLevel level, EntityMaid maid, BlockPos pos, MaidPathFindingBFS pathFinding) {
        return IAutoCraftGuideGenerator.super.positionalAvailable(level, maid, pos, pathFinding) && getSpreaderSpeed(level, pos) > 0;
    }

    protected double getSpreaderSpeed(Level level, BlockPos pos) {
        return BlockPos.betweenClosedStream(pos.offset(-3, -3, -3), pos.offset(3, 3, 3))
                .map(t -> (level.getBlockEntity(t) instanceof ManaSpreaderBlockEntity msbe) ? msbe : null)
                .filter(Objects::nonNull)
                .filter(be -> be.getBinding() != null && be.getBinding().equals(pos))
                .map(be -> {
                    double distance = Math.sqrt(be.getBlockPos().distSqr(pos)) - 0.5;
                    int flyTicks = (int) Math.ceil(distance / 0.2);
                    return (be.getVariant().burstMana - (flyTicks + 1) * be.getVariant().lossPerTick) / Math.min(flyTicks, 20);
                })
                .max(Comparator.comparingDouble(t -> t))
                .orElse(0.0f);
    }

    @Override
    public void generate(List<InventoryItem> inventory, Level level, BlockPos pos, ICachableGeneratorGraph graph, Map<ResourceLocation, List<BlockPos>> recognizedTypePositions) {
        double speed = getSpreaderSpeed(level, pos);

        level.getRecipeManager()
                .getAllRecipesFor(BotaniaRecipeTypes.RUNE_TYPE)
                .forEach(recipe -> {
                    ArrayList<Ingredient> list = new ArrayList<>(recipe.getIngredients());
                    list.add(recipe.getReagent());
                    list.add(Ingredient.of(BotaniaItems.twigWand));
                    ItemStack output = recipe.getResultItem(level.registryAccess());
                    graph.addRecipe(
                            recipe.getId(),
                            list,
                            list.stream().map(t -> 1).toList(),
                            output,
                            items -> {
                                List<CraftGuideStepData> steps = new ArrayList<>();
                                for (int i = 0; i < items.size() - 2; i++) {
                                    ItemStack t = items.get(i);
                                    steps.add(new CraftGuideStepData(
                                            Target.virtual(pos, Direction.UP),
                                            List.of(t),
                                            List.of(),
                                            CommonUseAction.TYPE,
                                            false,
                                            new CompoundTag()
                                    ));
                                }

                                CompoundTag compoundTag = new CompoundTag();
                                compoundTag.putInt("time", (int) ((double) recipe.getManaUsage() / speed * WAIT_TIME_SCALE.getValue()));
                                compoundTag.putInt("u", 0);
                                steps.add(new CraftGuideStepData(
                                        Target.virtual(pos, Direction.UP),
                                        List.of(),
                                        List.of(),
                                        CommonIdleAction.TYPE,
                                        false,
                                        compoundTag
                                ));
                                ItemStack react = items.get(items.size() - 2);
                                steps.add(new CraftGuideStepData(
                                        Target.virtual(pos, Direction.UP),
                                        List.of(react),
                                        List.of(),
                                        CommonUseAction.TYPE,
                                        false,
                                        new CompoundTag()
                                ));
                                ItemStack wand = items.get(items.size() - 1);
                                steps.add(new CraftGuideStepData(
                                        Target.virtual(pos, Direction.UP),
                                        List.of(wand),
                                        List.of(wand),
                                        CommonUseAction.TYPE,
                                        false,
                                        new CompoundTag()
                                ));

                                steps.add(new CraftGuideStepData(
                                        Target.virtual(pos, Direction.UP),
                                        List.of(),
                                        List.of(output),
                                        CommonPickupItemAction.TYPE,
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

    @Override
    public void onCache(RecipeManager manager) {
        manager.getAllRecipesFor(BotaniaRecipeTypes.RUNE_TYPE)
                .forEach(t -> {
                    ArrayList<Ingredient> list = new ArrayList<>(t.getIngredients());
                    list.add(t.getReagent());
                    list.add(Ingredient.of(BotaniaItems.twigWand));
                    RecipeIngredientCache.addRecipeCache(t.getId(), list);
                });
    }

    @Override
    public Component getConfigName() {
        return Component.translatable("config.maid_storage_manager.crafting.generating.botania.runic_altar");
    }

    @Override
    public boolean canCacheGraph() {
        return false;
    }
}
