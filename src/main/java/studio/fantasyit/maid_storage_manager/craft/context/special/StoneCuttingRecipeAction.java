package studio.fantasyit.maid_storage_manager.craft.context.special;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
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
        if (craftGuideStepData.getStorage() == null)
            return Result.FAIL;
        return Result.CONTINUE;
    }

    @Override
    public Result tick() {
        Level level = maid.level();
        if (!level.getBlockState(craftGuideStepData.storage.pos).is(Blocks.STONECUTTER))
            return Result.NOT_DONE;
        CombinedInvWrapper inv = maid.getAvailableInv(false);
        ItemStack input = craftGuideStepData.getInput().get(0);
        ItemStack output = craftGuideStepData.getOutput().get(0);
        ItemStack t1 = InvUtil.tryExtractForCrafting(inv, input);
        if (ItemStackUtil.isSameInCrafting(t1, input)) {
            List<StonecutterRecipe> stonecuttingRecipe = RecipeUtil.getStonecuttingRecipe(level, t1);
            Optional<StonecutterRecipe> first = stonecuttingRecipe.stream().filter(recipe ->
                    ItemStackUtil.isSameInCrafting(recipe.getResultItem(level.registryAccess()), output)
            ).findFirst();
            if (first.isPresent()) {
                ItemStack tmpResult = first.get().getResultItem(level.registryAccess());
                ItemStack result = tmpResult.copyWithCount(tmpResult.getCount() * input.getCount());
                if (ItemStackUtil.isSameInCrafting(result, output)) {
                    craftLayer.addCurrentStepPlacedCounts(0, result.getCount());

                    int maxCanPlace = InvUtil.maxCanPlace(inv, result);
                    if (maxCanPlace >= result.getCount()) {
                        InvUtil.tryPlace(inv, result);
                        level.playSound(null, craftGuideStepData.storage.pos, SoundEvents.UI_STONECUTTER_TAKE_RESULT, SoundSource.BLOCKS, 1.0F, 1.0F);
                        return Result.SUCCESS;
                    }
                }
            }
        }
        InvUtil.tryPlace(inv, t1);
        return Result.FAIL;
    }

    @Override
    public void stop() {

    }
}
