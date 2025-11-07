package studio.fantasyit.maid_storage_manager.storage;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.ModLoader;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.data.ItemCount;
import studio.fantasyit.maid_storage_manager.integration.Integrations;
import studio.fantasyit.maid_storage_manager.maid.memory.ViewedInventoryMemory;
import studio.fantasyit.maid_storage_manager.storage.ItemHandler.ChestMultiBlockProcessor;
import studio.fantasyit.maid_storage_manager.storage.ItemHandler.ItemHandlerStorage;
import studio.fantasyit.maid_storage_manager.storage.ae2.Ae2Storage;
import studio.fantasyit.maid_storage_manager.storage.base.IMaidStorage;
import studio.fantasyit.maid_storage_manager.storage.base.IMultiBlockProcessor;
import studio.fantasyit.maid_storage_manager.storage.create.place.CreateChainConveyorStorage;
import studio.fantasyit.maid_storage_manager.storage.create.stock.CreateStockTickerStorage;
import studio.fantasyit.maid_storage_manager.storage.qio.QIOStorage;
import studio.fantasyit.maid_storage_manager.storage.rs.RSStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MaidStorage {
    public static MaidStorage instance = null;
    List<IMaidStorage> storages;
    List<IMultiBlockProcessor> processors;

    public static MaidStorage getInstance() {
        if (instance == null) {
            instance = new MaidStorage();
        }
        return instance;
    }

    public void collectStorage() {
        ArrayList<IMaidStorage> list = new ArrayList<>();
        List<IMultiBlockProcessor> processorList = new ArrayList<>();

        if (Integrations.ae2Storage()) {
            list.add(new Ae2Storage());
        }
        if (Integrations.rsStorage()) {
            list.add(new RSStorage());
        }
        if (Integrations.createStorage()) {
            list.add(new CreateStockTickerStorage());
            list.add(new CreateChainConveyorStorage());
        }
        if (Integrations.mekanismStorage()) {
            list.add(new QIOStorage());
        }
        list.add(new ItemHandlerStorage());

        processorList.add(new ChestMultiBlockProcessor());

        CollectStorageEvent event = new CollectStorageEvent(list, processorList);

        ModLoader.postEvent(event);
        this.storages = event.getStorages();
        this.processors = event.getMultiBlockStorageProcessors();
    }

    public @Nullable Target isValidTarget(ServerLevel level, LivingEntity maid, Target target) {
        BlockState blockState = level.getBlockState(target.pos);
        if (blockState.is(Blocks.AIR)) return null;
        BlockEntity blockEntity = level.getBlockEntity(target.pos);
        ResourceLocation type = target.getType();
        for (IMaidStorage storage : storages) {
            if (storage.getType().equals(type) && storage.isValidTarget(level, maid, target.pos, target.side, blockState, blockEntity))
                return target;
        }
        return null;
    }

    public @Nullable Target isValidTarget(ServerLevel level, LivingEntity maid, BlockPos block) {
        return isValidTarget(level, maid, block, null);
    }

    public @Nullable Target isValidTarget(ServerLevel level, LivingEntity maid, BlockPos block, Direction side) {
        BlockState blockState = level.getBlockState(block);
        if (blockState.isAir()) return null;
        BlockEntity blockEntity = level.getBlockEntity(block);
        for (IMaidStorage storage : storages) {
            if (storage.isValidTarget(level, maid, block, side, blockState, blockEntity)) {
                return new Target(storage.getType(), block, side);
            }
        }
        return null;
    }

    public @Nullable IMaidStorage getStorage(ResourceLocation type) {
        for (IMaidStorage storage : storages) {
            if (storage.getType().equals(type)) {
                return storage;
            }
        }
        return null;
    }

    public boolean isCraftGuideProvider(Target target, List<ItemCount> blockPos) {
        IMaidStorage storage = getStorage(target.type);
        if (storage == null) return false;
        return storage.isCraftGuideProvider(blockPos);
    }

    public boolean processSpecialMultiBlockStorage(Level level, BlockPos target, Consumer<BlockPos> processor) {
        for (var processorPair : processors) {
            if (processorPair.isValid(level, target, level.getBlockState(target))) {
                processorPair.process(level, target, processor);
                return true;
            }
        }
        return false;
    }
}
