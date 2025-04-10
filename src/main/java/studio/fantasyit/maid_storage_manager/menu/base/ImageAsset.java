package studio.fantasyit.maid_storage_manager.menu.base;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class ImageAsset {
    public ResourceLocation location;
    public int u, v, w, h, iw, ih;


    public ImageAsset(ResourceLocation location, int u, int v, int w, int h, int iw, int ih) {
        this.location = location;
        this.u = u;
        this.v = v;
        this.w = w;
        this.h = h;
        this.iw = iw;
        this.ih = ih;
    }

    public ImageAsset(ResourceLocation location, int u, int v, int w, int h) {
        this(location, u, v, w, h, 256, 256);
    }

    public ImageAsset(ResourceLocation location, int w, int h) {
        this(location, 0, 0, w, h);
    }

    public void blit(GuiGraphics graphics, int x, int y) {
        graphics.blit(location, x, y, u, v, w, h, 256, 256);
    }
}
