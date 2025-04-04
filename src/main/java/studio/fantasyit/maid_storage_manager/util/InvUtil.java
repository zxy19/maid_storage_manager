package studio.fantasyit.maid_storage_manager.util;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageContext;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageInsertableContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
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

    public static ItemStack tryExtract(IItemHandler inv, ItemStack itemStack) {
        int count = 0;
        int max = itemStack.getCount();
        for (int i = 0; i < inv.getSlots(); i++) {
            ItemStack stackInSlot = inv.getStackInSlot(i);
            if (ItemStack.isSameItemSameTags(stackInSlot, itemStack)) {
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

    public static TagKey<Block> allowTag = TagKey.create(ForgeRegistries.BLOCKS.getRegistryKey(), new ResourceLocation(MaidStorageManager.MODID, "mb_chests"));

    public static void checkNearByContainers(ServerLevel level, BlockPos pos, Consumer<BlockPos> consumer) {
        BlockState blockState = level.getBlockState(pos);
        if (!blockState.is(allowTag)) {
            return;
        }
        BlockEntity blockEntity1 = level.getBlockEntity(pos);
        if (blockEntity1 == null) return;
        @NotNull LazyOptional<IItemHandler> optCap = blockEntity1.getCapability(ForgeCapabilities.ITEM_HANDLER);
        if (!optCap.isPresent()) return;
        IItemHandler inv = optCap.orElseThrow(RuntimeException::new);
        ItemStack itemStack = inv.extractItem(0, inv.getStackInSlot(0).getCount(), false);
        ItemStack markItem = Items.STICK.getDefaultInstance().copyWithCount(1);
        CompoundTag tag = markItem.getOrCreateTag();
        tag.putUUID("uuid", UUID.randomUUID());
        markItem.setTag(tag);
        inv.insertItem(0, markItem, false);
        PosUtil.findAroundUpAndDown(pos, blockPos -> {
            if (blockPos.equals(pos)) return null;
            BlockEntity blockEntity = level.getBlockEntity(blockPos);
            if (blockEntity != null) {
                blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(itemHandler -> {
                    if (ItemStack.isSameItemSameTags(itemHandler.getStackInSlot(0), markItem)) {
                        consumer.accept(blockPos);
                    }
                });
            }
            return null;
        }, 1);

        inv.extractItem(0, markItem.getCount(), false);
        if (itemStack.getCount() > 0) {
            inv.insertItem(0, itemStack, false);
        }
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

    public static void throwItem(EntityMaid maid, ItemStack itemStack) {
        Level level = maid.level();
        ItemEntity itementity = new ItemEntity(level, maid.getX(), maid.getY(), maid.getZ(), itemStack);
        maid.getMaxHeadXRot();
        Vec3 direction = Vec3.directionFromRotation(maid.getXRot(), maid.getYRot()).normalize().scale(0.5);
        itementity.setDeltaMovement(direction);
        itementity.setUnlimitedLifetime();
        level.addFreshEntity(itementity);
    }
}
