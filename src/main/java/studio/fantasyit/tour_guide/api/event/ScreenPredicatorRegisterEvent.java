package studio.fantasyit.tour_guide.api.event;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;
import studio.fantasyit.tour_guide.client.screen_predicator.ScreenPredicatorAndTransformer;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

@OnlyIn(Dist.CLIENT)
public class ScreenPredicatorRegisterEvent extends Event implements IModBusEvent {
    public void register(ResourceLocation id, Predicate<Screen> predicate) {
        ScreenPredicatorAndTransformer.register(id, predicate);
    }

    public void register(ResourceLocation id, BiConsumer<Screen, GuiGraphics> transformer) {
        ScreenPredicatorAndTransformer.register(id, transformer);
    }
}
