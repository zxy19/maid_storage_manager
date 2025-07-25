package studio.fantasyit.maid_storage_manager.craft.context.common;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.craft.context.AbstractCraftActionContext;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.work.CraftLayer;
import studio.fantasyit.maid_storage_manager.util.InvUtil;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.List;

public class CommonPickupItemAction extends AbstractCraftActionContext {
    public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath(MaidStorageManager.MODID, "pickup");
    int ingredientIndex = 0;
    List<ItemStack> ingredients;
    List<Entity> entities;

    public CommonPickupItemAction(EntityMaid maid, CraftGuideData craftGuideData, CraftGuideStepData craftGuideStepData, CraftLayer layer) {
        super(maid, craftGuideData, craftGuideStepData, layer);
    }

    @Override
    public void loadEnv(CompoundTag env) {
        ingredientIndex = env.contains("ingredientIndex") ? env.getInt("ingredientIndex") : 0;
    }

    @Override
    public CompoundTag saveEnv(CompoundTag env) {
        env.putInt("ingredientIndex", ingredientIndex);
        return super.saveEnv(env);
    }

    @Override
    public Result start() {
        ingredients = craftGuideStepData.getNonEmptyOutput();
        if (ingredients.isEmpty())
            return Result.SUCCESS;
        entities = null;
        return Result.CONTINUE;
    }

    @Override
    public Result tick() {
        if (allDone()) return Result.SUCCESS;
        ItemStack current = ingredients.get(ingredientIndex);
        int hasTaken = craftLayer.getCurrentStepCount(ingredientIndex);
        if (hasTaken >= current.getCount()) {
            ingredientIndex++;
            if (ingredientIndex >= ingredients.size()) {
                ingredientIndex %= ingredients.size();
                return Result.NOT_DONE_INTERRUPTABLE;
            }
            return Result.NOT_DONE;
        }
        if (entities == null)
            entities = maid.level().getEntities(maid, maid.getBoundingBox().inflate(2.8),
                    e -> e instanceof ItemEntity ie && EntityMaid.canInsertItem(ie.getItem()) && (!ie.hasPickUpDelay() || Config.pickupIgnoreDelay)
            );
        for (Entity entity : entities) {
            if (!entity.isAlive()) continue;
            if (entity instanceof ItemEntity ie && ItemStackUtil.isSameInCrafting(ie.getItem(), current)) {
                int toTakeCount = Math.min(ie.getItem().getCount(), current.getCount() - hasTaken);
                ItemStack notPlaced = InvUtil.tryPlace(maid.getAvailableInv(false), current.copyWithCount(toTakeCount));
                int realTake = toTakeCount - notPlaced.getCount();
                int realRemain = ie.getItem().getCount() - realTake;
                if (realTake != 0) {
                    craftLayer.addCurrentStepPlacedCounts(ingredientIndex, realTake);
                    maid.take(ie, toTakeCount - notPlaced.getCount());
                    if (realRemain == 0)
                        ie.discard();
                    else
                        ie.setItem(ie.getItem().copyWithCount(realRemain));

                    if (current.getCount() <= hasTaken + realTake) {
                        ingredientIndex++;
                        ingredientIndex %= ingredients.size();
                    }
                    return Result.CONTINUE;
                }
                return Result.NOT_DONE;
            }
        }
        entities = null;
        ingredientIndex++;
        if (ingredientIndex >= ingredients.size() && craftGuideStepData.isOptional()) {
            return Result.SUCCESS;
        }
        if (ingredientIndex >= ingredients.size()) {
            ingredientIndex %= ingredients.size();
            return Result.NOT_DONE_INTERRUPTABLE;
        }
        return Result.NOT_DONE;
    }

    @Override
    public void stop() {
    }


    private boolean allDone() {
        if (craftGuideStepData == null) return false;
        List<ItemStack> items = craftGuideStepData.getOutput();
        for (int i = 0; i < items.size(); i++) {
            if (craftLayer.getCurrentStepCount(i) < items.get(i).getCount()) {
                return false;
            }
        }
        return true;
    }
}
