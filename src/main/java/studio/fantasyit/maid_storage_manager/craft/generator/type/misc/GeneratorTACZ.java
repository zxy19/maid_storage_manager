package studio.fantasyit.maid_storage_manager.craft.generator.type.misc;

import com.tacz.guns.block.entity.GunSmithTableBlockEntity;
import com.tacz.guns.crafting.GunSmithTableIngredient;
import com.tacz.guns.crafting.GunSmithTableRecipe;
import com.tacz.guns.init.ModRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.generator.algo.GeneratorGraph;
import studio.fantasyit.maid_storage_manager.craft.generator.cache.RecipeIngredientCache;
import studio.fantasyit.maid_storage_manager.craft.generator.type.base.IAutoCraftGuideGenerator;
import studio.fantasyit.maid_storage_manager.craft.generator.util.GenerateCondition;
import studio.fantasyit.maid_storage_manager.craft.type.CraftingType;
import studio.fantasyit.maid_storage_manager.craft.type.TaczType;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;
import studio.fantasyit.maid_storage_manager.integration.tacz.TaczRecipe;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.StorageAccessUtil;

import java.util.List;
import java.util.Map;

public class GeneratorTACZ implements IAutoCraftGuideGenerator {
    @Override
    public ResourceLocation getType() {
        return TaczType.TYPE;
    }

    @Override
    public boolean isBlockValid(Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof GunSmithTableBlockEntity;
    }

    @Override
    public boolean allowMultiPosition() {
        return true;
    }

    @Override
    public void generate(List<InventoryItem> inventory, Level level, BlockPos pos, GeneratorGraph graph, Map<ResourceLocation, List<BlockPos>> recognizedTypePositions) {
        StorageAccessUtil.Filter posFilter = GenerateCondition.getFilterOn(level, pos);
        ResourceLocation blockId = TaczRecipe.getBlockId(level, pos);
        List<GunSmithTableRecipe> allRecipesForBlockId = TaczRecipe.getAllRecipesForBlockId(level, blockId);
        allRecipesForBlockId.forEach(recipe -> {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putString("block_id", blockId.toString());
            compoundTag.putString("recipe_id", recipe.getId().toString());
            List<GunSmithTableIngredient> ingredients = recipe.getInputs();
            if (!posFilter.isAvailable(recipe.getOutput()))
                return;
            graph.addRecipe(recipe.getId(),
                    ingredients.stream().map(GunSmithTableIngredient::getIngredient).toList(),
                    ingredients.stream().map(GunSmithTableIngredient::getCount).toList(),
                    recipe.getOutput(),
                    (items) -> {
                        CraftGuideStepData step = new CraftGuideStepData(
                                new Target(CraftingType.TYPE, pos),
                                items,
                                List.of(recipe.getOutput()),
                                TaczType.TYPE,
                                false,
                                compoundTag
                        );
                        return new CraftGuideData(
                                List.of(step),
                                TaczType.TYPE
                        );
                    });
        });
    }

    @Override
    public void onCache(RecipeManager manager) {
        manager.getAllRecipesFor(ModRecipe.GUN_SMITH_TABLE_CRAFTING.get())
                .forEach(recipe -> {
                    RecipeIngredientCache.addRecipeCache(
                            recipe.getId(),
                            recipe.getInputs().stream().map(GunSmithTableIngredient::getIngredient).toList()
                    );
                });
    }

    @Override
    public Component getConfigName() {
        return Component.translatable("config.maid_storage_manager.crafting.generating.maid_storage_manager.work_bench");
    }
}
