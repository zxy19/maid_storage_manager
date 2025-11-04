package studio.fantasyit.maid_storage_manager.menu.communicate;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.communicate.data.ConfigurableCommunicateData;

import java.util.List;
import java.util.function.Consumer;

public class CommunicateListItemWidget extends AbstractWidget {
    private final Consumer<Integer> consumer;
    private int i;
    private ConfigurableCommunicateData data;

    public CommunicateListItemWidget(int x, int y, int w, int h, ConfigurableCommunicateData data, int i, Consumer<Integer> onClick) {
        super(x, y, w, h, Component.literal(""));
        this.data = data;
        this.i = i;
        this.consumer = onClick;
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics graphics, int mX, int mY, float pt) {
        if (isHovered && active) {
            graphics.fill(getX(), getY(), getX() + width, getY() + height, 0xA0FFFFFF);
        }
        graphics.drawString(Minecraft.getInstance().font,
                Component.translatable("gui.maid_storage_manager.communicate_terminal.list.index", i),
                getX() - 2,
                getY() + 1,
                0xFFFFFF);
        ConfigurableCommunicateData.Item item = data.items.get(i);
        graphics.fill(getX(), getY() + 11, getX() + 4, getY() + 14, item.whiteMode ? 0xFFFFFFFF : 0xFF000000);
        graphics.pose().pushPose();
        graphics.pose().translate(getX() + 4, getY() + 3, 0);
        graphics.pose().scale(0.7f, 0.7f, 1);
        item.slot.drawGold(graphics, 0, 0);
        graphics.pose().popPose();


        List<ItemStack> items = item.requires.stream().filter(itemStack -> !itemStack.isEmpty()).toList();
        if (!items.isEmpty()) {
            int idxItem = (Minecraft.getInstance().player.tickCount / 20) % items.size();
            ItemStack itemStack = items.get(idxItem);
            graphics.pose().pushPose();
            graphics.pose().translate(getX() + 15, getY(), 0);
            graphics.pose().scale(0.9f, 0.9f, 1);
            graphics.renderItem(itemStack, 0, 0);
            graphics.pose().popPose();
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput p_259858_) {

    }

    @Override
    public void onClick(double p_93634_, double p_93635_) {
        consumer.accept(i);
    }
}
