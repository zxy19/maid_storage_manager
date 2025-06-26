package studio.fantasyit.maid_storage_manager.integration.kubejs;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import studio.fantasyit.maid_storage_manager.integration.kubejs.event.KJSCraftEvent;

public class KJSRegEvent {
    static EventGroup group = EventGroup.of("MaidStorageManagerEvents");
    static EventHandler CRAFT = group.startup("craft", () -> KJSCraftEvent.class);
}