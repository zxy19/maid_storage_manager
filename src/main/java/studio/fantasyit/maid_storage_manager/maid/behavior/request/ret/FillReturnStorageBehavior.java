package studio.fantasyit.maid_storage_manager.maid.behavior.request.ret;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.util.Conditions;
import studio.fantasyit.maid_storage_manager.util.InvUtil;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;
import studio.fantasyit.maid_storage_manager.util.SimulateTargetInteractHelper;

import java.util.Map;

public class FillReturnStorageBehavior extends Behavior<EntityMaid> {

    @Nullable SimulateTargetInteractHelper helper;

    public FillReturnStorageBehavior() {
        super(Map.of(), 10000000);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel p_22538_, EntityMaid maid) {
        if (!MemoryUtil.isWorkingRequest(maid)) return false;

        if (!Conditions.isTryToReturnStorage(maid)) return false;
        return Conditions.hasReachedValidTargetOrReset(maid);
    }

    @Override
    protected boolean canStillUse(ServerLevel p_22545_, EntityMaid p_22546_, long p_22547_) {
        return helper != null && !helper.donePlacing();
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long p_22542_) {
        BlockPos pos = RequestListItem.getStorageBlock(maid.getMainHandItem());
        if (pos == null) return;
        helper = new SimulateTargetInteractHelper(maid, pos, level);
        helper.open();
    }

    @Override
    protected void tick(ServerLevel p_22551_, EntityMaid maid, long p_22553_) {
        super.tick(p_22551_, maid, p_22553_);
        if (helper != null) {
            helper.placeItemTick((i, targetInv, maxStore) ->
                    RequestListItem.updateStoredItems(maid.getMainHandItem(), i, maxStore)
                            .getCount()
            );
        }
    }

    @Override
    protected void stop(@NotNull ServerLevel level, @NotNull EntityMaid maid, long p_22550_) {
        super.stop(level, maid, p_22550_);
        boolean done = Conditions.finishAll(maid) || Conditions.listAllDone(maid);
        if (helper != null)
            if (helper.itemHandler != null && Conditions.listAllStored(maid) && done) {
                if (!InvUtil.tryPlace(helper.itemHandler, maid.getMainHandItem()).isEmpty()) {
                    //需求清单未能正确插入目标容器
                    //设置清单为工作忽略模式，并尝试放置到背包中
                    ItemStack reqList = maid.getMainHandItem();
                    CompoundTag tag = reqList.getOrCreateTag();
                    tag.putBoolean(RequestListItem.TAG_IGNORE_TASK, true);
                    reqList.setTag(tag);
                    if (!InvUtil.tryPlace(maid.getAvailableBackpackInv(), reqList).isEmpty()) {
                        //背包也没空。。扔地上站未来
                        ItemEntity itementity = new ItemEntity(level, maid.getX(), maid.getY(), maid.getZ(), reqList);
                        maid.getMaxHeadXRot();
                        Vec3 direction = Vec3.directionFromRotation(maid.getXRot(), maid.getYRot()).normalize().scale(0.5);
                        itementity.setDeltaMovement(direction);
                        itementity.setUnlimitedLifetime();
                        level.addFreshEntity(itementity);

                    }
                }
                maid.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                MemoryUtil.setWorkingRequest(maid, false);
            }
        if (helper != null)
            helper.stop();
        MemoryUtil.clearReturnToStorage(maid);
        MemoryUtil.clearPosition(maid);
    }
}