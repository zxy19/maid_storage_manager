package studio.fantasyit.maid_storage_manager.maid.behavior.request.craft.dispatched;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.craft.work.CraftLayer;
import studio.fantasyit.maid_storage_manager.craft.work.CraftLayerChain;
import studio.fantasyit.maid_storage_manager.entity.VirtualItemEntity;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.maid.memory.CraftMemory;
import studio.fantasyit.maid_storage_manager.util.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class DispatchedGatherBehavior extends Behavior<EntityMaid> {
    private List<ItemStack> list;

    public DispatchedGatherBehavior() {
        super(ImmutableMap.of());
    }

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel level, @NotNull EntityMaid maid) {
        if (MemoryUtil.getCurrentlyWorking(maid) != ScheduleBehavior.Schedule.REQUEST) return false;
        if (MemoryUtil.getRequestProgress(maid).isReturning()) return false;
        if (!Conditions.takingRequestList(maid)) return false;
        if (!MemoryUtil.getCrafting(maid).isGatheringDispatched()) return false;
        @Nullable UUID entityUU = RequestListItem.getStorageEntity(maid.getMainHandItem());
        if (entityUU == null)
            return false;
        @Nullable Entity entity = level.getEntity(entityUU);
        if (entity == null)
            return false;

        if (entity.distanceTo(maid) < 3) return true;

        Optional<WalkTarget> memory = maid.getBrain().getMemory(MemoryModuleType.WALK_TARGET);
        if (memory.isEmpty() || !(memory.get().getTarget() instanceof EntityTracker et) || !et.getEntity().equals(entity)) {
            MemoryUtil.clearTarget(maid);
        }
        return false;
    }


    VirtualItemEntity thrown;
    EntityMaid target;
    int index = 0;
    BehaviorBreath breath = new BehaviorBreath();

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long p_22542_) {
        index = 0;
        target = null;
        thrown = null;
        breath.reset();
        @Nullable UUID entityUU = RequestListItem.getStorageEntity(maid.getMainHandItem());
        if (entityUU == null)
            return;
        @Nullable Entity entity = level.getEntity(entityUU);
        if (entity == null)
            return;
        if (entity instanceof EntityMaid maid1) {
            CraftMemory targetCrafting = MemoryUtil.getCrafting(maid1);
            CraftMemory currentCrafting = MemoryUtil.getCrafting(maid);
            if (targetCrafting.hasPlan() && currentCrafting.hasPlan()) {
                CraftLayerChain plan = currentCrafting.plan();
                CraftLayerChain targetPlan = targetCrafting.plan();
                if (plan.hasCurrent()) {
                    CraftLayer outLayer = plan.getCurrentLayer();
                    list = targetPlan.getDispatchedRemainItem(outLayer);
                    target = maid1;
                    target.getNavigation().stop();
                    MemoryUtil.setTarget(target, maid, (float) Config.collectSpeed);
                }
            }
        }
    }

    @Override
    protected boolean canStillUse(ServerLevel p_22545_, EntityMaid maid, long p_22547_) {
        if (!Conditions.takingRequestList(maid)) return false;
        if (!MemoryUtil.getRequestProgress(maid).isTryCrafting()) return false;
        if (!MemoryUtil.getCrafting(maid).isGatheringDispatched()) return false;
        if (thrown != null) return true;
        return target != null && list != null && index < list.size();
    }

    @Override
    protected void tick(ServerLevel p_22551_, EntityMaid maid, long p_22553_) {
        if (!breath.breathTick(maid)) return;
        if (thrown == null) {
            ItemStack toSeekItem = list.get(index);
            if (toSeekItem.getCount() > toSeekItem.getMaxStackSize()) {
                toSeekItem = toSeekItem.copyWithCount(toSeekItem.getMaxStackSize());
            }
            ItemStack gotItem = InvUtil.tryExtractForCrafting(target.getAvailableInv(false), toSeekItem);
            if (!gotItem.isEmpty()) {
                Vec3 targetDir = MathUtil.getFromToWithFriction(target, maid.getPosition(0));
                thrown = InvUtil.throwItemVirtual(maid, gotItem, targetDir);
                list.get(index).shrink(gotItem.getCount());
                if (list.get(index).isEmpty())
                    index++;
            } else {
                index++;
            }
        } else {
            InvUtil.pickUpVirtual(maid, thrown);
            if (!thrown.isAlive())
                thrown = null;
        }
    }

    @Override
    public void stop(ServerLevel level, EntityMaid maid, long gameTime) {
        MemoryUtil.getCrafting(maid).setGatheringDispatched(false);
        MemoryUtil.clearTarget(maid);
        MemoryUtil.clearTarget(target);
        MemoryUtil.clearPickUpItemTemp(maid);
    }
}