package studio.fantasyit.tour_guide.mark.gui;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.tour_guide.mark.IGuiMark;

public record GuiMainTipMark(ResourceLocation screenPredicate, Component text, boolean canSkip) implements IGuiMark {
    public static final ResourceLocation ID = new ResourceLocation(MaidStorageManager.MODID, "gui_main_tip");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public void toNetwork(FriendlyByteBuf buf){
        buf.writeResourceLocation(screenPredicate);
        buf.writeComponent(text);
        buf.writeBoolean(canSkip);
    }
    public static GuiMainTipMark fromNetwork(FriendlyByteBuf buf){
        return new GuiMainTipMark(buf.readResourceLocation(), buf.readComponent(), buf.readBoolean());
    }
}
