package studio.fantasyit.maid_storage_manager.craft.context.common;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.craft.action.ActionOption;
import studio.fantasyit.maid_storage_manager.craft.context.AbstractCraftActionContext;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.work.CraftLayer;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.storage.base.IMaidStorage;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageContext;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageInsertableContext;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.List;

public class CommonPlaceItemAction extends AbstractCraftActionContext {
    public static final ActionOption<Boolean> OPTION_SPLIT = new ActionOption<>(
            ResourceLocation.fromNamespaceAndPath(MaidStorageManager.MODID, "split"),
            new Component[]{
                    Component.translatable("gui.maid_storage_manager.craft_guide.common.no_split"),
                    Component.translatable("gui.maid_storage_manager.craft_guide.common.split")
            },
            new ResourceLocation[]{
                    ResourceLocation.fromNamespaceAndPath("maid_storage_manager", "textures/gui/craft/option/no_split.png"),
                    ResourceLocation.fromNamespaceAndPath("maid_storage_manager", "textures/gui/craft/option/split.png")
            },
            "",
            new ActionOption.BiConverter<Integer, Boolean>(
                    i -> i != 0, b -> b ? 1 : 0
            ),
            ActionOption.ValuePredicatorOrGetter.getter(t ->
                    t ?
                            Component.translatable("gui.maid_storage_manager.craft_guide.common.split") :
                            Component.translatable("gui.maid_storage_manager.craft_guide.common.no_split")
            )
    );
    public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath(MaidStorageManager.MODID, "insert");
    protected IStorageContext storageContext;
    int slot = 0;
    int ingredientIndex = 0;

    public CommonPlaceItemAction(EntityMaid maid, CraftGuideData craftGuideData, CraftGuideStepData craftGuideStepData, CraftLayer layer) {
        super(maid, craftGuideData, craftGuideStepData, layer);
    }

    @Override
    public void loadEnv(CompoundTag env) {
        slot = env.contains("slot") ? env.getInt("slot") : 0;
        ingredientIndex = env.contains("ingredientIndex") ? env.getInt("ingredientIndex") : 0;
    }

    @Override
    public CompoundTag saveEnv(CompoundTag env) {
        env.putInt("slot", slot);
        env.putInt("ingredientIndex", ingredientIndex);
        return super.saveEnv(env);
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
        storageContext = storageType.onStartPlace(level, maid, validTarget);
        if (storageContext == null) {
            return Result.FAIL;
        }
        storageContext.start(maid, level, validTarget);
        return Result.CONTINUE;
    }

    @Override
    public Result tick() {
        if (allDone()) return Result.SUCCESS;
        boolean hasChange = false;
        boolean reTryStart = false;
        CombinedInvWrapper inv = maid.getAvailableInv(false);
        ItemStack stepItem = craftGuideStepData.getNonEmptyInput().get(ingredientIndex);
        if (storageContext instanceof IStorageInsertableContext isic) {
            boolean shouldDoPlace = false;
            int count = 0;
            for (; slot < inv.getSlots(); slot++) {
                //物品匹配且还需继续放入
                @NotNull ItemStack item = inv.getStackInSlot(slot);
                if (item.isEmpty()) continue;
                if (count++ > 10) break;
                if (ItemStackUtil.isSameInCrafting(stepItem, item)) {
                    if (craftLayer.getCurrentStepCount(ingredientIndex) < stepItem.getCount()) {
                        shouldDoPlace = true;
                        break;
                    }
                }
            }
            if (shouldDoPlace) {
                @NotNull ItemStack item = inv.getStackInSlot(slot);
                int placed = craftLayer.getCurrentStepCount(ingredientIndex);
                int required = stepItem.getCount();
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
                else
                    reTryStart = true;
                slot = 0;
            }
            if (ingredientIndex >= craftGuideStepData.getNonEmptyInput().size()) {
                return Result.SUCCESS;
            }
        } else {
            return Result.FAIL;
        }
        if (reTryStart)
            return hasChange ? Result.CONTINUE_INTERRUPTABLE : Result.NOT_DONE_INTERRUPTABLE;
        return hasChange ? Result.CONTINUE : Result.NOT_DONE;
    }

    @Override
    public void stop() {
        if (storageContext != null) {
            storageContext.finish();
        }
    }

    private boolean allDone() {
        if (craftGuideStepData == null) return false;
        List<ItemStack> items = craftGuideStepData.getInput();
        for (int i = 0; i < items.size(); i++) {
            if (craftLayer.getCurrentStepCount(i) < items.get(i).getCount()) {
                return false;
            }
        }
        return true;
    }
}
