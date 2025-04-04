package studio.fantasyit.maid_storage_manager.util;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageContext;

public class RequestItemUtil {
    public static void stopJobAndStoreOrThrowItem(EntityMaid maid, @Nullable IStorageContext storeTo) {
        Level level = maid.level();
        ItemStack reqList = maid.getMainHandItem();
        CompoundTag tag = reqList.getOrCreateTag();
        //1 尝试放入指定位置。例外：如果有循环请求任务，那么不会存入目标容器.
        if (tag.getInt(RequestListItem.TAG_REPEAT_INTERVAL) > 0 || storeTo == null || !InvUtil.tryPlace(storeTo, reqList).isEmpty()) {
            //没能成功，尝试背包
            if (tag.getInt(RequestListItem.TAG_REPEAT_INTERVAL) > 0) {
                tag.putInt(RequestListItem.TAG_COOLING_DOWN, tag.getInt(RequestListItem.TAG_REPEAT_INTERVAL));
            } else {
                tag.putBoolean(RequestListItem.TAG_IGNORE_TASK, true);
            }
            reqList.setTag(tag);
            if (!InvUtil.tryPlace(maid.getAvailableInv(false), reqList).isEmpty()) {
                //背包也没空。。扔地上站未来
                InvUtil.throwItem(maid, reqList);
            }
        }
        MemoryUtil.getRequestProgress(maid).clearTarget();
        MemoryUtil.getRequestProgress(maid).stopWork();
        maid.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
    }

}
