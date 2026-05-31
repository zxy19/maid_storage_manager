package studio.fantasyit.maid_storage_manager.integration.kubejs;

import dev.latvian.mods.kubejs.script.ScriptType;
import studio.fantasyit.maid_storage_manager.craft.CollectCraftEvent;
import studio.fantasyit.maid_storage_manager.integration.kubejs.event.KJSCraftEvent;

public class KJSEventPort {
    public static void postCraftCollect(CollectCraftEvent event) {
        KJSCraftEvent kjsCraftEvent = new KJSCraftEvent(event);
        KJSRegEvent.CRAFT.post(ScriptType.STARTUP, kjsCraftEvent);
    }
}