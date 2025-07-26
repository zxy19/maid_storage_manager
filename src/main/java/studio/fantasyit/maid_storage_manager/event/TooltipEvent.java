package studio.fantasyit.maid_storage_manager.event;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.registry.DataComponentRegistry;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.List;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME, modid = MaidStorageManager.MODID)
public class TooltipEvent {
    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        List<Component> toolTip = event.getToolTip();
        if (event.getItemStack().is(ItemRegistry.INVENTORY_LIST.get())) {
            toolTip.add(Component.translatable("tooltip.maid_storage_manager.inventory_list.desc").withStyle(ChatFormatting.GRAY));
        }

        if (event.getItemStack().has(DataComponentRegistry.TO_SPAWN_ITEMS)) {
            List<ItemStack> itemStacks = event.getItemStack().get(DataComponentRegistry.TO_SPAWN_ITEMS);
            toolTip.add(Component.translatable("tooltip.maid_storage_manager.to_spawn_items.desc").withStyle(ChatFormatting.YELLOW));
            if (itemStacks != null) {
                int totalCount = 0;
                for (int i = 0; i < itemStacks.size(); i++) {
                    totalCount += itemStacks.get(i).getCount();
                    if (i < itemStacks.size() - 1 && ItemStackUtil.isSame(itemStacks.get(i), itemStacks.get(i + 1), true)) {
                        continue;
                    }
                    ItemStack itemStack = itemStacks.get(i);
                    toolTip.add(Component.translatable("tooltip.maid_storage_manager.to_spawn_items.line", itemStack.getDisplayName(), totalCount));
                    totalCount = 0;
                }
            }
        }
    }
}
