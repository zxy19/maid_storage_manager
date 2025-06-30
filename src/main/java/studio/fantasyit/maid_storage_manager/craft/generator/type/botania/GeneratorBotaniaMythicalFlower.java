package studio.fantasyit.maid_storage_manager.craft.generator.type.botania;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonAttackAction;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonUseAction;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.generator.algo.ICachableGeneratorGraph;
import studio.fantasyit.maid_storage_manager.craft.generator.cache.RecipeIngredientCache;
import studio.fantasyit.maid_storage_manager.craft.generator.type.base.IAutoCraftGuideGenerator;
import studio.fantasyit.maid_storage_manager.craft.type.CommonType;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.PosUtil;
import studio.fantasyit.maid_storage_manager.util.StorageAccessUtil;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.common.lib.BotaniaTags;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GeneratorBotaniaMythicalFlower implements IAutoCraftGuideGenerator {
    @Override
    public @NotNull ResourceLocation getType() {
        return new ResourceLocation(BotaniaAPI.MODID, "mythical_flower");
    }

    @Override
    public boolean isBlockValid(Level level, BlockPos pos) {
        if (level.getBlockState(pos).is(BlockTags.DIRT)) {
            if (StorageAccessUtil.getMarksForPosSet((ServerLevel) level, Target.virtual(pos, null), List.of(pos.east(), pos.west(), pos.north(), pos.south()))
                    .stream()
                    .map(Pair::getB)
                    .anyMatch(t -> t.is(ItemRegistry.ALLOW_ACCESS.get()))) {
                if (PosUtil.findInUpperSquare(pos.above(), 1, 3, 1, t -> (level.getBlockState(t).isAir() ? null : true)) == null) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void generate(List<InventoryItem> inventory, Level level, BlockPos pos, ICachableGeneratorGraph graph, Map<ResourceLocation, List<BlockPos>> recognizedTypePositions) {
        for (ItemStack flower : Ingredient.of(BotaniaTags.Items.MYSTICAL_FLOWERS).getItems()) {
            String color = Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(flower.getItem())).getPath().split("_")[0];
            List<Ingredient> ingredients = List.of(Ingredient.of(flower.getItem(),
                            ForgeRegistries.ITEMS.getValue(new ResourceLocation(BotaniaAPI.MODID, color + "_petal"))),
                    Ingredient.of(Items.BONE_MEAL)
            );
            ItemStack output = ForgeRegistries.ITEMS.getValue(new ResourceLocation(BotaniaAPI.MODID, color + "_double_flower")).getDefaultInstance().copy();
            graph.addRecipe(
                    new ResourceLocation(BotaniaAPI.MODID, "mythical_flower_plant_" + color),
                    ingredients,
                    List.of(1, 1),
                    List.of(output),
                    items -> {
                        List<CraftGuideStepData> steps = new ArrayList<>();
                        steps.add(new CraftGuideStepData(
                                Target.virtual(pos, Direction.UP),
                                List.of(items.get(0)),
                                List.of(),
                                CommonUseAction.TYPE,
                                false,
                                new CompoundTag()
                        ));
                        steps.add(new CraftGuideStepData(
                                Target.virtual(pos.above(), null),
                                List.of(items.get(1)),
                                List.of(),
                                CommonUseAction.TYPE,
                                false,
                                new CompoundTag()
                        ));
                        steps.add(new CraftGuideStepData(
                                Target.virtual(pos.above(), null),
                                List.of(),
                                List.of(output),
                                CommonAttackAction.TYPE,
                                false,
                                new CompoundTag()
                        ));
                        return new CraftGuideData(steps, CommonType.TYPE);
                    }
            );
        }
    }

    @Override
    public void onCache(RecipeManager manager) {
        for (ItemStack flower : Ingredient.of(BotaniaTags.Items.MYSTICAL_FLOWERS).getItems()) {
            String color = Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(flower.getItem())).getPath().split("_")[0];
            RecipeIngredientCache.addRecipeCache(
                    new ResourceLocation(BotaniaAPI.MODID, "mythical_flower_plant_" + color),
                    List.of(Ingredient.of(flower.getItem(),
                                    ForgeRegistries.ITEMS.getValue(new ResourceLocation(BotaniaAPI.MODID, color + "_petal"))),
                            Ingredient.of(Items.BONE_MEAL)
                    )
            );
        }
    }

    @Override
    public Component getConfigName() {
        return Component.translatable("config.maid_storage_manager.crafting.generating.botania.mythrical_flower");
    }
}