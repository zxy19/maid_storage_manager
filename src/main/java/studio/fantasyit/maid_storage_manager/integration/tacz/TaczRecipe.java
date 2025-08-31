package studio.fantasyit.maid_storage_manager.integration.tacz;

import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.block.entity.GunSmithTableBlockEntity;
import com.tacz.guns.config.sync.SyncConfig;
import com.tacz.guns.crafting.GunSmithTableRecipe;
import com.tacz.guns.init.ModItems;
import com.tacz.guns.init.ModRecipe;
import com.tacz.guns.item.ModernKineticGunItem;
import com.tacz.guns.resource.filter.RecipeFilter;
import com.tacz.guns.resource.index.CommonBlockIndex;
import com.tacz.guns.resource.pojo.data.block.TabConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import studio.fantasyit.maid_storage_manager.craft.CollectCraftEvent;
import studio.fantasyit.maid_storage_manager.craft.action.CraftAction;
import studio.fantasyit.maid_storage_manager.craft.action.PathTargetLocator;
import studio.fantasyit.maid_storage_manager.craft.context.special.TaczRecipeAction;
import studio.fantasyit.maid_storage_manager.craft.generator.type.misc.GeneratorTACZ;
import studio.fantasyit.maid_storage_manager.craft.type.TaczType;

import java.util.List;
import java.util.Optional;

public class TaczRecipe {
    public static void addType(CollectCraftEvent event) {
        event.addCraftType(new TaczType());
        event.addAction(
                TaczType.TYPE,
                TaczRecipeAction::new,
                PathTargetLocator::commonNearestAvailablePos,
                CraftAction.PathEnoughLevel.NORMAL.value,
                false,
                true,
                10,
                1,
                List.of()
        );
        event.addAutoCraftGuideGenerator(new GeneratorTACZ());
        event.addItemStackPredicate(ModItems.MODERN_KINETIC_GUN.get(), (stack, target) -> {
            if (stack.getItem() instanceof ModernKineticGunItem gun) {
                return gun.getGunId(stack).equals(gun.getGunId(target));
            }
            return false;
        });
    }

    public static ResourceLocation getBlockId(Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof GunSmithTableBlockEntity gsbe) {
            return Optional.ofNullable(gsbe.getId()).orElse(DefaultAssets.DEFAULT_BLOCK_ID);
        }
        return DefaultAssets.EMPTY_BLOCK_ID;
    }

    public static List<GunSmithTableRecipe> getAllRecipesForBlockId(Level level, ResourceLocation blockId) {
        RecipeManager recipeManager = level.getRecipeManager();
        List<GunSmithTableRecipe> allRecipesFor = recipeManager.getAllRecipesFor(ModRecipe.GUN_SMITH_TABLE_CRAFTING.get());
        Optional<CommonBlockIndex> commonBlockIndex = TimelessAPI.getCommonBlockIndex(blockId);

        if (commonBlockIndex.isPresent()) {
            List<TabConfig> tabs = commonBlockIndex.get().getData().getTabs();
            if (DefaultAssets.DEFAULT_BLOCK_ID.equals(blockId) && !(Boolean) SyncConfig.ENABLE_TABLE_FILTER.get()) {
                tabs = TabConfig.DEFAULT_TABS;
            }
            List<TabConfig> finalTabs = tabs;
            allRecipesFor = allRecipesFor.stream().filter(recipe -> {
                ResourceLocation group = recipe.getResult().getGroup();
                return finalTabs.stream().anyMatch(tab -> tab.id().equals(group));
            }).toList();

            if (!blockId.equals(DefaultAssets.DEFAULT_BLOCK_ID) || SyncConfig.ENABLE_TABLE_FILTER.get()) {
                RecipeFilter filter = commonBlockIndex.get().getFilter();
                if (filter != null)
                    allRecipesFor = filter.filter(allRecipesFor, GunSmithTableRecipe::getId);
            }
        }
        return allRecipesFor;
    }
}
