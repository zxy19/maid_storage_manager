package studio.fantasyit.maid_storage_manager.data;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class InScreenTipData {
    public static Component tip = Component.literal("");
    public static float animate = 0;
    public static float time = 0;
    public static boolean hide = false;

    static long lastTick = 0;

    public static void tick() {
        long currentTick = System.currentTimeMillis();
        if (lastTick == 0) {
            lastTick = currentTick;
            return;
        }
        float deltaTick = (float) (currentTick - lastTick) / 1000f;
        lastTick = System.currentTimeMillis();
        if (tip != null) {
            if (hide) animate -= deltaTick;
            else animate += deltaTick;
            animate = Math.max(0, Math.min(1, animate));
            if (animate >= 1 && !hide) {
                time -= deltaTick;
                if (time <= 0) {
                    hide = true;
                }
            }
        }
    }

    public static void render(GuiGraphics guiGraphics, int mouseX, int mouseY, Screen screen) {
        if (tip != null) {
            int height = screen.getMinecraft().font.lineHeight;
            guiGraphics.drawCenteredString(
                    screen.getMinecraft().font,
                    tip,
                    screen.width / 2,
                    (int) ((animate - 0.5f) * 2 * height),
                    0xFFFFFF | (int) (animate * 255) << 24
            );
        }
    }

    public static void show(Component tip, float time) {
        InScreenTipData.tip = tip;
        InScreenTipData.time = time;
        InScreenTipData.hide = false;
    }
}
