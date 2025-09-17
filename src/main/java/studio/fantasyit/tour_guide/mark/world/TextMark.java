package studio.fantasyit.tour_guide.mark.world;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.tour_guide.mark.IMark;

public record TextMark(Component text, Vec3 pos, int color) implements IMark {

    public static final ResourceLocation ID = new ResourceLocation(MaidStorageManager.MODID, "text");
    @Override
    public ResourceLocation getId() {
        return ID;
    }
    public static TextMark fromNetwork(FriendlyByteBuf buf) {
        return new TextMark(buf.readComponent(), new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()), buf.readInt());
    }

    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeComponent(text);
        buf.writeDouble(pos.x);
        buf.writeDouble(pos.y);
        buf.writeDouble(pos.z);
        buf.writeInt(color);
    }
}
