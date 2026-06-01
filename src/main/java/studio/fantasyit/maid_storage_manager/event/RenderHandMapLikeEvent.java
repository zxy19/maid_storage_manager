package studio.fantasyit.maid_storage_manager.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.render.base.ICustomGraphics;

@EventBusSubscriber(modid = MaidStorageManager.MODID, value = Dist.CLIENT)
public class RenderHandMapLikeEvent {
    public enum MapLikeRenderContext {
        MAIN_HAND,
        OFF_HAND,
        BOTH_HANDS,
        ITEM_FRAME_LARGE,
        ITEM_FRAME_SMALL,
        ITEM_FRAME_SIDE
    }

    public interface MapLikeRenderItem {
        MapLikeRenderer getRenderer();

        default boolean available(ItemStack stack) {
            return true;
        }
    }

    public interface MapLikeRenderer {
        default float getWidth(MapLikeRenderContext context) {
            return 142.0F;
        }

        default float getHeight(MapLikeRenderContext context) {
            return 142.0F;
        }

        RenderType backgroundRenderType(Minecraft mc, PoseStack pPoseStack, SubmitNodeCollector pSubmitNodeCollector, int pCombinedLight, ItemStack pStack);

        void renderOnHand(ICustomGraphics graphics, ItemStack pStack, int pCombinedLight, MapLikeRenderContext context);

        default void extraTransform(PoseStack pPoseStack, MapLikeRenderContext context) {
        }
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        InteractionHand hand = event.getHand();
        ItemStack stack = event.getItemStack();

        if (hand == InteractionHand.MAIN_HAND && stack.getItem() instanceof MapLikeRenderItem mli) {
            if (!mli.available(stack)) return;
            event.setCanceled(true);
        } else if (hand == InteractionHand.OFF_HAND && stack.getItem() instanceof MapLikeRenderItem mli) {
            if (!mli.available(stack)) return;
            event.setCanceled(true);
        }
    }
}
