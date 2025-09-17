package studio.fantasyit.tour_guide.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import studio.fantasyit.tour_guide.client.TourGuidingClientData;
import studio.fantasyit.tour_guide.mark.ContextMarkSerializer;
import studio.fantasyit.tour_guide.mark.IMark;

import java.util.List;
import java.util.function.Supplier;

public record S2CUpdateTourGuideData(List<IMark> marks) {
    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeCollection(marks, ContextMarkSerializer::serialize);
    }

    public static S2CUpdateTourGuideData fromNetwork(FriendlyByteBuf buf) {
        return new S2CUpdateTourGuideData(buf.readList(ContextMarkSerializer::deserialize));
    }

    @OnlyIn(Dist.CLIENT)
    public static void handle(S2CUpdateTourGuideData packet, Supplier<NetworkEvent.Context> ctxGetter) {
        ctxGetter.get().enqueueWork(() -> {
            TourGuidingClientData.setMarks(packet.marks);
        });
        ctxGetter.get().setPacketHandled(true);
    }
}
