package studio.fantasyit.maid_storage_manager.items;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.menu.InventoryListScreen;
import studio.fantasyit.maid_storage_manager.network.Network;
import studio.fantasyit.maid_storage_manager.registry.DataComponentRegistry;

import java.util.List;

public class WrittenInvListItem extends Item {
    public static final String TAG_UUID = "uuid";
    public static final String TAG_AUTHOR = "author";
    public static final String TAG_TIME = "time";

    public WrittenInvListItem() {
        super(
                new Properties()
                        .stacksTo(1)
        );
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, @NotNull Player player, @NotNull InteractionHand p_41434_) {
        if (level.isClientSide) {
            ItemStack stack = player.getMainHandItem();
            if (stack.has(DataComponentRegistry.INVENTORY_UUID)) {
                Network.sendRequestListPacket(stack.get(DataComponentRegistry.INVENTORY_UUID));
                Minecraft.getInstance().setScreen(new InventoryListScreen(stack.get(DataComponentRegistry.INVENTORY_UUID)));
            }
            return InteractionResultHolder.consume(player.getItemInHand(p_41434_));
        } else {
            return InteractionResultHolder.pass(player.getItemInHand(p_41434_));
        }
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext p_339594_, List<Component> tooltip, TooltipFlag p_41424_) {
        super.appendHoverText(itemStack, p_339594_, tooltip, p_41424_);
        tooltip.add(Component.translatable("tooltip.maid_storage_manager.written_request_list.desc").withStyle(ChatFormatting.GRAY));
        if (itemStack.has(DataComponentRegistry.INVENTORY_AUTHOR))
            tooltip.add(Component.translatable(
                    "tooltip.maid_storage_manager.request_list.author",
                    itemStack.get(DataComponentRegistry.INVENTORY_AUTHOR)
            ));
        if (itemStack.has(DataComponentRegistry.INVENTORY_TIME))
            tooltip.add(Component.translatable(
                    "tooltip.maid_storage_manager.request_list.time",
                    getTimeStr(itemStack.get(DataComponentRegistry.INVENTORY_TIME))
            ));
    }

    private String getTimeStr(long aLong) {
        long day = aLong / (24000);
        long hour = (aLong - day * 24000) / 1000;
        long minute = (aLong - day * 24000 - hour * 1000) * 60 / 1000;
        return Component
                .translatable("tooltip.maid_storage_manager.request_list.time.str",
                        String.valueOf(day),
                        String.format("%02d", hour),
                        String.format("%02d", minute))
                .getString();
    }
}
