package studio.fantasyit.maid_storage_manager.menu.craft.smithing;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.menu.base.ImageAsset;
import studio.fantasyit.maid_storage_manager.menu.craft.base.AbstractCraftScreen;

public class SmithingCraftScreen extends AbstractCraftScreen<SmithingCraftMenu> {
    private static final Identifier background = Identifier.fromNamespaceAndPath(MaidStorageManager.MODID, "textures/gui/craft/type/smithing.png");
    private final ImageAsset arrowNeg = new ImageAsset(background, 179, 3, 22, 15);

    public SmithingCraftScreen(SmithingCraftMenu p_97741_, Inventory p_97742_, Component p_97743_) {
        super(p_97741_, p_97742_, p_97743_, background);
    }

    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;

        if (menu.stepDataContainer.getItem(3).isEmpty()) {
            arrowNeg.blit(guiGraphics, relX + 96, relY + 70);
        }
    }
}
