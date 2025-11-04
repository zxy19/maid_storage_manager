package studio.fantasyit.maid_storage_manager.api.event;

import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;
import studio.fantasyit.maid_storage_manager.communicate.data.ConfigurableCommunicateData;

import java.util.Map;
import java.util.Optional;

public class CollectCommunicateDataEvent extends Event implements IModBusEvent {
    private Map<ResourceLocation, ConfigurableCommunicateData> data;
    private Map<ResourceLocation, Component> translations;

    public CollectCommunicateDataEvent(Map<ResourceLocation, ConfigurableCommunicateData> data, Map<ResourceLocation, Component> translations) {
        this.data = data;
        this.translations = translations;
    }

    public void register(ResourceLocation id, Component translation, ConfigurableCommunicateData data) {
        this.data.put(id, data);
        this.translations.put(id, translation);
    }

    public void register(ResourceLocation id, ConfigurableCommunicateData data) {
        Optional<IMaidTask> task = TaskManager.findTask(id);
        task.ifPresent(t -> register(id, t.getName(), data));
    }

    public Map<ResourceLocation, ConfigurableCommunicateData> getData() {
        return data;
    }

    public Map<ResourceLocation, Component> getTranslations() {
        return translations;
    }
}
