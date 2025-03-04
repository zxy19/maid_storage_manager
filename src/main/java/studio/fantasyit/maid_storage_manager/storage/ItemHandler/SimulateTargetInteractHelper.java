package studio.fantasyit.maid_storage_manager.storage.ItemHandler;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class SimulateTargetInteractHelper {
    public static ConcurrentMap<BlockPos, Integer> counter = new ConcurrentHashMap<>();
    final ServerLevel level;
    final public BlockPos target;
    @Nullable
    final BlockEntity blockEntity;
    private final EntityMaid maid;
    @Nullable
    public IItemHandler itemHandler;
    final Player opener;
    int currentSlot = 0;
    int restTick = 0;

    public SimulateTargetInteractHelper(EntityMaid maid, BlockPos targetPos, @Nullable Direction side, ServerLevel level) {
        this.maid = maid;
        this.target = targetPos;
        this.level = level;
        this.blockEntity = level.getBlockEntity(target);
        this.opener = ChestOpener.getOrCreate(level, maid);
        if (blockEntity != null) {
            LazyOptional<IItemHandler> capability;
            if (side == null)
                capability = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER);
            else
                capability = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, side);

            if (capability.isPresent()) {
                itemHandler = capability.orElseThrow(RuntimeException::new);
            }
        }
    }

    protected boolean isStillValid() {
        if (blockEntity == null || itemHandler == null) return false;
        return !blockEntity.isRemoved();
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
        if (blockEntity instanceof WorldlyContainer wc) {
            return Optional.of(new ContainerOpenersCounter() {
                @Override
                public void decrementOpeners(Player p_155469_, Level p_155470_, BlockPos p_155471_, BlockState p_155472_) {
                    wc.stopOpen(p_155469_);
                }

                @Override
                public void incrementOpeners(Player p_155453_, Level p_155454_, BlockPos p_155455_, BlockState p_155456_) {
                    wc.startOpen(p_155453_);
                }

                @Override
                protected void onOpen(Level p_155460_, BlockPos p_155461_, BlockState p_155462_) {

                }

                @Override
                protected void onClose(Level p_155473_, BlockPos p_155474_, BlockState p_155475_) {
                }

                @Override
                protected void openerCountChanged(Level p_155463_, BlockPos p_155464_, BlockState p_155465_, int p_155466_, int p_155467_) {
                }

                @Override
                protected boolean isOwnContainer(Player p_155451_) {
                    return false;
                }
            });
        }
        return Optional.empty();
    }

    public boolean doneTaking() {
        if (restTick > 0) return false;
        if (!isStillValid()) return true;
        return itemHandler == null || currentSlot >= itemHandler.getSlots();
    }

    public boolean doneViewing() {
        return doneTaking();
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
        counter.put(target, counter.getOrDefault(target, 0) + 1);
    }

    public void takeItemTick(Function<ItemStack, ItemStack> cb) {
        if (itemHandler == null) return;
        int count = 0;
        for (; currentSlot < itemHandler.getSlots(); currentSlot++) {
            if (++count >= 10) break;
            //可以获取到的物品
            ItemStack copy = itemHandler.extractItem(currentSlot,
                    itemHandler.getStackInSlot(currentSlot).getCount(),
                    true).copy();
            int originalCount = copy.getCount();
            if (copy.isEmpty()) continue;
            //获取在处理后剩余的物品数量
            ItemStack result = cb.apply(copy);
            //如果没有变化，则跳过
            if (result.getCount() == originalCount) continue;
            itemHandler.extractItem(currentSlot, originalCount - result.getCount(), false);
            break;
        }
    }

    public void viewItemTick(Consumer<ItemStack> cb) {
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
        if (blockEntity != null && opener != null && isStillValid()) {
            trySeekCounter(blockEntity).ifPresent(containerOpenersCounter -> {
                containerOpenersCounter.decrementOpeners(opener,
                        level,
                        target,
                        level.getBlockState(target));
            });
        }
        counter.put(target, Math.max(counter.getOrDefault(target, 0) - 1, 0));
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
