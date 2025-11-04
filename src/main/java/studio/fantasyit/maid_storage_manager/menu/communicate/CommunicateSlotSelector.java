package studio.fantasyit.maid_storage_manager.menu.communicate;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.communicate.data.SlotType;
import studio.fantasyit.maid_storage_manager.menu.base.ImageAsset;

import java.util.List;
import java.util.function.Consumer;

public class CommunicateSlotSelector extends AbstractWidget {

    private final Consumer<SlotType> consumer;

    public CommunicateSlotSelector(int x, int y, Consumer<SlotType> consumer) {
        super(x, y, BACK1.w + BACK2.w, BACK1.h, Component.empty());
        this.consumer = consumer;
        visible = false;
        active = true;
    }

    static final ResourceLocation background = new ResourceLocation(MaidStorageManager.MODID, "textures/gui/communicate_terminal.png");
    static final ImageAsset SLOT = new ImageAsset(background, 237, 32, 18, 18);
    static final ImageAsset SLOT_HIGHLIGHT = new ImageAsset(background, 219, 32, 18, 18);
    static final ImageAsset BACK1 = ImageAsset.from4Point(background, 176, 64, 216, 132);
    static final ImageAsset BACK2 = ImageAsset.from4Point(background, 176, 132, 216, 199);
    static final Vector2i BASE_XY1 = new Vector2i(-176, -59);
    static final Vector2i BASE_XY2 = new Vector2i(-176 + BACK1.w, -127);
    public List<Pair<Vector2i, @Nullable SlotType>> slots = List.of(
            new Pair<>(new Vector2i(BASE_XY1).add(181, 64), SlotType.ALL),
            new Pair<>(new Vector2i(BASE_XY1).add(199, 64), SlotType.MAIN_HAND),
            new Pair<>(new Vector2i(BASE_XY2).add(176, 132), SlotType.OFF_HAND),
            new Pair<>(new Vector2i(BASE_XY2).add(194, 132), SlotType.BAUBLE),
            new Pair<>(new Vector2i(BASE_XY1).add(181, 84), SlotType.HEAD),
            new Pair<>(new Vector2i(BASE_XY1).add(199, 84), SlotType.CHEST),
            new Pair<>(new Vector2i(BASE_XY2).add(176, 152), SlotType.LEGS),
            new Pair<>(new Vector2i(BASE_XY2).add(194, 152), SlotType.FEET),
            new Pair<>(new Vector2i(BASE_XY1).add(181, 104), SlotType.ETA),
            new Pair<>(new Vector2i(BASE_XY2).add(194, 172), SlotType.FLOWER)
    );


    @Override
    protected void renderWidget(@NotNull GuiGraphics graphics, int _x, int _y, float p_268085_) {
        if (!visible) return;
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 1000);
        BACK1.blit(graphics, getX(), getY());
        BACK2.blit(graphics, getX() + BACK1.w, getY());
        int x = _x - getX();
        int y = _y - getY();
        for (Pair<Vector2i, SlotType> slot : slots) {
            Vector2i pos = slot.getA();
            SLOT.blit(graphics, pos.x + getX(), pos.y + getY());
            @Nullable ImageAsset s = slot.getB().icon();
            if (s != null)
                s.blit(graphics, pos.x + getX() + 1, pos.y + getY() + 1);
            if (x >= pos.x && x < pos.x + SLOT.w && y >= pos.y && y < pos.y + SLOT.h) {
                SLOT_HIGHLIGHT.blit(graphics, pos.x + getX(), pos.y + getY());
                graphics.renderTooltip(Minecraft.getInstance().font, slot.getB().getName(), getX(), getY());
            }
        }
        graphics.pose().popPose();
    }

    @Override
    public void onClick(double _x, double _y) {
        int x = (int) _x - getX();
        int y = (int) _y - getY();
        for (Pair<Vector2i, SlotType> slot : slots) {
            Vector2i pos = slot.getA();
            if (x >= pos.x && x < pos.x + SLOT.w && y >= pos.y && y < pos.y + SLOT.h) {
                consumer.accept(slot.getB());
                this.visible = false;
            }
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput p_259858_) {

    }

    public void show() {
        visible = true;
    }
}