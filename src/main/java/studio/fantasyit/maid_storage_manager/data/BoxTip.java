package studio.fantasyit.maid_storage_manager.data;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import studio.fantasyit.maid_storage_manager.storage.Target;

import java.util.Objects;

public record BoxTip(Target target, Component tip, int maxTime, float[] argb) {
    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeFloat(argb[0]);
        buf.writeFloat(argb[1]);
        buf.writeFloat(argb[2]);
        buf.writeFloat(argb[3]);
        buf.writeNbt(target.toNbt());
        buf.writeComponent(tip);
        buf.writeInt(maxTime);
    }

    public static BoxTip fromNetwork(FriendlyByteBuf buf) {
        float a = buf.readFloat();
        float r = buf.readFloat();
        float g = buf.readFloat();
        float b = buf.readFloat();
        return new BoxTip(Target.fromNbt(Objects.requireNonNull(buf.readNbt())), buf.readComponent(), buf.readInt(), new float[]{a, r, g, b});
    }
}
