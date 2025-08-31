package studio.fantasyit.maid_storage_manager.menu.base;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import studio.fantasyit.maid_storage_manager.render.base.ICustomGraphics;

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

    public static ImageAsset from4Point(ResourceLocation location, int u, int v, int u1, int v1) {
        return new ImageAsset(location, u, v, u1 - u + 1, v1 - v + 1);
    }

    public ImageAsset(ResourceLocation location, int u, int v, int w, int h) {
        this(location, u, v, w, h, 256, 256);
    }

    public ImageAsset(ResourceLocation location, int w, int h) {
        this(location, 0, 0, w, h);
    }

    public void blit(GuiGraphics graphics, int x, int y) {
        graphics.blit(location, x, y, u, v, w, h, iw, ih);
    }

    public void blit(ICustomGraphics graphics, int x, int y) {
        graphics.blit(location, x, y, u, v, w, h, iw, ih);
    }
}
