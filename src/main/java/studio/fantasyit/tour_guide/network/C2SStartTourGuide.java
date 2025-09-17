package studio.fantasyit.tour_guide.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import studio.fantasyit.tour_guide.api.TourManager;

import java.util.Objects;
import java.util.function.Supplier;

public record C2SStartTourGuide(ResourceLocation id) {
    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeResourceLocation(id);
    }

    public static C2SStartTourGuide fromNetwork(FriendlyByteBuf buf) {
        return new C2SStartTourGuide(buf.readResourceLocation());
    }

    public static void handle(C2SStartTourGuide packet, Supplier<NetworkEvent.Context> ctxGetter) {
        NetworkEvent.Context ctx = ctxGetter.get();
        ctx.enqueueWork(() -> TourManager.start(Objects.requireNonNull(ctx.getSender()), packet.id));
        ctx.setPacketHandled(true);
    }
}
