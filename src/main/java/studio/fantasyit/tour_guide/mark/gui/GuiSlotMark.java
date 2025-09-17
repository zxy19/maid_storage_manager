package studio.fantasyit.tour_guide.mark.gui;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.tour_guide.mark.IGuiMark;

public record GuiSlotMark(ResourceLocation screenPredicate, int id, int color) implements IGuiMark {
    public static final ResourceLocation ID = new ResourceLocation(MaidStorageManager.MODID, "gui_slot");
    @Override
    public ResourceLocation getId() {
        return ID;
    }
    public static GuiSlotMark fromNetwork(FriendlyByteBuf buf) {
        return new GuiSlotMark(buf.readResourceLocation(), buf.readInt(), buf.readInt());
    }

    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeResourceLocation(screenPredicate);
        buf.writeInt(id);
        buf.writeInt(color);
    }
}
