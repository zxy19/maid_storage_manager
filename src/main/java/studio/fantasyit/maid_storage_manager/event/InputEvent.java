package studio.fantasyit.maid_storage_manager.event;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent.MouseScrollingEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.integration.request.IngredientRequestClient;
import studio.fantasyit.maid_storage_manager.network.ClientInputPacket;
import studio.fantasyit.maid_storage_manager.network.Network;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.util.BoxRenderUtil;

@Mod.EventBusSubscriber(modid = MaidStorageManager.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class InputEvent {


    public static final Lazy<KeyMapping> KEY_REQUEST_INGREDIENT = Lazy.of(() -> new net.minecraft.client.KeyMapping(
            "key.maid_storage_manager.request_ingredient",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_ALT,
            "key.maid_storage_manager.category"
    ));
    public static final Lazy<KeyMapping> KEY_SEE_THROUGH_MARK_BOX = Lazy.of(() -> new net.minecraft.client.KeyMapping(
            "key.maid_storage_manager.see_through_mark_box",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_ALT,
            "key.maid_storage_manager.category"
    ));
    public static final Lazy<KeyMapping> KEY_ROLL_SPECIAL_MODE = Lazy.of(() -> new net.minecraft.client.KeyMapping(
            "key.maid_storage_manager.roll_special_mode",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_ALT,
            "key.maid_storage_manager.category"
    ));
    protected static boolean pressingSpecialKey = false;

    @Mod.EventBusSubscriber(modid = MaidStorageManager.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModBus {
        @SubscribeEvent
        public static void registerKeyMappings(final RegisterKeyMappingsEvent event) {
            event.register(KEY_REQUEST_INGREDIENT.get());
            event.register(KEY_SEE_THROUGH_MARK_BOX.get());
            event.register(KEY_ROLL_SPECIAL_MODE.get());
        }
    }

    @SubscribeEvent
    public static void onScroll(MouseScrollingEvent event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        ItemStack itemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (player.isShiftKeyDown()) {
            if (itemStack.is(ItemRegistry.CRAFT_GUIDE.get()) || itemStack.is(ItemRegistry.STORAGE_DEFINE_BAUBLE.get()) || itemStack.is(ItemRegistry.LOGISTICS_GUIDE.get()) || itemStack.is(ItemRegistry.PROGRESS_PAD.get())) {
                event.setCanceled(true);
                Network.INSTANCE.send(PacketDistributor.SERVER.noArg(),
                        new ClientInputPacket(ClientInputPacket.Type.SCROLL, (int) (event.getScrollDelta() * 100))
                );
            }
        } else if (pressingSpecialKey) {
            event.setCanceled(true);
            if (itemStack.is(ItemRegistry.CRAFT_GUIDE.get()) || itemStack.is(ItemRegistry.PROGRESS_PAD.get())) {
                Network.INSTANCE.send(PacketDistributor.SERVER.noArg(),
                        new ClientInputPacket(ClientInputPacket.Type.ALT_SCROLL, (int) (event.getScrollDelta() * 100))
                );
            }
        }
    }

    @SubscribeEvent
    public static void onKey(net.minecraftforge.client.event.InputEvent.Key event) {
        InputConstants.Key key = InputConstants.getKey(event.getKey(), event.getScanCode());
        if (KEY_REQUEST_INGREDIENT.get().getKey().equals(key)) {
            if (event.getAction() == GLFW.GLFW_PRESS) {
                IngredientRequestClient.keyPressed = true;
            } else if (event.getAction() == GLFW.GLFW_RELEASE) {
                IngredientRequestClient.keyPressed = false;
            }
        }
        if (KEY_SEE_THROUGH_MARK_BOX.get().getKey().equals(key)) {
            if (event.getAction() == GLFW.GLFW_PRESS) {
                BoxRenderUtil.useSeeThroughBox = true;
            } else if (event.getAction() == GLFW.GLFW_RELEASE) {
                BoxRenderUtil.useSeeThroughBox = false;
            }
        }

        if (KEY_ROLL_SPECIAL_MODE.get().getKey().equals(key)) {
            if (event.getAction() == GLFW.GLFW_PRESS) {
                pressingSpecialKey = true;
            } else if (event.getAction() == GLFW.GLFW_RELEASE) {
                pressingSpecialKey = false;
            }
        }
    }
}
