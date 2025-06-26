package studio.fantasyit.maid_storage_manager.craft.generator.type.vanilla;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonAttackAction;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonIdleAction;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonUseAction;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.generator.algo.GeneratorGraph;
import studio.fantasyit.maid_storage_manager.craft.generator.cache.RecipeIngredientCache;
import studio.fantasyit.maid_storage_manager.craft.generator.type.base.IAutoCraftGuideGenerator;
import studio.fantasyit.maid_storage_manager.craft.type.CommonType;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.storage.ItemHandler.ItemHandlerStorage;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.PosUtil;
import studio.fantasyit.maid_storage_manager.util.StorageAccessUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class GeneratorStripping implements IAutoCraftGuideGenerator {
    @Override
    public @NotNull ResourceLocation getType() {
        return new ResourceLocation(MaidStorageManager.MODID, "stripping");
    }

    @Override
    public boolean isBlockValid(Level level, BlockPos pos) {
        if (level.getBlockState(pos).isCollisionShapeFullBlock(level, pos)) {
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
    public void generate(List<InventoryItem> inventory, Level level, BlockPos pos, GeneratorGraph graph, Map<ResourceLocation, List<BlockPos>> recognizedTypePositions) {
        Arrays.stream(Ingredient.of(ItemTags.LOGS)
                        .getItems())
                .forEach(itemStack -> {
                    if (itemStack.getItem() instanceof BlockItem blockItem) {
                        BlockState axeStrippingState = AxeItem.getAxeStrippingState(blockItem.getBlock().defaultBlockState());
                        if (axeStrippingState == null) return;
                        ItemStack strippedItem = axeStrippingState.getBlock().asItem().getDefaultInstance();
                        @Nullable ResourceLocation _key = ForgeRegistries.ITEMS.getKey(strippedItem.getItem());
                        if (_key == null) return;
                        ResourceLocation key = new ResourceLocation(_key.getNamespace(), _key.getPath() + "_stripping");
                        graph.addRecipe(
                                key,
                                List.of(Ingredient.of(itemStack), Ingredient.of(ItemTags.AXES)),
                                List.of(1, 1),
                                List.of(strippedItem),
                                (items) -> {
                                    List<CraftGuideStepData> craftGuideData = new ArrayList<>();
                                    craftGuideData.add(new CraftGuideStepData(
                                            new Target(ItemHandlerStorage.TYPE, pos, Direction.UP),
                                            List.of(items.get(0)),
                                            List.of(),
                                            CommonUseAction.TYPE,
                                            false,
                                            new CompoundTag()
                                    ));
                                    craftGuideData.add(new CraftGuideStepData(
                                            new Target(ItemHandlerStorage.TYPE, pos.above(), Direction.UP),
                                            List.of(items.get(1)),
                                            List.of(),
                                            CommonUseAction.TYPE,
                                            false,
                                            new CompoundTag()
                                    ));
                                    craftGuideData.add(new CraftGuideStepData(
                                            new Target(ItemHandlerStorage.TYPE, pos.above()),
                                            List.of(items.get(1)),
                                            List.of(strippedItem),
                                            CommonAttackAction.TYPE,
                                            false,
                                            new CompoundTag()
                                    ));

                                    craftGuideData.add(new CraftGuideStepData(
                                            new Target(ItemHandlerStorage.TYPE, pos, Direction.UP),
                                            List.of(),
                                            List.of(items.get(1), items.get(1)),
                                            CommonIdleAction.TYPE,
                                            false,
                                            new CompoundTag()
                                    ));
                                    return new CraftGuideData(
                                            craftGuideData,
                                            CommonType.TYPE
                                    );
                                }
                        );
                    }
                });

    }

    @Override
    public void onCache(RecipeManager manager) {
        Arrays.stream(Ingredient.of(ItemTags.LOGS)
                        .getItems())
                .forEach(itemStack -> {
                    if (itemStack.getItem() instanceof BlockItem blockItem) {
                        BlockState axeStrippingState = AxeItem.getAxeStrippingState(blockItem.getBlock().defaultBlockState());
                        if (axeStrippingState == null) return;
                        ItemStack strippedItem = axeStrippingState.getBlock().asItem().getDefaultInstance();
                        @Nullable ResourceLocation _key = ForgeRegistries.ITEMS.getKey(strippedItem.getItem());
                        if (_key == null) return;
                        ResourceLocation key = new ResourceLocation(_key.getNamespace(), _key.getPath() + "_stripping");
                        RecipeIngredientCache.addRecipeCache(
                                key,
                                List.of(Ingredient.of(itemStack), Ingredient.of(ItemTags.AXES))
                        );
                    }
                });
    }
    @Override
    public Component getConfigName() {
        return Component.translatable("config.maid_storage_manager.crafting.generating.maid_storage_manager.stripping");
    }
}