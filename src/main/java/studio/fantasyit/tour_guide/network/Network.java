package studio.fantasyit.tour_guide.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;

public class Network {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MaidStorageManager.MODID, "tour_guide"),
            () -> PROTOCOL_VERSION,
            (v) -> true,
            (v) -> true
    );

    public static void registerMessage() {
        int i = 0;
        INSTANCE.registerMessage(i++,
                S2CUpdateTourGuideData.class,
                S2CUpdateTourGuideData::toNetwork,
                S2CUpdateTourGuideData::fromNetwork,
                S2CUpdateTourGuideData::handle
        );
        INSTANCE.registerMessage(i++,
                S2CSyncTriggerableItems.class,
                S2CSyncTriggerableItems::toNetwork,
                S2CSyncTriggerableItems::fromNetwork,
                S2CSyncTriggerableItems::handle
        );
        INSTANCE.registerMessage(i++,
                C2SRequestTriggerableItems.class,
                C2SRequestTriggerableItems::toNetwork,
                C2SRequestTriggerableItems::fromNetwork,
                C2SRequestTriggerableItems::handle
        );
        INSTANCE.registerMessage(i++,
                C2SStartTourGuide.class,
                C2SStartTourGuide::toNetwork,
                C2SStartTourGuide::fromNetwork,
                C2SStartTourGuide::handle
        );
        INSTANCE.registerMessage(i++,
                C2SInteractTourGuideData.class,
                C2SInteractTourGuideData::toNetwork,
                C2SInteractTourGuideData::fromNetwork,
                C2SInteractTourGuideData::handle
        );
        INSTANCE.registerMessage(i++,
                C2SClientTrigger.class,
                C2SClientTrigger::toNetwork,
                C2SClientTrigger::fromNetwork,
                C2SClientTrigger::handle
        );
    }


    @Mod.EventBusSubscriber(modid = MaidStorageManager.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.DEDICATED_SERVER)
    public static class Server {
        @SubscribeEvent
        public static void FMLClientSetupEvent(FMLDedicatedServerSetupEvent event) {
            registerMessage();
        }
    }

    @Mod.EventBusSubscriber(modid = MaidStorageManager.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class Client {
        @SubscribeEvent
        public static void FMLClientSetupEvent(FMLClientSetupEvent event) {
            registerMessage();
        }

    }
}
