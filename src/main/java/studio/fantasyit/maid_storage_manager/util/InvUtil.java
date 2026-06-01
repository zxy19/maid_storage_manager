package studio.fantasyit.maid_storage_manager.util;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.Logger;
import studio.fantasyit.maid_storage_manager.entity.VirtualItemEntity;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageContext;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageInsertableContext;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class InvUtil {
    public static boolean hasAnyFree(ResourceHandler<ItemResource> container) {
        for (int i = 0; i < container.size(); i++) {
            if (container.getResource(i).isEmpty())
                return true;
        }
        return false;
    }

    public static ItemStack tryPlace(ResourceHandler<ItemResource> container, ItemStack itemStack) {
        if (itemStack.isEmpty()) return itemStack;
        return ItemUtil.insertItemReturnRemaining(container, itemStack, false, null);
    }

    public static ItemStack tryPlace(IStorageContext container, ItemStack itemStack) {
        if (itemStack.isEmpty()) return itemStack;
        if (container instanceof IStorageInsertableContext isic) {
            return isic.insert(itemStack);
        }
        return itemStack;
    }

    public static ItemStack tryExtract(ResourceHandler<ItemResource> inv, ItemStack itemStack, ItemStackUtil.MATCH_TYPE matchTag) {
        int count = 0;
        int max = itemStack.getCount();
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stackInSlot = ItemUtil.getStack(inv, i);
            if (ItemStackUtil.isSame(stackInSlot, itemStack, matchTag)) {
                int extractCurrent = Math.min(max - count, stackInSlot.getCount());
                try (var tx = Transaction.open(null)) {
                    int extracted = inv.extract(i, ItemResource.of(stackInSlot), extractCurrent, tx);
                    tx.commit();
                    count += extracted;
                }
                if (count >= max) break;
            }
        }
        return itemStack.copyWithCount(count);
    }

    public static ItemStack tryExtractForCrafting(ResourceHandler<ItemResource> inv, ItemStack itemStack) {
        int count = 0;
        int max = itemStack.getCount();
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stackInSlot = ItemUtil.getStack(inv, i);
            if (ItemStackUtil.isSameInCrafting(stackInSlot, itemStack)) {
                int extractCurrent = Math.min(max - count, stackInSlot.getCount());
                try (var tx = Transaction.open(null)) {
                    int extracted = inv.extract(i, ItemResource.of(stackInSlot), extractCurrent, tx);
                    tx.commit();
                    count += extracted;
                }
                if (count >= max) break;
            }
        }
        return itemStack.copyWithCount(count);
    }

    public static int maxCanPlace(ResourceHandler<ItemResource> container, ItemStack itemStack) {
        int count = 0;
        ItemStack testStack = itemStack.copyWithCount(itemStack.getMaxStackSize());
        for (int i = 0; i < container.size(); i++) {
            if (container.isValid(i, ItemResource.of(itemStack))) {
                @NotNull ItemStack rest = ItemUtil.insertItemReturnRemaining(container, i, testStack, true, null);
                if (rest.isEmpty())
                    count += itemStack.getMaxStackSize();
                else
                    count += itemStack.getMaxStackSize() - rest.getCount();
            }
        }
        return count;
    }

    public static List<ItemStack> forSlotMatches(ResourceHandler<ItemResource> container, Predicate<ItemStack> matches) {
        List<ItemStack> list = new ArrayList<>();
        for (int i = 0; i < container.size(); i++) {
            ItemStack stackInSlot = ItemUtil.getStack(container, i);
            if (matches.test(stackInSlot)) {
                list.add(stackInSlot);
            }
        }
        return list;
    }

    public static boolean isEmpty(ResourceHandler<ItemResource> availableInv) {
        for (int i = 0; i < availableInv.size(); i++) {
            if (!availableInv.getResource(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public static int freeSlots(ResourceHandler<ItemResource> availableInv) {
        int count = 0;
        for (int i = 0; i < availableInv.size(); i++) {
            if (availableInv.getResource(i).isEmpty()) {
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
        ResourceHandler<ItemResource> availableInv = maid.getAvailableInv(true);
        ItemStack rest = InvUtil.tryPlace(availableInv, itemStack);
        if (MemoryUtil.getCrafting(maid).hasPlan() && MemoryUtil.getCrafting(maid).plan().isMaster()) {
            Logger.debug("[II]pickup %s %d", itemStack.getItem(), itemStack.getCount() - rest.getCount());
        }
        if (rest.isEmpty()) {
            // broadcast disabled - API changed
            // ((ServerLevel) maid.level()).getChunkSource().broadcast(itemEntity, new ClientboundTakeItemEntityPacket(itemEntity.getId(), maid.getId(), 1));
            itemEntity.discard();
        } else
            itemEntity.setItem(rest);
    }

    public static int getTargetIndex(EntityMaid maid, ItemStack itemStack, boolean matchTag) {
        ResourceHandler<ItemResource> inv = maid.getAvailableInv(true);
        for (int i = 0; i < inv.size(); i++) {
            if (ItemStackUtil.isSame(ItemUtil.getStack(inv, i), itemStack, matchTag)) {
                return i;
            }
        }
        return -1;
    }

    public static int getTargetIndexInCrafting(EntityMaid maid, ItemStack itemStack, int skip) {
        return getTargetIndexInCrafting(maid, itemStack, skip, -1);
    }

    public static int getTargetIndexInCrafting(EntityMaid maid, ItemStack itemStack, int skip, int except) {
        ResourceHandler<ItemResource> inv = maid.getAvailableInv(true);
        for (int i = skip; i < inv.size(); i++) {
            if (i == except)
                continue;
            if (itemStack.isEmpty() && inv.getResource(i).isEmpty()) {
                return i;
            }
            if (!itemStack.isEmpty() && ItemStackUtil.isSameInCrafting(ItemUtil.getStack(inv, i), itemStack)) {
                return i;
            }
        }
        return -1;
    }

    public static void swapHandAndSlot(EntityMaid maid, InteractionHand hand, int slot) {
        ResourceHandler<ItemResource> inv = maid.getAvailableInv(true);
        ItemStack handItem = maid.getItemInHand(hand);
        ItemStack slotItem = ItemUtil.getStack(inv, slot);
        maid.setItemInHand(hand, slotItem);
        try (var tx = Transaction.open(null)) {
            inv.extract(slot, ItemResource.of(slotItem), slotItem.getCount(), tx);
            if (!handItem.isEmpty()) {
                int inserted = inv.insert(slot, ItemResource.of(handItem), handItem.getCount(), tx);
                if (inserted < handItem.getCount()) {
                    inv.insert(ItemResource.of(handItem), handItem.getCount() - inserted, tx);
                }
            }
            tx.commit();
        }
    }

    public static void mergeSameStack(ResourceHandler<ItemResource> inv) {
        for (int i = inv.size() - 1; i >= 0; i--) {
            ItemStack stackInSlot = ItemUtil.getStack(inv, i);
            if (!stackInSlot.isEmpty()) {
                for (int j = 0; j < i; j++) {
                    ItemStack stackInSlot1 = ItemUtil.getStack(inv, j);
                    if (!ItemStack.isSameItemSameComponents(stackInSlot, stackInSlot1)) continue;
                    try (var tx = Transaction.open(null)) {
                        int inserted = inv.insert(i, ItemResource.of(stackInSlot1), stackInSlot1.getCount(), tx);
                        if (inserted > 0) {
                            inv.extract(j, ItemResource.of(stackInSlot1), inserted, tx);
                        }
                        tx.commit();
                    }
                }
            }
        }
    }

    public static boolean hasItem(ResourceHandler<ItemResource> inv, Item item) {
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stackInSlot = ItemUtil.getStack(inv, i);
            if (!stackInSlot.isEmpty() && stackInSlot.getItem() == item) {
                return true;
            }
        }
        return false;
    }
}
