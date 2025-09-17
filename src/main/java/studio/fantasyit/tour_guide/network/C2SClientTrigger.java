package studio.fantasyit.tour_guide.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import studio.fantasyit.tour_guide.api.TourGuideTrigger;

import java.util.function.Supplier;

public record C2SClientTrigger(String name) {
    public static C2SClientTrigger fromNetwork(FriendlyByteBuf buf) {
        return new C2SClientTrigger(buf.readUtf());
    }

    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeUtf(name);
    }

    public static void handle(C2SClientTrigger packet, Supplier<NetworkEvent.Context> ctxGetter) {
        ctxGetter.get().enqueueWork(() -> {
            TourGuideTrigger.trigger(ctxGetter.get().getSender(), packet.name);
        });
        ctxGetter.get().setPacketHandled(true);
    }
}
