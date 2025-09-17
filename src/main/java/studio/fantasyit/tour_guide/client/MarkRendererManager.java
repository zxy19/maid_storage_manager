package studio.fantasyit.tour_guide.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import studio.fantasyit.tour_guide.client.gui.GuiRectMarkRenderer;
import studio.fantasyit.tour_guide.client.gui.GuiSlotMarkRenderer;
import studio.fantasyit.tour_guide.client.gui.GuiTextMarkRenderer;
import studio.fantasyit.tour_guide.client.screen_predicator.ScreenPredicatorAndTransformer;
import studio.fantasyit.tour_guide.client.world.BlockMarkRenderer;
import studio.fantasyit.tour_guide.client.world.EntityMarkRenderer;
import studio.fantasyit.tour_guide.client.world.TextMarkRenderer;
import studio.fantasyit.tour_guide.mark.IGuiMark;
import studio.fantasyit.tour_guide.mark.IMark;
import studio.fantasyit.tour_guide.mark.gui.GuiRectMark;
import studio.fantasyit.tour_guide.mark.gui.GuiSlotMark;
import studio.fantasyit.tour_guide.mark.gui.GuiTextMark;
import studio.fantasyit.tour_guide.mark.world.BlockMark;
import studio.fantasyit.tour_guide.mark.world.EntityMark;
import studio.fantasyit.tour_guide.mark.world.TextMark;

import java.util.HashMap;
import java.util.Map;

public class MarkRendererManager {
    private static Map<ResourceLocation, Either<IWorldMarkRenderer<?>, IGuiMarkRenderer<?>>> renderers = new HashMap<>();

    public static void register(ResourceLocation id, IWorldMarkRenderer<?> renderer) {
        renderers.put(id, Either.left(renderer));
    }

    public static void register(ResourceLocation id, IGuiMarkRenderer<?> renderer) {
        renderers.put(id, Either.right(renderer));
    }

    public static void dispatchWorldRender(IMark mark, MultiBufferSource source, Camera camera, LevelRenderer levelRenderer, PoseStack poseStack, float partialTicks) {
        if (!renderers.containsKey(mark.getId())) return;
        Either<IWorldMarkRenderer<?>, IGuiMarkRenderer<?>> iWorldMarkRendererIGuiMarkRendererEither = renderers.get(mark.getId());
        iWorldMarkRendererIGuiMarkRendererEither.ifLeft(i -> {
            ((IWorldMarkRenderer) i).render(source, levelRenderer, poseStack,camera, partialTicks, mark, new IWorldMarkRenderer.Context(new HashMap<>()));
        });
    }

    public static void dispatchGuiRender(IMark mark, GuiGraphics graphics, Screen screen) {
        if (!renderers.containsKey(mark.getId())) return;
        Either<IWorldMarkRenderer<?>, IGuiMarkRenderer<?>> iWorldMarkRendererIGuiMarkRendererEither = renderers.get(mark.getId());
        iWorldMarkRendererIGuiMarkRendererEither.ifRight(i -> {
            if (!(mark instanceof IGuiMark igm)) return;
            if (!ScreenPredicatorAndTransformer.predicate(igm.screenPredicate(), screen)) return;
            graphics.pose().pushPose();
            ScreenPredicatorAndTransformer.transform(igm.screenPredicate(), screen, graphics);
            ((IGuiMarkRenderer) i).render(graphics, mark, screen);
            graphics.pose().popPose();
        });
    }


    static {
        register(GuiTextMark.ID, new GuiTextMarkRenderer());
        register(GuiSlotMark.ID, new GuiSlotMarkRenderer());
        register(GuiRectMark.ID, new GuiRectMarkRenderer());
        register(TextMark.ID, new TextMarkRenderer());
        register(BlockMark.ID, new BlockMarkRenderer());
        register(EntityMark.ID, new EntityMarkRenderer());

    }
}
