package studio.fantasyit.maid_storage_manager.menu.craft.altar;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.anti_ad.mc.ipn.api.IPNIgnore;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.menu.craft.base.AbstractCraftScreen;
import yalter.mousetweaks.api.MouseTweaksDisableWheelTweak;

@MouseTweaksDisableWheelTweak
@IPNIgnore
public class AltarCraftScreen extends AbstractCraftScreen<AltarCraftMenu> {
    private static final ResourceLocation background = new ResourceLocation(MaidStorageManager.MODID, "textures/gui/craft/type/altar.png");

    public AltarCraftScreen(AltarCraftMenu p_97741_, Inventory p_97742_, Component p_97743_) {
        super(p_97741_, p_97742_, p_97743_, background);
    }


    @Override
    protected void renderTooltip(@NotNull GuiGraphics graphics, int x, int y) {
        super.renderTooltip(graphics, x, y);
        if (this.menu.ppcost >= 0) {
            graphics.drawString(this.font,
                    String.valueOf(this.menu.ppcost),
                    59 + this.getGuiLeft(),
                    96 + this.getGuiTop(),
                    0xffffff
            );
        }
    }
}