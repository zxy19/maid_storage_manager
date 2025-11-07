package studio.fantasyit.maid_storage_manager.storage;

import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;
import studio.fantasyit.maid_storage_manager.storage.base.IMaidStorage;
import studio.fantasyit.maid_storage_manager.storage.base.IMultiBlockProcessor;

import java.util.List;

public class CollectStorageEvent extends Event implements IModBusEvent {
    private final List<IMaidStorage> storages;
    private final List<IMultiBlockProcessor> multiBlockStorageProcessors;

    public CollectStorageEvent(List<IMaidStorage> storages, List<IMultiBlockProcessor> multiBlockStorageProcessors) {
        this.storages = storages;
        this.multiBlockStorageProcessors = multiBlockStorageProcessors;
    }

    public List<IMaidStorage> getStorages() {
        return storages;
    }

    public void addStorage(IMaidStorage storage) {
        storages.add(storage);
    }

    public void addMultiBlockStorageProcessor(IMultiBlockProcessor processor){
        multiBlockStorageProcessors.add(processor);
    }
    public List<IMultiBlockProcessor> getMultiBlockStorageProcessors() {
        return multiBlockStorageProcessors;
    }
}
