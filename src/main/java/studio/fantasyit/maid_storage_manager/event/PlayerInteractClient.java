package studio.fantasyit.maid_storage_manager.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.items.HangUpItem;

@Mod.EventBusSubscriber(modid = MaidStorageManager.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class PlayerInteractClient {
    @SubscribeEvent
    public static void onPlayerInteractEntity(PlayerInteractEvent.EntityInteract event) {
        if (event.getTarget() instanceof ItemFrame ifr) {
            if (ifr.getItem().getItem() instanceof HangUpItem hi && hi.allowClickThrough() && !event.getEntity().isShiftKeyDown()) {
                if (tryClickThrough(event)) {
                    event.setCanceled(true);
                    event.setCancellationResult(InteractionResult.CONSUME);
                }
            }
        }
    }

    private static boolean tryClickThrough(PlayerInteractEvent.EntityInteract event) {
        Minecraft minecraft = Minecraft.getInstance();
        BlockHitResult clip = event.getLevel().clip(new ClipContext(
                minecraft.player.getEyePosition(),
                minecraft.player.getEyePosition().add(minecraft.player.getViewVector(1.0F).scale(5.0D)),
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.ANY,
                minecraft.player
        ));
        if (clip.getType() != HitResult.Type.BLOCK) return false;
        int i = minecraft.player.getItemInHand(event.getHand()).getCount();
        InteractionResult interactionresult1 = minecraft.gameMode.useItemOn((LocalPlayer) event.getEntity(), event.getHand(), clip);
        if (interactionresult1.consumesAction()) {
            if (interactionresult1.shouldSwing()) {
                minecraft.player.swing(event.getHand());
                ItemStack itemstack = minecraft.player.getItemInHand(event.getHand());
                if (!itemstack.isEmpty() && (itemstack.getCount() != i || minecraft.gameMode.hasInfiniteItems())) {
                    minecraft.gameRenderer.itemInHandRenderer.itemUsed(event.getHand());
                }
            }

            return true;
        }
        return interactionresult1 == InteractionResult.FAIL;
    }
}
