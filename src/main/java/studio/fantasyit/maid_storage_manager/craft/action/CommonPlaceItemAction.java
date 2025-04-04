package studio.fantasyit.maid_storage_manager.craft.action;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftLayer;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.storage.base.IMaidStorage;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageContext;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageInsertableContext;

public class CommonPlaceItemAction extends AbstractCraftActionContext {
    IStorageContext storageContext;
    int slot = 0;
    int ingredientIndex = 0;
    public CommonPlaceItemAction(EntityMaid maid, CraftGuideData craftGuideData, CraftGuideStepData craftGuideStepData, int idx, CraftLayer layer) {
        super(maid, craftGuideData, craftGuideStepData, layer);
    }

    @Override
    public Result start() {
        ServerLevel level = (ServerLevel) maid.level();
        Target target = craftGuideStepData.getStorage();
        @Nullable Target validTarget = MaidStorage.getInstance().isValidTarget(level, maid, target);
        if (validTarget == null) {
            return Result.FAIL;
        }
        @Nullable IMaidStorage storageType = MaidStorage.getInstance().getStorage(validTarget.getType());
        if (storageType == null) {
            return Result.FAIL;
        }
        storageContext = storageType.onStartCollect(level, maid, validTarget);
        if (storageContext == null) {
            return Result.FAIL;
        }
        storageContext.start(maid, level, validTarget);
        return Result.CONTINUE;
    }

    @Override
    public Result tick() {
        boolean hasChange = false;
        CombinedInvWrapper inv = maid.getAvailableInv(false);
        ItemStack stepItem = craftGuideStepData.getItems().get(ingredientIndex);
        if (storageContext instanceof IStorageInsertableContext isic) {
            boolean shouldDoPlace = false;
            int count = 0;
            for (; slot < inv.getSlots(); slot++) {
                //物品匹配且还需继续放入
                @NotNull ItemStack item = inv.getStackInSlot(slot);
                if (item.isEmpty()) continue;
                if (count++ > 10) break;
                if (ItemStack.isSameItem(stepItem, item)) {
                    if (craftLayer.getCurrentStepCount(ingredientIndex) < stepItem.getCount()) {
                        shouldDoPlace = true;
                        break;
                    }
                }
            }
            if (shouldDoPlace) {
                @NotNull ItemStack item = inv.getStackInSlot(slot);
                int placed = craftLayer.getCurrentStepCount(ingredientIndex);
                int required = craftGuideStepData.getItems().get(ingredientIndex).getCount();
                int pick = Math.min(
                        required - placed,
                        item.getCount()
                );
                ItemStack copy = item.copyWithCount(pick);
                ItemStack rest = isic.insert(copy);
                item.shrink(pick - rest.getCount());
                craftLayer.addCurrentStepPlacedCounts(ingredientIndex, pick - rest.getCount());
                if (pick - rest.getCount() != 0) {
                    hasChange = true;
                } else if (craftLayer.getStep() == 1)
                    slot++;
            }

            if (craftLayer.getCurrentStepCount(ingredientIndex) >= stepItem.getCount()) {
                ingredientIndex++;
                slot = 0;
            } else if (slot >= inv.getSlots()) {
                if (craftGuideStepData.isOptional())//尽力满足输入，而非必须全部输入
                    ingredientIndex++;
                slot = 0;
            }
            if (ingredientIndex >= craftGuideStepData.getItems().size()) {
                return Result.SUCCESS;
            }
        } else {
            return Result.FAIL;
        }
        return hasChange ? Result.CONTINUE : Result.NOT_DONE;
    }

    @Override
    public void stop() {
        if (storageContext != null) {
            storageContext.finish();
        }
    }
}
