package studio.fantasyit.maid_storage_manager.menu.craft.smithing;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.anti_ad.mc.ipn.api.IPNIgnore;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.menu.base.ImageAsset;
import studio.fantasyit.maid_storage_manager.menu.craft.base.AbstractCraftScreen;
import yalter.mousetweaks.api.MouseTweaksDisableWheelTweak;

@MouseTweaksDisableWheelTweak
@IPNIgnore
public class SmithingCraftScreen extends AbstractCraftScreen<SmithingCraftMenu> {
    private static final ResourceLocation background = new ResourceLocation(MaidStorageManager.MODID, "textures/gui/craft/type/smithing.png");
    private final ImageAsset arrowNeg = new ImageAsset(background, 179, 3, 22, 15);

    public SmithingCraftScreen(SmithingCraftMenu p_97741_, Inventory p_97742_, Component p_97743_) {
        super(p_97741_, p_97742_, p_97743_, background);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float p_97788_, int p_97789_, int p_97790_) {
        super.renderBg(guiGraphics, p_97788_, p_97789_, p_97790_);
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;

        if (menu.stepDataContainer.getItem(3).isEmpty()) {
            arrowNeg.blit(guiGraphics, relX + 96, relY + 70);
        }
    }
}