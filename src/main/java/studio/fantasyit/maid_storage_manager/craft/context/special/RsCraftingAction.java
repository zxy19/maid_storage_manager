package studio.fantasyit.maid_storage_manager.craft.context.special;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.refinedmods.refinedstorage.api.autocrafting.ICraftingManager;
import com.refinedmods.refinedstorage.api.autocrafting.task.ICalculationResult;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonTakeItemAction;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftLayer;
import studio.fantasyit.maid_storage_manager.storage.rs.AbstractRSContext;

public class RsCraftingAction extends CommonTakeItemAction {
    public RsCraftingAction(EntityMaid maid, CraftGuideData craftGuideData, CraftGuideStepData craftGuideStepData, CraftLayer layer) {
        super(maid, craftGuideData, craftGuideStepData, layer);
    }


    public static final ResourceLocation TYPE = new ResourceLocation(MaidStorageManager.MODID, "rs_craft");
    int nextCrafting = 0;

    int toTick = 0;

    public Result start() {
        return super.start();
    }

    @Override
    public Result tick() {
        Result result = super.tick();
        if (result == Result.SUCCESS)
            return Result.SUCCESS;
        if (storageContext instanceof AbstractRSContext rsContext) {
            if (nextCrafting >= craftGuideStepData.getOutput().size()) {
                if (maid.level().getServer().getTickCount() > toTick)
                    return Result.NOT_DONE;
                return Result.CONTINUE;
            }

            ICraftingManager craftingManager = rsContext.network.getCraftingManager();
            //当前没有计算任务

            ItemStack itemStack = craftGuideStepData.getOutput().get(nextCrafting);
            ICalculationResult currentCalculating = craftingManager.create(itemStack, itemStack.getCount());
            if (currentCalculating.isOk() && currentCalculating.getTask() != null) {
                craftingManager.start(currentCalculating.getTask());
            }
            nextCrafting++;
            //多等待两分钟
            toTick = maid.level().getServer().getTickCount() + 2400;
            return Result.CONTINUE;
        } else {
            return Result.FAIL;
        }
    }
}
