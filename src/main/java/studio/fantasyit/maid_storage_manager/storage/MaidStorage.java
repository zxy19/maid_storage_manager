package studio.fantasyit.maid_storage_manager.storage;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.storage.ItemHandler.ItemHandlerStorage;
import studio.fantasyit.maid_storage_manager.storage.ae2.Ae2Storage;
import studio.fantasyit.maid_storage_manager.storage.base.IMaidStorage;

import javax.swing.text.html.parser.Entity;
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

        list.add(new ItemHandlerStorage());
        if (ModList.get().isLoaded("ae2") && Config.enableAe2Sup) {
            list.add(new Ae2Storage());
        }

        CollectStorageEvent event = new CollectStorageEvent(list);
        MinecraftForge.EVENT_BUS.post(event);
        this.storages = event.getStorages();
    }


    public @Nullable Storage isValidTarget(ServerLevel level, LivingEntity maid, BlockPos block) {
        return isValidTarget(level, maid, block, null);
    }

    public @Nullable Storage isValidTarget(ServerLevel level, LivingEntity maid, BlockPos block, Direction side) {
        for (IMaidStorage storage : storages) {
            if (storage.isValidTarget(level, maid, block, side)) {
                return new Storage(storage.getType(), block, side);
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
}
