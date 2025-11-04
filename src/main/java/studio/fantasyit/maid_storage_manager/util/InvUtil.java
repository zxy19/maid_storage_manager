package studio.fantasyit.maid_storage_manager.util;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.Logger;
import studio.fantasyit.maid_storage_manager.entity.VirtualItemEntity;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageContext;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageInsertableContext;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class InvUtil {
    public static boolean hasAnyFree(IItemHandler container) {
        for (int i = 0; i < container.getSlots(); i++) {
            if (container.getStackInSlot(i).isEmpty())
                return true;
        }
        return false;
    }

    public static ItemStack tryPlace(IItemHandler container, ItemStack itemStack) {
        if (itemStack.isEmpty()) return itemStack;
        ItemStack restItem = itemStack.copy();
        for (int i = 0; i < container.getSlots(); i++) {
            if (ItemStack.isSameItemSameTags(container.getStackInSlot(i), restItem)) {
                restItem = container.insertItem(i, restItem, false);
                if (restItem.isEmpty()) break;
            }
        }
        for (int i = 0; i < container.getSlots(); i++) {
            if (container.isItemValid(i, itemStack)) {
                restItem = container.insertItem(i, restItem, false);
                if (restItem.isEmpty()) break;
            }
        }
        return restItem;
    }

    public static ItemStack tryPlace(IStorageContext container, ItemStack itemStack) {
        if (itemStack.isEmpty()) return itemStack;
        if (container instanceof IStorageInsertableContext isic) {
            return isic.insert(itemStack);
        }
        return itemStack;
    }

    public static ItemStack tryExtract(IItemHandler inv, ItemStack itemStack, ItemStackUtil.MATCH_TYPE matchTag) {
        int count = 0;
        int max = itemStack.getCount();
        for (int i = 0; i < inv.getSlots(); i++) {
            ItemStack stackInSlot = inv.getStackInSlot(i);
            if (ItemStackUtil.isSame(stackInSlot, itemStack, matchTag)) {
                int extractCurrent = Math.min(max - count, stackInSlot.getCount());
                ItemStack get = inv.extractItem(i, extractCurrent, false);
                count += get.getCount();
                if (count >= max) break;
            }
        }
        return itemStack.copyWithCount(count);
    }

    public static ItemStack tryExtractForCrafting(IItemHandler inv, ItemStack itemStack) {
        int count = 0;
        int max = itemStack.getCount();
        for (int i = 0; i < inv.getSlots(); i++) {
            ItemStack stackInSlot = inv.getStackInSlot(i);
            if (ItemStackUtil.isSameInCrafting(stackInSlot, itemStack)) {
                int extractCurrent = Math.min(max - count, stackInSlot.getCount());
                ItemStack get = inv.extractItem(i, extractCurrent, false);
                count += get.getCount();
                if (count >= max) break;
            }
        }
        return itemStack.copyWithCount(count);
    }

    public static int maxCanPlace(IItemHandler container, ItemStack itemStack) {
        int count = 0;
        ItemStack testStack = itemStack.copyWithCount(itemStack.getMaxStackSize());
        for (int i = 0; i < container.getSlots(); i++) {
            if (container.isItemValid(i, itemStack)) {
                @NotNull ItemStack rest = container.insertItem(i, testStack, true);
                if (rest.isEmpty())
                    count += itemStack.getMaxStackSize();
                else
                    count += itemStack.getMaxStackSize() - rest.getCount();
            }
        }
        return count;
    }

    public static boolean isEmpty(CombinedInvWrapper availableInv) {
        for (int i = 0; i < availableInv.getSlots(); i++) {
            if (!availableInv.getStackInSlot(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public static List<ItemStack> forSlotMatches(IItemHandler container, Predicate<ItemStack> matches) {
        List<ItemStack> list = new ArrayList<>();
        for (int i = 0; i < container.getSlots(); i++) {
            ItemStack stackInSlot = container.getStackInSlot(i);
            if (matches.test(stackInSlot)) {
                list.add(stackInSlot);
            }
        }
        return list;
    }

    public static int freeSlots(IItemHandler availableInv) {
        int count = 0;
        for (int i = 0; i < availableInv.getSlots(); i++) {
            if (availableInv.getStackInSlot(i).isEmpty()) {
                count++;
            }
        }
        return count;
    }

    public static ItemEntity throwItem(EntityMaid maid, ItemStack itemStack) {
        Vec3 direction = Vec3.directionFromRotation(maid.getXRot(), maid.getYRot()).normalize().scale(0.5);
        return throwItem(maid, itemStack, direction);
    }

    public static ItemEntity throwItem(EntityMaid maid, ItemStack itemStack, Vec3 direction) {
        return throwItem(maid, itemStack, direction, false);
    }

    public static ItemEntity throwItem(EntityMaid maid, ItemStack itemStack, Vec3 direction, boolean noPickUpDelay) {
        Level level = maid.level();
        ItemEntity itementity = new ItemEntity(level, maid.getX(), maid.getY(), maid.getZ(), itemStack);
        maid.getMaxHeadXRot();
        itementity.setDeltaMovement(direction);
        itementity.setUnlimitedLifetime();
        if (noPickUpDelay) {
            itementity.setNoPickUpDelay();
        }
        level.addFreshEntity(itementity);
        return itementity;
    }

    public static VirtualItemEntity throwItemVirtual(EntityMaid maid, ItemStack itemStack, Vec3 direction) {
        if (MemoryUtil.getCrafting(maid).hasPlan() && MemoryUtil.getCrafting(maid).plan().isMaster()) {
            Logger.debug("[II]throw %s %d", itemStack.getItem(), itemStack.getCount());
        }
        Level level = maid.level();
        VirtualItemEntity itementity = VirtualItemEntity.create(level, maid.position(), itemStack);
        maid.getMaxHeadXRot();
        itementity.setDeltaMovement(direction);
        level.addFreshEntity(itementity);
        return itementity;
    }

    public static void pickUpVirtual(EntityMaid maid, VirtualItemEntity itemEntity) {
        ItemStack itemStack = itemEntity.getItem();
        CombinedInvWrapper availableInv = maid.getAvailableInv(true);
        ItemStack rest = InvUtil.tryPlace(availableInv, itemStack);
        if (MemoryUtil.getCrafting(maid).hasPlan() && MemoryUtil.getCrafting(maid).plan().isMaster()) {
            Logger.debug("[II]pickup %s %d", itemStack.getItem(), itemStack.getCount() - rest.getCount());
        }
        if (rest.isEmpty()) {
            ((ServerLevel) maid.level()).getChunkSource().broadcast(itemEntity, new ClientboundTakeItemEntityPacket(itemEntity.getId(), maid.getId(), 1));
            itemEntity.discard();
        } else
            itemEntity.setItem(rest);
    }

    public static int getTargetIndex(EntityMaid maid, ItemStack itemStack, boolean matchTag) {
        CombinedInvWrapper inv = maid.getAvailableInv(true);
        for (int i = 0; i < inv.getSlots(); i++) {
            if (ItemStackUtil.isSame(inv.getStackInSlot(i), itemStack, matchTag)) {
                return i;
            }
        }
        return -1;
    }

    public static int getTargetIndexInCrafting(EntityMaid maid, ItemStack itemStack, int skip) {
        return getTargetIndexInCrafting(maid, itemStack, skip, -1);
    }

    public static int getTargetIndexInCrafting(EntityMaid maid, ItemStack itemStack, int skip, int except) {
        CombinedInvWrapper inv = maid.getAvailableInv(true);
        for (int i = skip; i < inv.getSlots(); i++) {
            if (i == except)
                continue;
            if (itemStack.isEmpty() && inv.getStackInSlot(i).isEmpty()) {
                return i;
            }
            if (!itemStack.isEmpty() && ItemStackUtil.isSameInCrafting(inv.getStackInSlot(i), itemStack)) {
                return i;
            }
        }
        return -1;
    }

    public static void swapHandAndSlot(EntityMaid maid, InteractionHand hand, int slot) {
        CombinedInvWrapper inv = maid.getAvailableInv(true);
        ItemStack handItem = maid.getItemInHand(hand);
        maid.setItemInHand(hand, inv.getStackInSlot(slot));
        inv.setStackInSlot(slot, handItem);
    }

    public static void mergeSameStack(IItemHandler inv) {
        for (int i = inv.getSlots() - 1; i >= 0; i--) {
            ItemStack stackInSlot = inv.getStackInSlot(i);
            if (!stackInSlot.isEmpty()) {
                for (int j = 0; j < i; j++) {
                    ItemStack stackInSlot1 = inv.getStackInSlot(j);
                    ItemStack rest = inv.insertItem(i, stackInSlot1, false);
                    if (rest.getCount() != stackInSlot1.getCount()) {
                        inv.extractItem(j, stackInSlot1.getCount() - rest.getCount(), false);
                    }
                }
            }
        }
    }

    public static boolean hasItem(IItemHandler inv, Item item) {
        for (int i = 0; i < inv.getSlots(); i++) {
            ItemStack stackInSlot = inv.getStackInSlot(i);
            if (!stackInSlot.isEmpty() && stackInSlot.getItem() == item) {
                return true;
            }
        }
        return false;
    }
}
