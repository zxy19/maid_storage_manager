package studio.fantasyit.maid_storage_manager.api;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;

public interface IGuiGraphicsGetter {
    GuiGraphics getGuiGraphics(PoseStack pose);
}
