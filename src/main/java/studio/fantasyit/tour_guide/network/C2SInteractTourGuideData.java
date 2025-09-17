package studio.fantasyit.tour_guide.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import studio.fantasyit.tour_guide.api.TourManager;
import studio.fantasyit.tour_guide.data.TourData;

import java.util.function.Supplier;

public record C2SInteractTourGuideData(Type type) {
    public enum Type {
        DONE,
        QUIT,
        SKIP
    }
    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeEnum(type);
    }

    public static C2SInteractTourGuideData fromNetwork(FriendlyByteBuf buf) {
        return new C2SInteractTourGuideData(buf.readEnum(Type.class));
    }

    public static void handle(C2SInteractTourGuideData packet, Supplier<NetworkEvent.Context> ctxGetter) {
        ctxGetter.get().enqueueWork(() -> {
            ServerPlayer sender = ctxGetter.get().getSender();
            if (sender == null) {
                return;
            }
            TourData tourData = TourManager.get(sender);
            if (tourData == null) {
                return;
            }
            switch (packet.type) {
                case DONE -> tourData.doneAndTryNextStep();
                case SKIP -> tourData.skipAndTryNextStep();
            }
        });
        ctxGetter.get().setPacketHandled(true);
    }
}
