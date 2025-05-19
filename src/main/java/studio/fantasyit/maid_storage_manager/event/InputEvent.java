package studio.fantasyit.maid_storage_manager.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.network.ClientInputPacket;
import studio.fantasyit.maid_storage_manager.network.Network;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;

@Mod.EventBusSubscriber(modid = MaidStorageManager.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class InputEvent {
    @SubscribeEvent
    public static void onScroll(net.minecraftforge.client.event.InputEvent.MouseScrollingEvent event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        ItemStack itemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if(player.isShiftKeyDown()) {
            if (itemStack.is(ItemRegistry.CRAFT_GUIDE.get()) || itemStack.is(ItemRegistry.STORAGE_DEFINE_BAUBLE.get()) || itemStack.is(ItemRegistry.LOGISTICS_GUIDE.get())) {
                event.setCanceled(true);
                Network.INSTANCE.send(PacketDistributor.SERVER.noArg(),
                        new ClientInputPacket(ClientInputPacket.Type.SCROLL, (int) (event.getScrollDelta() * 100))
                );
            }
        }
    }
}
