package studio.fantasyit.maid_storage_manager.api.event;

import net.neoforged.bus.api.Event;
import studio.fantasyit.maid_storage_manager.api.communicate.data.CommunicateRequest;

public class CommunicateFinishEvent extends Event {
    public final CommunicateRequest request;
    public CommunicateFinishEvent(CommunicateRequest request) {
        this.request = request;
    }
}
