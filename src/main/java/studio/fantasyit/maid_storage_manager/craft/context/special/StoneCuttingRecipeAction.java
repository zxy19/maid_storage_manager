package studio.fantasyit.maid_storage_manager.craft.context.special;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.craft.context.AbstractCraftActionContext;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftLayer;
import studio.fantasyit.maid_storage_manager.util.InvUtil;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;
import studio.fantasyit.maid_storage_manager.util.RecipeUtil;

import java.util.List;
import java.util.Optional;

public class StoneCuttingRecipeAction extends AbstractCraftActionContext {
    public static final ResourceLocation TYPE = new ResourceLocation(MaidStorageManager.MODID, "stone_cutting");

    public StoneCuttingRecipeAction(EntityMaid maid, CraftGuideData craftGuideData, CraftGuideStepData craftGuideStepData, CraftLayer layer) {
        super(maid, craftGuideData, craftGuideStepData, layer);
    }

    @Override
    public Result start() {
        Level level = maid.level();
        CombinedInvWrapper inv = maid.getAvailableInv(false);
        ItemStack input = craftGuideStepData.getInput().get(0);
        ItemStack output = craftGuideStepData.getOutput().get(0);
        ItemStack t1 = InvUtil.tryExtract(inv, input, craftGuideStepData.matchTag);
        if (ItemStackUtil.isSame(t1, input, craftGuideStepData.matchTag)) {
            List<StonecutterRecipe> stonecuttingRecipe = RecipeUtil.getStonecuttingRecipe(level, t1);
            Optional<StonecutterRecipe> first = stonecuttingRecipe.stream().filter(recipe ->
                    ItemStackUtil.isSame(recipe.getResultItem(level.registryAccess()), output, craftGuideStepData.matchTag)
            ).findFirst();
            if (first.isPresent()) {
                ItemStack tmpResult = first.get().getResultItem(level.registryAccess());
                ItemStack result = tmpResult.copyWithCount(tmpResult.getCount() * input.getCount());
                if (ItemStackUtil.isSame(result, output, craftGuideStepData.matchTag)) {
                    craftLayer.addCurrentStepPlacedCounts(0, result.getCount());

                    int maxCanPlace = InvUtil.maxCanPlace(inv, result);
                    if (maxCanPlace >= result.getCount()) {
                        InvUtil.tryPlace(inv, result);
                        return Result.SUCCESS;
                    }
                }
            }
        }
        InvUtil.tryPlace(inv, t1);
        return Result.FAIL;
    }

    @Override
    public Result tick() {
        return Result.SUCCESS;
    }

    @Override
    public void stop() {

    }
}
