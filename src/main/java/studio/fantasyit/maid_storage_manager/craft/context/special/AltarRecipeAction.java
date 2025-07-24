package studio.fantasyit.maid_storage_manager.craft.context.special;

import com.github.tartaricacid.touhoulittlemaid.crafting.AltarRecipe;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityAltar;
import com.github.tartaricacid.touhoulittlemaid.util.PosListData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.craft.context.AbstractDynamicAddedAction;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonPickupItemAction;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonUseAction;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.work.CraftLayer;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.PosUtil;
import studio.fantasyit.maid_storage_manager.util.RecipeUtil;

import java.util.List;
import java.util.Optional;

public class AltarRecipeAction extends AbstractDynamicAddedAction {
    public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath(MaidStorageManager.MODID, "altar");

    public AltarRecipeAction(EntityMaid maid, CraftGuideData craftGuideData, CraftGuideStepData craftGuideStepData, CraftLayer layer) {
        super(maid, craftGuideData, craftGuideStepData, layer);
    }

    @Override
    public Result getList() {
        Optional<AltarRecipe> recipe = RecipeUtil.getAltarRecipe(maid.level(), RecipeUtil.wrapAltarRecipeInventory(craftGuideStepData.getInput()));
        if (recipe.isEmpty())
            return Result.FAIL;
        float cost = recipe.get().getPowerCost();
        BlockPos target = craftGuideStepData.getStorage().pos;
        if (maid.level().getBlockEntity(target) instanceof TileEntityAltar tea) {
            PosListData canPlaceItemPosList = tea.getCanPlaceItemPosList();
            List<BlockPos> data = canPlaceItemPosList.getData();
            List<ItemStack> inputs = craftGuideStepData.getNonEmptyInput();
            int totalUsed = Math.min(data.size(), inputs.size());

            for (int i = 0; i < totalUsed; i++) {
                addStep(new CraftGuideStepData(
                        Target.virtual(data.get(i), null),
                        List.of(inputs.get(i)),
                        List.of(ItemStack.EMPTY),
                        CommonUseAction.TYPE,
                        false,
                        new CompoundTag()
                ));
            }
            addStep(new CraftGuideStepData(
                    Target.virtual(getCenterPos(data), null),
                    List.of(),
                    List.of(craftGuideStepData.getNonEmptyOutput().get(0)),
                    CommonPickupItemAction.TYPE,
                    false,
                    new CompoundTag()
            ));
        }
        return Result.SUCCESS;
    }

    BlockPos getCenterPos(List<BlockPos> data) {
        long acX = 0, acZ = 0, acY = 0;
        for (int i = 0; i < data.size(); i++) {
            int c = 0;
            for (int j = 0; j < data.size(); j++) {
                if (i != j && (data.get(i).getX() == data.get(j).getX() || data.get(i).getZ() == data.get(j).getZ()))
                    c++;
            }
            if (c == 2) {
                acX += data.get(i).getX();
                acZ += data.get(i).getZ();
                acY += data.get(i).getY();
            }
        }
        int ty = (int) (acY / 4);
        while (!PosUtil.isSafePos(maid.level(), new BlockPos((int) (acX / 4), ty, (int) (acZ / 4))) && ty > maid.level().getMinBuildHeight())
            ty--;
        return new BlockPos((int) (acX / 4), ty, (int) (acZ / 4));
    }
}
