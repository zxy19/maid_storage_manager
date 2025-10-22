package studio.fantasyit.maid_storage_manager.communicate;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;

import java.util.Map;

public class CollectCommunicateDataEvent extends Event implements IModBusEvent {
    private Map<ResourceLocation, ConfigurableCommunicateData> data;

    public CollectCommunicateDataEvent(Map<ResourceLocation, ConfigurableCommunicateData> data) {
        this.data = data;
    }

    public void register(ResourceLocation id, ConfigurableCommunicateData data) {
        this.data.put(id, data);
    }

    public Map<ResourceLocation, ConfigurableCommunicateData> getData() {
        return data;
    }
}
