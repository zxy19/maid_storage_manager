package studio.fantasyit.tour_guide.data;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import oshi.util.tuples.Pair;
import studio.fantasyit.tour_guide.api.event.ItemTourGuideRegisterEvent;
import studio.fantasyit.tour_guide.network.Network;
import studio.fantasyit.tour_guide.network.S2CSyncTriggerableItems;

import java.util.HashMap;
import java.util.Map;

public class ItemTourGuide {
    public static Map<Item, ResourceLocation> itemTourGuide = new HashMap<>();

    public static void clear() {
        itemTourGuide.clear();
    }

    public static void register(Item item, ResourceLocation tourGuide) {
        itemTourGuide.put(item, tourGuide);
    }

    public static void syncTo(ServerPlayer player) {
        Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new S2CSyncTriggerableItems(
                itemTourGuide
                        .entrySet()
                        .stream()
                        .map(t -> new Pair<>(ForgeRegistries.ITEMS.getKey(t.getKey()), t.getValue()))
                        .toList()
        ));
    }

    public static void clearAndBroadcastRegister() {
        clear();
        MinecraftForge.EVENT_BUS.post(new ItemTourGuideRegisterEvent());
    }

    public static ResourceLocation get(Item item) {
        return itemTourGuide.get(item);
    }
}
