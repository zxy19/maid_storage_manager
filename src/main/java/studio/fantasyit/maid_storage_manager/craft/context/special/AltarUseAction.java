package studio.fantasyit.maid_storage_manager.craft.context.special;


import com.github.tartaricacid.touhoulittlemaid.capability.PowerCapabilityProvider;
import com.github.tartaricacid.touhoulittlemaid.crafting.AltarRecipe;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonUseAction;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftLayer;
import studio.fantasyit.maid_storage_manager.util.RecipeUtil;

import java.util.List;
import java.util.Optional;

public class AltarUseAction extends CommonUseAction {
    public static final ResourceLocation TYPE = new ResourceLocation(MaidStorageManager.MODID, "use_with_power");

    public AltarUseAction(EntityMaid maid, CraftGuideData craftGuideData, CraftGuideStepData craftGuideStepData, CraftLayer layer) {
        super(maid, craftGuideData, craftGuideStepData, layer);
    }

    ItemStack tmpFirstItem = ItemStack.EMPTY;

    @Override
    public Result start() {
        Optional<AltarRecipe> recipe = RecipeUtil.getAltarRecipe(maid.level(), RecipeUtil.wrapAltarRecipeInventory(craftGuideStepData.getInput()));
        if (recipe.isEmpty()) {
            tmpFirstItem = ItemStack.EMPTY;
            return Result.FAIL;
        }
        //最后一个是没处理的，第一个是程序即将要处理的
        tmpFirstItem = craftGuideStepData.getInput().get(0);
        List<ItemStack> nonEmptyItems = craftGuideStepData.getNonEmptyItems();
        craftGuideStepData.setInput(0, nonEmptyItems.get(nonEmptyItems.size() - 1));
        Result result = super.start();
        if (result == Result.FAIL || result == Result.SUCCESS)
            return result;
        int cost = (int) Math.ceil(recipe.get().getPowerCost() / 4);
        if (maid.getExperience() >= cost) {
            maid.setExperience(maid.getExperience() - cost);
        } else {
            return Result.FAIL;
        }
        fakePlayer.getCapability(PowerCapabilityProvider.POWER_CAP).ifPresent(powerCapability -> {
            powerCapability.add(recipe.get().getPowerCost());
        });
        return result;
    }

    @Override
    public void stop() {
        craftGuideStepData.setInput(0, tmpFirstItem);
        super.stop();
    }
}