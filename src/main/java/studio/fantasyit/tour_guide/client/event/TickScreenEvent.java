package studio.fantasyit.tour_guide.client.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.tour_guide.client.ClientItemTourGuideCounter;

@Mod.EventBusSubscriber(modid = MaidStorageManager.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class TickScreenEvent {
    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if (Minecraft.getInstance().screen instanceof AbstractContainerScreen<?> s && s.getSlotUnderMouse() != null) {
            ClientItemTourGuideCounter.updateHoveredItem(s.getSlotUnderMouse().getItem().getItem());
        } else {
            ClientItemTourGuideCounter.updateHoveredItem(null);
        }
    }
}
