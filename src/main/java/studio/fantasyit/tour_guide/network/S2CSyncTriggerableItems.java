package studio.fantasyit.tour_guide.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;
import oshi.util.tuples.Pair;
import studio.fantasyit.tour_guide.data.ItemTourGuide;

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
            ItemTourGuide.clear();
            packet.items.forEach(t -> {
                ItemTourGuide.register(ForgeRegistries.ITEMS.getValue(t.getA()), t.getB());
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
