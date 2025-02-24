package studio.fantasyit.maid_storage_manager.event;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;

import java.util.List;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = MaidStorageManager.MODID)
public class TooltipEvent {
    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        List<Component> toolTip = event.getToolTip();
        if (event.getItemStack().is(ItemRegistry.INVENTORY_LIST.get())) {
            toolTip.add(Component.translatable("tooltip.maid_storage_manager.inventory_list.desc").withStyle(ChatFormatting.GRAY));
        }
    }
}
