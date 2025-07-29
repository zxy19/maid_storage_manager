package studio.fantasyit.maid_storage_manager.items;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import studio.fantasyit.maid_storage_manager.event.RenderHandMapLikeEvent;

public class ProgressPad extends Item implements RenderHandMapLikeEvent.MapLikeRenderItem {

    private static final RenderType MAP_BACKGROUND = RenderType.text(new ResourceLocation("textures/map/map_background.png"));

    public ProgressPad() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public float getWidth() {
        return 80;
    }

    @Override
    public float getHeight() {
        return 142;
    }

    @Override
    public RenderType backgroundRenderType(Minecraft mc, PoseStack pPoseStack, MultiBufferSource pBuffer, int pCombinedLight, ItemStack pStack) {
        return MAP_BACKGROUND;
    }

    @Override
    public void renderOnHand(GuiGraphics graphics, ItemStack pStack, int pCombinedLight) {
        graphics.renderItem(
                Items.GRASS.getDefaultInstance(),
                0,
                0
        );
    }
}
