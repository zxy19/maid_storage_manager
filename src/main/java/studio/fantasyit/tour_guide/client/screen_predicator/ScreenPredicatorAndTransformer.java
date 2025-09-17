package studio.fantasyit.tour_guide.client.screen_predicator;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.tour_guide.mark.ServerScreenPredicatorMarks;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class ScreenPredicatorAndTransformer {
    private static final Map<ResourceLocation, BiConsumer<Screen, GuiGraphics>> SCREEN_GRAPHICS_TRANSFORMERS = new HashMap<>(Map.of(
            ServerScreenPredicatorMarks.ALL, (screen, graphics) -> {
                if (screen instanceof AbstractContainerScreen<?> containerScreen) {
                    graphics.pose().translate(
                            containerScreen.getGuiLeft(),
                            containerScreen.getGuiTop(),
                            0
                    );
                }
            },
            ServerScreenPredicatorMarks.NO_GUI, (screen, graphics) -> {
            }
    ));
    private static final Map<ResourceLocation, Predicate<@Nullable Screen>> SCREEN_PREDICATORS = new HashMap<>(Map.of(
            ServerScreenPredicatorMarks.ALL, screen -> true,
            ServerScreenPredicatorMarks.NO_GUI, screen -> screen == null || screen instanceof ChatScreen
    ));

    public static boolean predicate(ResourceLocation id, Screen screen) {
        if (SCREEN_PREDICATORS.containsKey(id))
            return SCREEN_PREDICATORS.get(id).test(screen);
        return false;
    }

    public static void transform(ResourceLocation id, Screen screen, GuiGraphics graphics) {
        if (SCREEN_GRAPHICS_TRANSFORMERS.containsKey(id))
            SCREEN_GRAPHICS_TRANSFORMERS.get(id).accept(screen, graphics);
        else
            SCREEN_GRAPHICS_TRANSFORMERS.get(ServerScreenPredicatorMarks.ALL).accept(screen, graphics);
    }

    public static void register(ResourceLocation id, Predicate<Screen> predicate) {
        SCREEN_PREDICATORS.put(id, predicate);
    }

    public static void register(ResourceLocation id, BiConsumer<Screen, GuiGraphics> transformer) {
        SCREEN_GRAPHICS_TRANSFORMERS.put(id, transformer);
    }
}
