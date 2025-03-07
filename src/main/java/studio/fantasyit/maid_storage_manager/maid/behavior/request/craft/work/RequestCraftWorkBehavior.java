package studio.fantasyit.maid_storage_manager.maid.behavior.request.craft.work;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraftforge.items.wrapper.RangedWrapper;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.craft.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.CraftLayer;
import studio.fantasyit.maid_storage_manager.debug.DebugData;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;
import studio.fantasyit.maid_storage_manager.storage.Storage;
import studio.fantasyit.maid_storage_manager.storage.base.*;
import studio.fantasyit.maid_storage_manager.util.*;

import java.util.*;
import java.util.function.Function;

public class RequestCraftWorkBehavior extends Behavior<EntityMaid> {
    BehaviorBreath breath = new BehaviorBreath();
    IStorageContext context;
    CraftGuideStepData craftGuideStepData;
    boolean isOutput;
    private CraftLayer layer;
    int slot;
    private boolean done;
    private boolean fail;
    private int tryTick;

    public RequestCraftWorkBehavior() {
        super(Map.of(), 10000000);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, EntityMaid maid, long p_22547_) {
        if (!Conditions.takingRequestList(maid)) return false;
        if (!MemoryUtil.getRequestProgress(maid).isTryCrafting()) return false;
        if (!MemoryUtil.getCrafting(maid).getCurrentLayer().hasCollectedAll()) return false;
        return !done;
    }

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel level, @NotNull EntityMaid maid) {
        if (MemoryUtil.getCurrentlyWorking(maid) != ScheduleBehavior.Schedule.REQUEST) return false;
        if (!MemoryUtil.getRequestProgress(maid).isTryCrafting()) return false;
        if (!MemoryUtil.getCrafting(maid).hasStartWorking()) return false;
        if (!MemoryUtil.getCrafting(maid).hasTarget()) return false;
        if (!MemoryUtil.getCrafting(maid).hasCurrent()) return false;
        if (!MemoryUtil.getCrafting(maid).getCurrentLayer().hasCollectedAll()) return false;
        return Conditions.hasReachedValidTargetOrReset(maid);
    }


    @Override
    protected void start(@NotNull ServerLevel level, @NotNull EntityMaid maid, long gameTimeIn) {
        fail = false;
        if (!MemoryUtil.getCrafting(maid).hasTarget()) {
            fail = done = true;
            return;
        }
        layer = MemoryUtil.getCrafting(maid).getCurrentLayer();
        craftGuideStepData = layer.getStepData();
        Storage storageTarget = MemoryUtil.getCrafting(maid).getTarget();
        if (!storageTarget.getType().equals(new ResourceLocation(MaidStorageManager.MODID, "crafting"))) {
            //非工作台配方
            Storage target = MaidStorage.getInstance().isValidTarget(level, maid, storageTarget.pos, storageTarget.side);
            if (target == null) {
                fail = done = true;
                return;
            }

            IMaidStorage storage = Objects.requireNonNull(MaidStorage.getInstance().getStorage(target.getType()));
            if (layer.isOutput())
                context = storage.onStartCollect(level, maid, target);
            else
                context = storage.onStartPlace(level, maid, target);
            if (context != null) {
                context.start(maid, level, target);
            }
        }
        if (craftGuideStepData == null) {
            MemoryUtil.getCrafting(maid).lastSuccess();
            done = true;
            return;
        }
        slot = 0;
        tryTick = 0;
        done = false;
        breath.reset();
    }

    @Override
    protected void tick(ServerLevel level, EntityMaid maid, long p_22553_) {
        tryTick++;
        if (allDone(maid)) {
            done = true;
        } else if (tryTick > Config.maxCraftTries) {
            fail = true;
            done = true;
            return;
        }
        if (!breath.breathTick()) return;
        if (layer.getCraftData().isPresent()) {
            CraftGuideData craftGuideData = layer.getCraftData().get();
            if (craftGuideData.getInput1().available()
                    && craftGuideData
                    .getInput1()
                    .storage
                    .getType()
                    .equals(new ResourceLocation(MaidStorageManager.MODID, "crafting"))
            ) {
                if (!layer.isOutput()) {
                    fail = false;
                    done = true;
                    return;
                } else {
                    tryCrafting(level, maid);
                    return;
                }
            }
        }
        if (layer.isOutput()) tickTakeResult(level, maid);
        else placeIngredient(level, maid);
    }

    private boolean allDone(EntityMaid maid) {
        if (craftGuideStepData == null) return false;
        for (int i = 0; i < craftGuideStepData.items.size(); i++) {
            if (layer.getCurrentStepCount(i) < craftGuideStepData.items.get(i).getCount()) {
                return false;
            }
        }
        return true;
    }

    private void tryCrafting(ServerLevel level, EntityMaid maid) {
        layer.getCraftData().ifPresentOrElse(craftGuideData -> {
            RangedWrapper inv = maid.getAvailableBackpackInv();
            List<ItemStack> needs = craftGuideData.getInput1().items;
            int[] slotExtractCount = new int[inv.getSlots()];
            Arrays.fill(slotExtractCount, 0);
            boolean allMatch = true;
            for (int i = 0; i < needs.size(); i++) {
                boolean found = false;
                if (needs.get(i).isEmpty()) continue;
                for (int j = 0; j < inv.getSlots(); j++) {
                    if (ItemStack.isSameItemSameTags(inv.getStackInSlot(j), needs.get(i))) {
                        //还有剩余（
                        if (inv.getStackInSlot(j).getCount() > slotExtractCount[j]) {
                            found = true;
                            slotExtractCount[j] += 1;
                            break;
                        }
                    }
                }
                if (!found) {
                    allMatch = false;
                    break;
                }
            }
            if (allMatch) {
                CraftingContainer container = RecipeUtil.wrapContainer(
                        this.layer.getCraftData().get().input1.items
                        , 3, 3);
                Optional<CraftingRecipe> recipe = RecipeUtil.getRecipe(level, container);
                if (recipe.isPresent()) {
                    ItemStack result = recipe.get().assemble(container, level.registryAccess());
                    if (ItemStack.isSameItemSameTags(result, craftGuideStepData.getItems().get(0))) {
                        layer.addCurrentStepPlacedCounts(0, 1);
                    }
                    int maxCanPlace = InvUtil.maxCanPlace(maid.getAvailableBackpackInv(), result);
                    if (maxCanPlace > result.getCount()) {
                        InvUtil.tryPlace(maid.getAvailableBackpackInv(), result);
                        for (int j = 0; j < inv.getSlots(); j++) {
                            inv.extractItem(j, slotExtractCount[j], false);
                        }
                    } else {
                        fail = true;
                    }
                }
            } else {
                fail = true;
            }
        }, () -> {
            fail = true;
        });
        done = true;
    }

    private void tickTakeResult(ServerLevel level, EntityMaid maid) {
        List<ItemStack> allItems = craftGuideStepData.getItems();
        Function<ItemStack, ItemStack> taker = itemStack -> {
            int idx = -1;
            for (int i = 0; i < allItems.size(); i++) {
                if (ItemStack.isSameItemSameTags(allItems.get(i), itemStack)) {
                    idx = i;
                    break;
                }
            }
            if (idx != -1) {
                int totalCount = craftGuideStepData.getItems().get(idx).getCount();
                int takenCount = layer.getCurrentStepCount(idx);
                int toTakeCount = Math.min(totalCount - takenCount, itemStack.getCount());
                ItemStack takenItem = itemStack.copyWithCount(toTakeCount);
                ItemStack itemStack1 = InvUtil.tryPlace(maid.getAvailableBackpackInv(), takenItem);
                takenItem.shrink(itemStack1.getCount());
                layer.addCurrentStepPlacedCounts(idx, takenItem.getCount());
                if (takenItem.getCount() > 0) tryTick = 0;
                return itemStack.copyWithCount(itemStack.getCount() - takenItem.getCount());
            }
            return itemStack;
        };
        if (context instanceof IStorageExtractableContext isec) {
            isec.extract(craftGuideStepData.getItems(), true, taker);
        } else if (context instanceof IStorageInteractContext isic) {
            isic.tick(taker);
        }
        if (context.isDone())
            context.reset();
    }

    private void placeIngredient(ServerLevel level, EntityMaid maid) {
        RangedWrapper inv = maid.getAvailableBackpackInv();
        if (context instanceof IStorageInsertableContext isic) {
            @NotNull ItemStack item = inv.getStackInSlot(slot);
            int idx = -1;
            for (int i = 0; i < craftGuideStepData.getItems().size(); i++) {
                //物品匹配且还需继续放入
                ItemStack stepItem = craftGuideStepData.getItems().get(i);
                if (ItemStack.isSameItemSameTags(stepItem, item)) {
                    if (layer.getCurrentStepCount(i) < stepItem.getCount()) {
                        idx = i;
                        break;
                    }
                }
            }
            if (idx != -1) {
                int placed = layer.getCurrentStepCount(idx);
                int required = craftGuideStepData.getItems().get(idx).getCount();
                int pick = Math.min(
                        required - placed,
                        item.getCount()
                );
                ItemStack copy = item.copyWithCount(pick);
                ItemStack rest = isic.insert(copy);
                item.shrink(pick - rest.getCount());
                layer.addCurrentStepPlacedCounts(idx, pick - rest.getCount());
                if (pick - rest.getCount() == 0) {
                    slot++;
                } else {
                    tryTick = 0;
                }
            } else {
                slot++;
            }

            if (slot >= inv.getSlots()) {
                if (layer.getStep() == 1)//Input 1:尽力满足输入，而非必须全部输入
                    done = true;
                else
                    slot = 0;
            }
        } else {
            fail = true;
            done = true;
        }
    }


    @Override
    protected void stop(ServerLevel level, EntityMaid maid, long p_22550_) {
        super.stop(level, maid, p_22550_);
        if (context != null) {
            context.finish();
            if (context.isDone()) {
                Storage targetPos = MemoryUtil.getCrafting(maid).getTarget();
                MemoryUtil.getCrafting(maid).addVisitedPos(targetPos);
                InvUtil.checkNearByContainers(level, targetPos.getPos(), (pos) -> {
                    MemoryUtil.getCrafting(maid).addVisitedPos(targetPos.sameType(pos, null));
                });
            }
        }
        MemoryUtil.getCrafting(maid).clearTarget();
        MemoryUtil.clearTarget(maid);

        if (fail) {
            DebugData.getInstance().sendMessage("[REQUEST_CRAFT_WORK]crafting fail");
            MemoryUtil.getCrafting(maid).failCurrent(maid, craftGuideStepData.getItems());
        } else {
            DebugData.getInstance().sendMessage("[REQUEST_CRAFT_WORK]crafting done %s", layer.getStep());
            MemoryUtil.getCrafting(maid).getCurrentLayer().nextStep();
            if (MemoryUtil.getCrafting(maid).getCurrentLayer().isDone()) {
                DebugData.getInstance().sendMessage("[REQUEST_CRAFT_WORK]layer done");
                MemoryUtil.getCrafting(maid).nextLayer();
                MemoryUtil.getCrafting(maid).resetAndMarkVisForRequest(level, maid);
            }
        }
    }

}
