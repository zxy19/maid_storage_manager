package studio.fantasyit.maid_storage_manager.storage;

import net.minecraftforge.eventbus.api.Event;
import studio.fantasyit.maid_storage_manager.storage.base.IMaidStorage;

import java.util.List;

public class CollectStorageEvent extends Event {
    private final List<IMaidStorage> storages;

    public CollectStorageEvent(List<IMaidStorage> storages){
        this.storages = storages;
    }
    public List<IMaidStorage> getStorages() {
        return storages;
    }
    public void addStorage(IMaidStorage storage){
        storages.add(storage);
    }
}
