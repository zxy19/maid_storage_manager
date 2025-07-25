package studio.fantasyit.maid_storage_manager.items;

import com.github.tartaricacid.touhoulittlemaid.api.bauble.IMaidBauble;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.inventory.handler.BaubleItemHandler;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.work.CraftLayer;
import studio.fantasyit.maid_storage_manager.craft.work.CraftLayerChain;
import studio.fantasyit.maid_storage_manager.craft.work.SolvedCraftLayer;
import studio.fantasyit.maid_storage_manager.maid.ChatTexts;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.maid.memory.CraftMemory;
import studio.fantasyit.maid_storage_manager.maid.memory.ViewedInventoryMemory;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.Conditions;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;
import studio.fantasyit.maid_storage_manager.util.RequestItemUtil;
import studio.fantasyit.maid_storage_manager.util.StorageAccessUtil;

import java.util.*;

public class WorkCardItem extends MaidInteractItem implements IMaidBauble {
    @Override
    public void onTick(EntityMaid maid, ItemStack baubleItem) {
        if (maid.level().isClientSide) return;
        checkTask(maid, baubleItem);
        tryDispatch(maid, baubleItem);
        tryDispatchFind(maid, baubleItem);
    }

    private void checkTask(EntityMaid maid, ItemStack baubleItem) {
        CraftMemory craftMemory = MemoryUtil.getCrafting(maid);
        if (craftMemory.hasPlan())
            craftMemory.plan().checkDispatchedValidation(maid);
    }

    protected void tryDispatch(EntityMaid maid, ItemStack baubleItem) {
        if (MemoryUtil.getCurrentlyWorking(maid) != ScheduleBehavior.Schedule.REQUEST) return;
        if (!MemoryUtil.getRequestProgress(maid).isTryCrafting()) return;
        CraftMemory crafting = MemoryUtil.getCrafting(maid);
        if (!crafting.hasPlan() || !crafting.plan().isMaster()) return;
        if (crafting.isGoPlacingBeforeCraft()) return;
        CraftLayerChain plan = crafting.plan();
        if (plan.getIsStoppingAdding()) return;

        // 寻找范围内的可分发的女仆
        getNearbyMaidsSameGroup(maid, baubleItem, true)
                .stream()
                .sorted(Comparator.comparingDouble(toMaid -> toMaid.distanceTo(maid)))
                .forEach(toMaid -> {
                    //获取可分发的层
                    @Nullable Pair<CraftLayer, SolvedCraftLayer> dispatchLayerData = plan.getAndDispatchLayer(toMaid);
                    if (dispatchLayerData == null) return;
                    SolvedCraftLayer node = dispatchLayerData.getB();
                    CraftLayer dispatchLayer = dispatchLayerData.getA();

                    //获取所有的输出，构建虚拟方案
                    List<ItemStack> targetItems = dispatchLayer
                            .getCraftData()
                            .map(CraftGuideData::getAllOutputItems)
                            .map(t -> t.stream().map(i -> i.copyWithCount(i.getCount() * dispatchLayer.getCount())).toList())
                            .orElseThrow();
                    CraftLayerChain newPlan = new CraftLayerChain(toMaid);
                    newPlan.setMaster(maid.getUUID(), MemoryUtil.getRequestProgress(maid).getWorkUUID());
                    newPlan.addLayer(dispatchLayer.copyWithNoState());
                    newPlan.addLayer(new CraftLayer(
                            Optional.empty(),
                            targetItems,
                            1
                    ));

                    //构建虚拟请求列表任务。
                    ItemStack dispatchedRequest = RequestItemUtil.makeVirtualItemStack(
                            targetItems,
                            null,
                            maid,
                            "DISPATCHED"
                    );
                    CompoundTag data = new CompoundTag();
                    data.putUUID("master", maid.getUUID());
                    data.putInt("index", node.index());
                    RequestListItem.setVirtualData(dispatchedRequest, data);

                    //构建记忆，直接开始合成，跳过寻找阶段
                    MemoryUtil.getCrafting(toMaid).setGatheringDispatched(true);
                    MemoryUtil.getCrafting(toMaid).setPlan(newPlan);
                    MemoryUtil.getRequestProgress(toMaid).newWork(RequestListItem.getUUID(dispatchedRequest));
                    MemoryUtil.getRequestProgress(toMaid).setTryCrafting(true);
                    toMaid.setItemInHand(InteractionHand.MAIN_HAND, dispatchedRequest);

                    //执行分发，标记为已分发的任务。
                    plan.doDispatchLayer(node, toMaid.getUUID(), RequestListItem.getUUID(dispatchedRequest));
                    plan.showCraftingProgress(maid);
                    newPlan.showCraftingProgress(toMaid);

                    MemoryUtil.getViewedInventory(toMaid).receiveFrom(MemoryUtil.getViewedInventory(maid));
                    MemoryUtil.clearTarget(toMaid);
                    MemoryUtil.getCrafting(toMaid).clearTarget();
                    MemoryUtil.getCrafting(toMaid).clearIgnoreTargets();
                    MemoryUtil.getCrafting(toMaid).addIgnoreTargets(crafting.getIgnoreTargets());
                    MemoryUtil.getCrafting(toMaid).resetAndMarkVis((ServerLevel) maid.level(), toMaid);
                    newPlan.setStatusMessage(toMaid, Component.translatable(ChatTexts.CHAT_CRAFT_DISPATCHED));
                });
    }

    protected void tryDispatchFind(EntityMaid maid, ItemStack baubleItem) {
        if (Conditions.takingRequestList(maid) && maid.getVehicle() != null && maid.getMainHandItem().is(ItemRegistry.REQUEST_LIST_ITEM.get())) {
            if (MemoryUtil.getRequestProgress(maid).isTryCrafting()) return;
            if (MemoryUtil.getRequestProgress(maid).isReturning()) return;
            if (MemoryUtil.getCurrentlyWorking(maid) != ScheduleBehavior.Schedule.REQUEST) return;
            List<EntityMaid> nearbyMaidsSameGroup = getNearbyMaidsSameGroup(maid, baubleItem, true);
            if (!nearbyMaidsSameGroup.isEmpty()) {
                EntityMaid toMaid = nearbyMaidsSameGroup.get(0);

                ItemStack itemStack = RequestItemUtil.makeVirtualItemStack(maid.getMainHandItem(), "DISPATCH_FIND");
                CompoundTag data = new CompoundTag();
                data.putUUID("master", maid.getUUID());
                RequestListItem.setVirtualData(itemStack, data);

                toMaid.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
                maid.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);

                ChatTexts.send(maid, Component.translatable(ChatTexts.CHAT_REQUEST_DISPATCH, toMaid.getName()));
            }
        }
    }

    public static boolean matches(ItemStack incoming, ItemStack source) {
        if (incoming.has(DataComponents.CUSTOM_NAME)) {
            if (source.has(DataComponents.CUSTOM_NAME) && !source.getHoverName().equals(incoming.getHoverName())) {
                return false;
            }
        }
        return true;
    }

    protected static boolean hasBaubleAndAvailable(EntityMaid maid, ItemStack source, boolean requireAvailable) {
        if (requireAvailable) {
            if (MemoryUtil.getCurrentlyWorking(maid) != ScheduleBehavior.Schedule.VIEW)
                return false;
            if (!maid.getMainHandItem().isEmpty()) return false;
            if (maid.isMaidInSittingPose()) return false;
            if (maid.getVehicle() != null) return false;
            if (MemoryUtil.getViewedInventory(maid).isViewing()) return false;
            if (!Conditions.isNothingToPlace(maid)) return false;
        }
        BaubleItemHandler t = maid.getMaidBauble();
        for (int i = 0; i < t.getSlots(); i++)
            if (t.getStackInSlot(i).is(ItemRegistry.WORK_CARD.get())) {
                //如果当前物品存在名字，而且目标物品也存在名字，而且不一样，那么跳过
                if (!matches(t.getStackInSlot(i), source)) {
                    continue;
                }
                return true;
            }
        return false;
    }

    public static List<EntityMaid> getNearbyMaidsSameGroup(EntityMaid maid, boolean requireAvailable, boolean propagate) {
        List<EntityMaid> maids = new ArrayList<>();
        Set<Component> hasChecked = new HashSet<>();
        Queue<ItemStack> queue = new LinkedList<>();
        BaubleItemHandler inv = maid.getMaidBauble();
        for (int i = 0; i < inv.getSlots(); i++) {
            if (inv.getStackInSlot(i).is(ItemRegistry.WORK_CARD.get())) {
                queue.add(inv.getStackInSlot(i));
                // 空名字天然匹配一切，可以直接跳过
                if (!inv.getStackInSlot(i).has(DataComponents.CUSTOM_NAME))
                    return getNearbyMaidsSameGroup(maid, inv.getStackInSlot(i), requireAvailable);
                hasChecked.add(inv.getStackInSlot(i).getHoverName());
            }
        }
        // 如果匹配到空的名字，那么可以直接退出。所有的相关的都能被匹配
        while (!queue.isEmpty()) {
            ItemStack stack = queue.poll();
            List<EntityMaid> tmp = getNearbyMaidsSameGroup(maid, stack, requireAvailable);
            for (EntityMaid nearbyMaid : tmp) {
                if (maids.stream().noneMatch(m -> m.getUUID().equals(nearbyMaid.getUUID()))) {
                    maids.add(nearbyMaid);
                }
                if (!propagate) continue;
                BaubleItemHandler tt = nearbyMaid.getMaidBauble();
                for (int i = 0; i < tt.getSlots(); i++) {
                    if (!tt.getStackInSlot(i).is(ItemRegistry.WORK_CARD.get())) continue;
                    if (!tt.getStackInSlot(i).has(DataComponents.CUSTOM_NAME))
                        return getNearbyMaidsSameGroup(maid, inv.getStackInSlot(i), requireAvailable);
                    if (!hasChecked.contains(tt.getStackInSlot(i).getHoverName())) {
                        queue.add(tt.getStackInSlot(i));
                        hasChecked.add(tt.getStackInSlot(i).getHoverName());
                    }
                }
            }
        }
        return maids;
    }

    public static List<EntityMaid> getNearbyMaidsSameGroup(EntityMaid maid, ItemStack baubleItem, boolean requireAvailable) {
        Level level = maid.level();
        return level.getEntities(
                EntityTypeTest.forClass(EntityMaid.class),
                getMaidFindingBBox(maid),
                t -> hasBaubleAndAvailable(t, baubleItem, requireAvailable) && !t.getUUID().equals(maid.getUUID())
        );
    }

    private static AABB getMaidFindingBBox(EntityMaid maid) {
        if (maid.hasRestriction())
            return new AABB(maid.getRestrictCenter()).inflate(maid.getRestrictRadius());
        return new AABB(maid.blockPosition()).inflate(7);
    }

    public static void syncStorageOn(EntityMaid maid, Target ambitiousTarget) {
        Target target = MemoryUtil.getViewedInventory(maid).ambitiousPos((ServerLevel) maid.level(), ambitiousTarget);
        Map<String, List<ViewedInventoryMemory.ItemCount>> itemsAt = MemoryUtil.getViewedInventory(maid).getItemsAtInternal(target);
        ServerLevel level = (ServerLevel) maid.level();
        getNearbyMaidsSameGroup(maid, false, true)
                .forEach(toMaid -> {
                    if (!StorageAccessUtil.isValidTarget(level, toMaid, target, false)) return;
                    ViewedInventoryMemory toMem = MemoryUtil.getViewedInventory(toMaid);
                    if (toMem.isLockedAmbitious(level, target)) return;

                    toMem.resetViewedInvForPos(target);
                    StorageAccessUtil.checkNearByContainers(level, target.getPos(), pos -> {
                        toMem.resetViewedInvForPosAsRemoved(target.sameType(pos, null));
                    });
                    toMem.setItemsAtInternal(target, itemsAt);
                    toMem.addVisitedPos(target);
                });
    }
}
