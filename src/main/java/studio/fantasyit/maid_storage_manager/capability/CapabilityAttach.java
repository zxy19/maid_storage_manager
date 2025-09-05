package studio.fantasyit.maid_storage_manager.capability;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;

@Mod.EventBusSubscriber(modid = MaidStorageManager.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CapabilityAttach {
    @SubscribeEvent
    public static void register(RegisterCapabilitiesEvent event) {
        event.register(InventoryListDataProvider.InventoryListData.class);
    }

    @Mod.EventBusSubscriber(modid = MaidStorageManager.MODID)
    public static class ForgeBus {
        @SubscribeEvent
        public static void attachCapabilitiesLevel(AttachCapabilitiesEvent<Level> event) {
            if (event.getObject() instanceof ServerLevel level) {
                if (level.dimension().location().equals(new ResourceLocation("minecraft", "overworld"))) {
                    event.addCapability(new ResourceLocation(MaidStorageManager.MODID, "inventory_list"),
                            new InventoryListDataProvider());
                    event.addCapability(new ResourceLocation(MaidStorageManager.MODID, "maid_persist"), new MaidItemPersistDataProvider());
                }
                event.addCapability(new ResourceLocation(MaidStorageManager.MODID, "block_occupy"), new CraftBlockOccupyDataProvider());
            }
        }
    }
}
