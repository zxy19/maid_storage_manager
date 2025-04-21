package studio.fantasyit.maid_storage_manager.craft.context.common;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.craft.context.AbstractCraftActionContext;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftLayer;
import studio.fantasyit.maid_storage_manager.util.InvUtil;

import java.util.List;

public class CommonThrowItemAction extends AbstractCraftActionContext {
    public static final ResourceLocation TYPE = new ResourceLocation(MaidStorageManager.MODID, "throw");
    int ingredientIndex = 0;
    List<ItemStack> ingredients;

    public CommonThrowItemAction(EntityMaid maid, CraftGuideData craftGuideData, CraftGuideStepData craftGuideStepData, CraftLayer layer) {
        super(maid, craftGuideData, craftGuideStepData, layer);
    }

    @Override
    public Result start() {
        ingredients = craftGuideStepData.getNonEmptyInput();
        //不能动（），再动的话投掷位置偏差巨大会导致很大问题
        maid.getNavigation().stop();
        if (ingredients.isEmpty())
            return Result.SUCCESS;
        return Result.CONTINUE;
    }

    @Override
    public Result tick() {
        if (maid.getDeltaMovement().length() > 0.1) return Result.CONTINUE;
        ItemStack current = ingredients.get(ingredientIndex);
        if (current.isEmpty()) return Result.CONTINUE;
        ItemStack toThrow = InvUtil.tryExtract(maid.getAvailableInv(false), current, craftGuideStepData.matchTag);
        if (toThrow.getCount() < current.getCount()) {
            InvUtil.tryPlace(maid.getAvailableInv(false), current);
            return Result.FAIL;
        }
        BlockPos tp = craftGuideStepData.getStorage().pos;
        BlockPos directlyTarget = new BlockPos(tp.getX(), maid.getBlockY() >= tp.getY() ? tp.getY() : tp.getY() - 1, tp.getZ());

        Vec3 direction = directlyTarget.getCenter().subtract(maid.getPosition(0));
        InvUtil.throwItem(maid, toThrow, direction);
        ingredientIndex++;
        if (ingredientIndex >= ingredients.size()) {
            return Result.SUCCESS;
        } else {
            return Result.CONTINUE;
        }
    }

    @Override
    public void stop() {
    }
}
