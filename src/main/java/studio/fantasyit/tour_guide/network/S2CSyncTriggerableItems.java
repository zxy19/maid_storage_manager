package studio.fantasyit.tour_guide.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record S2CSyncTriggerableItems(List<Pair<ResourceLocation, ResourceLocation>> items) {
    public static S2CSyncTriggerableItems fromNetwork(FriendlyByteBuf buf) {
        return new S2CSyncTriggerableItems(buf.readCollection(ArrayList::new, t -> new Pair<>(t.readResourceLocation(), t.readResourceLocation())));
    }

    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeCollection(items, (t, u) -> {
            t.writeResourceLocation(u.getA());
            t.writeResourceLocation(u.getB());
        });
    }

    public static void handle(S2CSyncTriggerableItems packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            TourData.triggerableItems.clear();
            TourData.triggerableItems.addAll(packet.items);
        });
        ctx.get().setPacketHandled(true);
    }
}
