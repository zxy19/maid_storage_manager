package studio.fantasyit.maid_storage_manager.storage;

import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;
import studio.fantasyit.maid_storage_manager.storage.base.IMaidStorage;

import java.util.List;

public class CollectStorageEvent extends Event implements IModBusEvent {
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
