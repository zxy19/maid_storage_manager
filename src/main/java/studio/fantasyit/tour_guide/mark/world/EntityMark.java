package studio.fantasyit.tour_guide.mark.world;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.tour_guide.mark.IMark;

public record EntityMark(int id,int color, Component text) implements IMark {

    public static final ResourceLocation ID = new ResourceLocation(MaidStorageManager.MODID, "entity");
    @Override
    public ResourceLocation getId() {
        return ID;
    }
    public static EntityMark fromNetwork(FriendlyByteBuf buf) {
        return new EntityMark(buf.readInt(), buf.readInt(), buf.readComponent());
    }

    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeInt(id);
        buf.writeInt(color);
        buf.writeComponent(text);
    }
}