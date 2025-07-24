package studio.fantasyit.maid_storage_manager.event;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;

import java.util.List;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME, modid = MaidStorageManager.MODID)
public class TooltipEvent {
    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        List<Component> toolTip = event.getToolTip();
        if (event.getItemStack().is(ItemRegistry.INVENTORY_LIST.get())) {
            toolTip.add(Component.translatable("tooltip.maid_storage_manager.inventory_list.desc").withStyle(ChatFormatting.GRAY));
        }
    }
}
