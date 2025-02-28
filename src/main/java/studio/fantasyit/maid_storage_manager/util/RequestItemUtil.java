package studio.fantasyit.maid_storage_manager.util;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageContext;

public class RequestItemUtil {
    public static void stopJobAndStoreOrThrowItem(EntityMaid maid, @Nullable IStorageContext storeTo) {
        Level level = maid.level();
        ItemStack reqList = maid.getMainHandItem();
        //1 尝试放入指定位置
        if (storeTo == null || !InvUtil.tryPlace(storeTo, reqList).isEmpty()) {
            //没能成功，尝试背包
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
        MemoryUtil.getRequestProgress(maid).clearTarget();
        MemoryUtil.getRequestProgress(maid).stopWork();
        maid.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
    }
}
