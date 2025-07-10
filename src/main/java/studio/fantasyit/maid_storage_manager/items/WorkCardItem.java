package studio.fantasyit.maid_storage_manager.items;

import com.github.tartaricacid.touhoulittlemaid.api.bauble.IMaidBauble;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.inventory.handler.BaubleItemHandler;
import net.minecraft.nbt.CompoundTag;
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
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;
import studio.fantasyit.maid_storage_manager.util.RequestItemUtil;

import java.util.List;
import java.util.Optional;

public class WorkCardItem extends MaidInteractItem implements IMaidBauble {
    @Override
    public void onTick(EntityMaid maid, ItemStack baubleItem) {
        if (maid.level().isClientSide) return;
        checkTask(maid, baubleItem);
        tryDispatch(maid, baubleItem);
        checkBubble(maid);
    }

    private void checkBubble(EntityMaid maid) {
        if (maid.tickCount % 20 == 0)
            if (MemoryUtil.getCurrentlyWorking(maid) == ScheduleBehavior.Schedule.REQUEST && MemoryUtil.getRequestProgress(maid).isTryCrafting() && MemoryUtil.getCrafting(maid).hasPlan()) {
                CraftLayerChain plan = MemoryUtil.getCrafting(maid).plan();
                if (!plan.isDone() && !plan.hasCurrent()) {
                    plan.showCraftingProgress(maid);
                }
            }
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
        CraftLayerChain plan = crafting.plan();


        // 寻找范围内的可分发的女仆
        Level level = maid.level();
        level.getEntities(
                EntityTypeTest.forClass(EntityMaid.class),
                getMaidFindingBBox(maid),
                t -> hasBaubleAndAvailable(t, baubleItem)
        ).forEach(toMaid -> {
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
            CraftLayerChain newPlan = new CraftLayerChain(maid);
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

            MemoryUtil.getViewedInventory(toMaid).receiveFrom(MemoryUtil.getViewedInventory(maid));
            MemoryUtil.clearTarget(toMaid);
            MemoryUtil.getCrafting(toMaid).clearTarget();
            MemoryUtil.getCrafting(toMaid).clearIgnoreTargets();
            MemoryUtil.getCrafting(toMaid).addIgnoreTargets(crafting.getIgnoreTargets());
            MemoryUtil.getCrafting(toMaid).resetAndMarkVis((ServerLevel) maid.level(), toMaid);
            ChatTexts.send(toMaid, ChatTexts.CHAT_CRAFT_DISPATCHED);
        });
    }

    private AABB getMaidFindingBBox(EntityMaid maid) {
        if (maid.hasRestriction())
            return new AABB(maid.getRestrictCenter()).inflate(maid.getRestrictRadius());
        return new AABB(maid.blockPosition()).inflate(7);
    }

    protected boolean hasBaubleAndAvailable(EntityMaid maid, ItemStack source) {
        if (MemoryUtil.getCurrentlyWorking(maid) != ScheduleBehavior.Schedule.VIEW) return false;
        if (!maid.getMainHandItem().isEmpty()) return false;
        BaubleItemHandler t = maid.getMaidBauble();
        for (int i = 0; i < t.getSlots(); i++)
            if (t.getStackInSlot(i).is(ItemRegistry.WORK_CARD.get())) {
                //如果当前物品存在名字，而且目标物品也存在名字，而且不一样，那么跳过
                if (t.getStackInSlot(i).hasCustomHoverName()) {
                    if (source.hasCustomHoverName() && source.getHoverName().equals(t.getStackInSlot(i).getHoverName())) {
                        continue;
                    }
                }
                return true;
            }
        return false;
    }
}