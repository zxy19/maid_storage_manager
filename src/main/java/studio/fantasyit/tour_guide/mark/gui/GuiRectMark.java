package studio.fantasyit.tour_guide.mark.gui;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.tour_guide.mark.IGuiMark;

public record GuiRectMark(ResourceLocation screenPredicate, int x, int y, int width, int height,
                          int color,int fill) implements IGuiMark {

    public static final ResourceLocation ID = new ResourceLocation(MaidStorageManager.MODID, "gui_rect");
    @Override
    public ResourceLocation getId() {
        return ID;
    }
    public static GuiRectMark fromNetwork(FriendlyByteBuf buf) {
        return new GuiRectMark(buf.readResourceLocation(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt());
    }

    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeResourceLocation(screenPredicate);
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(width);
        buf.writeInt(height);
        buf.writeInt(color);
        buf.writeInt(fill);
    }
}
