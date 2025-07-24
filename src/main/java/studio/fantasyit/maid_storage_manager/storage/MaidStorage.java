package studio.fantasyit.maid_storage_manager.storage;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.integration.Integrations;
import studio.fantasyit.maid_storage_manager.maid.memory.ViewedInventoryMemory;
import studio.fantasyit.maid_storage_manager.storage.ItemHandler.ItemHandlerStorage;
import studio.fantasyit.maid_storage_manager.storage.ae2.Ae2Storage;
import studio.fantasyit.maid_storage_manager.storage.base.IMaidStorage;
import studio.fantasyit.maid_storage_manager.storage.create.place.CreateChainConveyorStorage;
import studio.fantasyit.maid_storage_manager.storage.create.stock.CreateStockTickerStorage;
import studio.fantasyit.maid_storage_manager.storage.qio.QIOStorage;
import studio.fantasyit.maid_storage_manager.storage.rs.RSStorage;

import java.util.ArrayList;
import java.util.List;

public class MaidStorage {
    public static MaidStorage instance = null;
    List<IMaidStorage> storages;

    public static MaidStorage getInstance() {
        if (instance == null) {
            instance = new MaidStorage();
        }
        return instance;
    }

    public void collectStorage() {
        ArrayList<IMaidStorage> list = new ArrayList<>();

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

        CollectStorageEvent event = new CollectStorageEvent(list);
        NeoForge.EVENT_BUS.post(event);
        this.storages = event.getStorages();
    }

    public @Nullable Target isValidTarget(ServerLevel level, LivingEntity maid, Target target) {
        ResourceLocation type = target.getType();
        for (IMaidStorage storage : storages) {
            if (storage.getType().equals(type) && storage.isValidTarget(level, maid, target.pos, target.side))
                return target;
        }
        return null;
    }

    public @Nullable Target isValidTarget(ServerLevel level, LivingEntity maid, BlockPos block) {
        return isValidTarget(level, maid, block, null);
    }

    public @Nullable Target isValidTarget(ServerLevel level, LivingEntity maid, BlockPos block, Direction side) {
        for (IMaidStorage storage : storages) {
            if (storage.isValidTarget(level, maid, block, side)) {
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

    public boolean isCraftGuideProvider(Target target, List<ViewedInventoryMemory.ItemCount> blockPos) {
        IMaidStorage storage = getStorage(target.type);
        if (storage == null) return false;
        return storage.isCraftGuideProvider(blockPos);
    }
}
