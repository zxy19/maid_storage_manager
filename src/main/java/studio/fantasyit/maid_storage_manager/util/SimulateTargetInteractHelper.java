package studio.fantasyit.maid_storage_manager.util;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.registry.MemoryModuleRegistry;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

public class SimulateTargetInteractHelper {
    public static ConcurrentMap<BlockPos, Integer> counter = new ConcurrentHashMap<>();
    final ServerLevel level;
    final EntityMaid maid;
    final public BlockPos target;
    @Nullable
    final BlockEntity blockEntity;
    @Nullable
    public IItemHandler itemHandler;
    public final IItemHandler maidInv;
    final Player opener;
    int currentSlot = 0;
    int restTick = 0;

    public SimulateTargetInteractHelper(EntityMaid maid, BlockPos targetPos, ServerLevel level) {
        this.maid = maid;
        this.maidInv = maid.getAvailableBackpackInv();
        this.target = targetPos;
        this.level = level;
        this.blockEntity = level.getBlockEntity(target);
        this.opener = ChestOpener.getOrCreate(level, maid);
        if (blockEntity != null) {
            LazyOptional<IItemHandler> capability = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER);
            if (capability.isPresent()) {
                itemHandler = capability.orElseThrow(RuntimeException::new);
            }
        }
    }


    private Optional<ContainerOpenersCounter> trySeekCounter(BlockEntity blockEntity) {
        Field[] fields = blockEntity.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getType().equals(ContainerOpenersCounter.class)) {
                try {
                    field.setAccessible(true);
                    return Optional.of((ContainerOpenersCounter) field.get(blockEntity));
                } catch (IllegalAccessException ignore) {
                }
            }
        }
        return Optional.empty();
    }

    public boolean doneTaking() {
        if (restTick > 0) return false;
        return itemHandler == null || currentSlot >= itemHandler.getSlots();
    }

    public boolean doneViewing() {
        return doneTaking();
    }

    public boolean donePlacing() {
        if (restTick > 0) return false;
        return itemHandler == null || currentSlot >= maidInv.getSlots();
    }

    public void open() {
        if (blockEntity == null) return;
        maid.swing(InteractionHand.MAIN_HAND);
        trySeekCounter(blockEntity).ifPresent(containerOpenersCounter -> {
            containerOpenersCounter.incrementOpeners(opener,
                    level,
                    target,
                    level.getBlockState(target));
        });
        currentSlot = 0;
        restTick = 20;
        counter.put(target, counter.getOrDefault(target, 0) + 1);
    }

    private void tickCommon() {
        if (restTick > 0) {
            restTick--;
        }
    }

    public void takeItemTick(ToTakenItemGetter cb) {
        tickCommon();
        if (restTick > 0) return;
        invTransTick(cb, itemHandler, maidInv);
    }


    public void placeItemTick(ToTakenItemGetter cb) {
        tickCommon();
        if (restTick > 0) return;
        invTransTick(cb, maidInv, itemHandler);
    }

    private void invTransTick(ToTakenItemGetter cb, IItemHandler invFrom, IItemHandler invTarg) {
        if (invFrom == null || invTarg == null)
            return;
        int count = 0;
        for (; currentSlot < invFrom.getSlots(); currentSlot++) {
            if (++count >= 10) break;
            //可以获取到的物品
            ItemStack copy = invFrom.extractItem(currentSlot,
                    invFrom.getStackInSlot(currentSlot).getCount(),
                    true).copy();
            if (copy.isEmpty()) continue;
            int maxStore = InvUtil.maxCanPlace(invTarg, copy);
            int toTake = cb.getToTakenItemCount(copy, invTarg, maxStore);
            if (toTake == 0) continue;
            ItemStack restNotPlaced = InvUtil.tryPlace(invTarg, copy.copyWithCount(toTake));
            invFrom.extractItem(currentSlot, toTake - restNotPlaced.getCount(), false);
            currentSlot++;
            break;
        }
    }

    public void viewItemTick(Consumer<ItemStack> cb) {
        tickCommon();
        if (restTick > 0) return;
        if (itemHandler == null) return;
        int count = 0;
        for (; currentSlot < itemHandler.getSlots(); currentSlot++) {
            if (++count >= 10) break;
            ItemStack stack = itemHandler.getStackInSlot(currentSlot);
            if (stack.isEmpty()) continue;
            cb.accept(stack);
        }
    }

    public void reset() {
        currentSlot = 0;
    }

    public void stop() {
        Brain<EntityMaid> brain = maid.getBrain();
        brain.eraseMemory(MemoryModuleRegistry.ARRIVE_TARGET.get());
        if (blockEntity != null && opener != null) {
            trySeekCounter(blockEntity).ifPresent(containerOpenersCounter -> {
                containerOpenersCounter.decrementOpeners(opener,
                        level,
                        target,
                        level.getBlockState(target));
            });
        }
        counter.put(target, Math.max(counter.getOrDefault(target, 0) - 1, 0));
    }

    @FunctionalInterface
    public interface ToTakenItemGetter {
        int getToTakenItemCount(ItemStack itemStack, IItemHandler targetInv, int maxStore);
    }

    public static class ChestOpener extends FakePlayer {

        public static ConcurrentMap<UUID, ChestOpener> cache = new ConcurrentHashMap<>();

        public static ChestOpener getOrCreate(ServerLevel level, EntityMaid maid) {
            if (cache.containsKey(maid.getUUID())) {
                return cache.get(maid.getUUID());
            } else {
                ChestOpener opener = new ChestOpener(level, new GameProfile(UUID.randomUUID(), maid.getName().getString()));
                cache.put(maid.getUUID(), opener);
                return opener;
            }
        }

        public ChestOpener(ServerLevel level, GameProfile name) {
            super(level, name);
        }
    }
}
