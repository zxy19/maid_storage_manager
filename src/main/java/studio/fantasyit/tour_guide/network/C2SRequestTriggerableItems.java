package studio.fantasyit.tour_guide.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import studio.fantasyit.tour_guide.data.ItemTourGuide;

import java.util.function.Supplier;

public record C2SRequestTriggerableItems() {
    public static C2SRequestTriggerableItems fromNetwork(FriendlyByteBuf buf) {
        return new C2SRequestTriggerableItems();
    }

    public void toNetwork(FriendlyByteBuf buf) {
    }

    public static void handle(C2SRequestTriggerableItems message, Supplier<NetworkEvent.Context> contextGetter) {
        contextGetter.get().enqueueWork(() -> {
            ItemTourGuide.syncTo(contextGetter.get().getSender());
        });
        contextGetter.get().setPacketHandled(true);
    }
}
