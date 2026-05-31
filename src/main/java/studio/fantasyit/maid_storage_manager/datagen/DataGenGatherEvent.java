package studio.fantasyit.maid_storage_manager.datagen;

import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(value = Dist.CLIENT)
public class DataGenGatherEvent {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event) {
        ResourceManager rm = event.getResourceManager(PackType.SERVER_DATA);
        event.createProvider((a) -> new ModelGen(a, rm));
        event.createProvider(TagGenItem::new);
        event.createProvider(TagGenBlock::new);
    }
}
