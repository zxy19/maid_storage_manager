package studio.fantasyit.maid_storage_manager.api.event;

import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import studio.fantasyit.maid_storage_manager.communicate.data.ConfigurableCommunicateData;

import java.util.Map;
import java.util.Optional;

public class CollectCommunicateDataEvent extends Event implements IModBusEvent {
    private Map<Identifier, ConfigurableCommunicateData> data;
    private Map<Identifier, Component> translations;

    public CollectCommunicateDataEvent(Map<Identifier, ConfigurableCommunicateData> data, Map<Identifier, Component> translations) {
        this.data = data;
        this.translations = translations;
    }

    public void register(Identifier id, Component translation, ConfigurableCommunicateData data) {
        this.data.put(id, data);
        this.translations.put(id, translation);
    }

    public void register(Identifier id, ConfigurableCommunicateData data) {
        Optional<IMaidTask> task = TaskManager.findTask(id);
        task.ifPresent(t -> register(id, t.getName(), data));
    }

    public Map<Identifier, ConfigurableCommunicateData> getData() {
        return data;
    }

    public Map<Identifier, Component> getTranslations() {
        return translations;
    }
}
