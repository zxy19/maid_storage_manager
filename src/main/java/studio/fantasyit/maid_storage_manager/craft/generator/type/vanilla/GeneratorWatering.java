package studio.fantasyit.maid_storage_manager.craft.generator.type.vanilla;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.apache.commons.lang3.mutable.MutableInt;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonUseAction;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.generator.algo.GeneratorGraph;
import studio.fantasyit.maid_storage_manager.craft.generator.cache.RecipeIngredientCache;
import studio.fantasyit.maid_storage_manager.craft.generator.type.base.IAutoCraftGuideGenerator;
import studio.fantasyit.maid_storage_manager.craft.type.CommonType;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.PosUtil;

import java.util.List;

public class GeneratorWatering implements IAutoCraftGuideGenerator {
    @Override
    public ResourceLocation getType() {
        return new ResourceLocation(MaidStorageManager.MODID, "watering");
    }

    @Override
    public boolean isBlockValid(Level level, BlockPos pos) {
        if (!level.getBlockState(pos).is(Blocks.WATER)) return false;
        MutableInt count = new MutableInt(0);
        PosUtil.findAround(pos, t -> {
            if (level.getBlockState(t).is(Blocks.WATER))
                count.increment();
            return null;
        });
        return count.intValue() >= 3;
    }

    @Override
    public void generate(List<InventoryItem> inventory, Level level, BlockPos pos, GeneratorGraph graph) {
        graph.addRecipe(
                new ResourceLocation(MaidStorageManager.MODID, "watering_bottle"),
                List.of(Ingredient.of(Items.GLASS_BOTTLE)),
                List.of(1),
                List.of(PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER)),
                items -> {
                    CraftGuideStepData step = new CraftGuideStepData(
                            Target.virtual(pos, null),
                            items,
                            List.of(PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER)),
                            CommonUseAction.TYPE_R,
                            false,
                            new CompoundTag()
                    );
                    return new CraftGuideData(
                            List.of(step),
                            CommonType.TYPE
                    );
                }
        );
        graph.addRecipe(
                new ResourceLocation(MaidStorageManager.MODID, "watering_bucket"),
                List.of(Ingredient.of(Items.BUCKET)),
                List.of(1),
                List.of(new ItemStack(Items.WATER_BUCKET)),
                items -> {
                    CraftGuideStepData step = new CraftGuideStepData(
                            Target.virtual(pos, null),
                            items,
                            List.of(new ItemStack(Items.WATER_BUCKET)),
                            CommonUseAction.TYPE_R,
                            false,
                            new CompoundTag()
                    );
                    return new CraftGuideData(
                            List.of(step),
                            CommonType.TYPE
                    );
                }
        );
    }

    @Override
    public void onCache(RecipeManager manager) {
        RecipeIngredientCache.addRecipeCache(
                new ResourceLocation(MaidStorageManager.MODID, "watering_bottle"),
                List.of(Ingredient.of(Items.GLASS_BOTTLE))
        );
        RecipeIngredientCache.addRecipeCache(
                new ResourceLocation(MaidStorageManager.MODID, "watering_bucket"),
                List.of(Ingredient.of(Items.BUCKET))
        );
    }
}
