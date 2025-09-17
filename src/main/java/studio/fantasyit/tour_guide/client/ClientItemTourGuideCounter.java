package studio.fantasyit.tour_guide.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import studio.fantasyit.tour_guide.client.event.ClientInputEvent;
import studio.fantasyit.tour_guide.data.ItemTourGuide;
import studio.fantasyit.tour_guide.network.C2SStartTourGuide;
import studio.fantasyit.tour_guide.network.Network;
import studio.fantasyit.tour_guide.screen.BlockHoldKeyScreen;

import java.util.List;

public class ClientItemTourGuideCounter {
    private static final long TIME_OUT = 900;
    public static long startTimeStamp = 0;
    public static Item lastItem = null;
    public static boolean hasAvailable = false;

    public static void addTooltip(Item item, List<Component> tooltip) {
        if (lastItem != item || !hasAvailable) return;
        tooltip.add(Component.translatable("tooltip.tour_guide.item_tour_guide",
                Component.translatable("tour_guide." + ItemTourGuide.get(item).toLanguageKey())
        ).withStyle(ChatFormatting.GRAY));
        if (startTimeStamp == 0) {
            tooltip.add(Component.translatable("tooltip.tour_guide.item_tour_guide.start", ClientInputEvent.KEY_START_TOUR_GUIDE.get().getKey().getDisplayName()));
        } else {
            long hasHoldTime = System.currentTimeMillis() - startTimeStamp;
            int p1 = Math.toIntExact(hasHoldTime * 35 / TIME_OUT);
            int p2 = 35 - p1;
            StringBuilder sp1 = new StringBuilder();
            for (int i = 0; i < p1; i++) {
                sp1.append("|");
            }
            StringBuilder sp2 = new StringBuilder();
            for (int i = 0; i < p2; i++) {
                sp2.append("|");
            }
            tooltip.add(Component.translatable("tooltip.tour_guide.item_tour_guide.hold",
                    Component.literal(sp1.toString()).withStyle(ChatFormatting.WHITE),
                    Component.literal(sp2.toString()).withStyle(ChatFormatting.GRAY)
            ));
        }
    }

    public static void keyPressed() {
        startTimeStamp = System.currentTimeMillis();
    }

    public static void keyReleased() {
        startTimeStamp = 0;
        if (Minecraft.getInstance().screen instanceof BlockHoldKeyScreen) {
            Minecraft.getInstance().setScreen(null);
        }
    }

    public static void updateHoveredItem(Item item) {
        if (item != lastItem) {
            lastItem = item;
            hasAvailable = ItemTourGuide.get(item) != null;
            startTimeStamp = 0;
        }
        if (startTimeStamp != 0) {
            if (System.currentTimeMillis() - startTimeStamp > TIME_OUT) {
                sendStart();
                startTimeStamp = 0;
            }
        }
    }

    private static void sendStart() {
        Network.INSTANCE.sendToServer(new C2SStartTourGuide(ItemTourGuide.get(lastItem)));
        Minecraft.getInstance().player.closeContainer();
        Minecraft.getInstance().setScreen(new BlockHoldKeyScreen());
    }

    public static boolean isHasAvailable() {
        return hasAvailable;
    }
}
