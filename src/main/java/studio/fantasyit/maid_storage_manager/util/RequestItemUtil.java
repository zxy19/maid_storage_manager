package studio.fantasyit.maid_storage_manager.util;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageContext;

import java.util.List;
import java.util.UUID;

public class RequestItemUtil {
    public static void stopJobAndStoreOrThrowItem(EntityMaid maid, @Nullable IStorageContext storeTo, @Nullable Entity targetEntity) {
        Level level = maid.level();
        ItemStack reqList = maid.getMainHandItem();
        CompoundTag tag = reqList.getOrCreateTag();
        if (tag.getBoolean(RequestListItem.TAG_VIRTUAL)) {
            //虚拟的，不用处理
        }
        //1.1 尝试扔给目标实体
        else if (tag.getInt(RequestListItem.TAG_REPEAT_INTERVAL) <= 0 && targetEntity != null) {
            Vec3 targetDir = MathUtil.getFromToWithFriction(maid.position(), targetEntity.position(), 0.6);
            tag.putBoolean(RequestListItem.TAG_IGNORE_TASK, true);
            reqList.setTag(tag);
            InvUtil.throwItem(maid, reqList, targetDir, true);
            //因为扔出去会被女仆秒捡起，添加一个CD
            MemoryUtil.setReturnToScheduleAt(maid, level.getServer().getTickCount() + 40);
        }
        //1.2 尝试放入指定位置。例外：如果有循环请求任务，那么不会存入目标容器.
        else if (tag.getInt(RequestListItem.TAG_REPEAT_INTERVAL) > 0 || storeTo == null || !InvUtil.tryPlace(storeTo, reqList).isEmpty()) {
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

    public static ItemStack makeVirtualItemStack(List<ItemStack> list, @Nullable Target target, @Nullable Entity targetEntity) {
        ItemStack itemStack = ItemRegistry.REQUEST_LIST_ITEM.get().getDefaultInstance().copy();
        CompoundTag tag = itemStack.getOrCreateTag();
        tag.putBoolean(RequestListItem.TAG_VIRTUAL, true);
        ListTag listTag = new ListTag();
        for (int i = 0; i < 10; i++) {
            ItemStack item = i < list.size() ? list.get(i) : ItemStack.EMPTY;
            CompoundTag tmp = new CompoundTag();
            tmp.putInt(RequestListItem.TAG_ITEMS_REQUESTED, item.getCount());
            tmp.put(RequestListItem.TAG_ITEMS_ITEM, item.copyWithCount(1).save(new CompoundTag()));
            listTag.add(tmp);
        }
        tag.put(RequestListItem.TAG_ITEMS, listTag);
        tag.putBoolean(RequestListItem.TAG_BLACKMODE, false);
        tag.putBoolean(RequestListItem.TAG_STOCK_MODE, false);
        tag.putBoolean(RequestListItem.TAG_IGNORE_TASK, true);
        tag.putBoolean(RequestListItem.TAG_MATCH_TAG, false);
        tag.putInt(RequestListItem.TAG_REPEAT_INTERVAL, 0);
        if (target != null) {
            tag.put(RequestListItem.TAG_STORAGE, target.toNbt());
        } else if (targetEntity != null) {
            tag.putUUID(RequestListItem.TAG_STORAGE_ENTITY, targetEntity.getUUID());
        }
        tag.putUUID(RequestListItem.TAG_UUID, UUID.randomUUID());
        itemStack.setTag(tag);
        return itemStack;
    }
}
