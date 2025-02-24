package studio.fantasyit.maid_storage_manager.util;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.Consumer;

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
            if (container.isItemValid(i, itemStack)) {
                restItem = container.insertItem(i, restItem, false);
                if (restItem.isEmpty()) break;
            }
        }
        return restItem;
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


    public static void checkNearByContainers(ServerLevel level, BlockPos pos, IItemHandler inv, Consumer<BlockPos> consumer) {
        ItemStack itemStack = inv.extractItem(0, inv.getStackInSlot(0).getCount(), false);
        ItemStack markItem = Items.STICK.getDefaultInstance().copyWithCount(1);
        CompoundTag tag = markItem.getOrCreateTag();
        tag.putUUID("uuid", UUID.randomUUID());
        markItem.setTag(tag);
        inv.insertItem(0, markItem, false);
        PosUtil.findAroundUpAndDown(pos, blockPos -> {
            if (blockPos.equals(pos)) return false;
            BlockEntity blockEntity = level.getBlockEntity(blockPos);
            if (blockEntity != null) {
                blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(itemHandler -> {
                    if (ItemStack.isSameItemSameTags(itemHandler.getStackInSlot(0), markItem)) {
                        consumer.accept(blockPos);
                    }
                });
            }
            return false;
        }, 1);

        inv.extractItem(0, markItem.getCount(), false);
        if (itemStack.getCount() > 0) {
            inv.insertItem(0, itemStack, false);
        }
    }
}
