package studio.fantasyit.maid_storage_manager.menu.container;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import studio.fantasyit.maid_storage_manager.menu.InventoryListScreen;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;

public class InventorySelectButton extends AbstractButton {
    private final Screen screen;
    private Object uuid = null;

    public InventorySelectButton(int p_93365_, int p_93366_, Screen screen) {
        super(p_93365_, p_93366_, 16, 16, Component.translatable("gui.maid_storage_manager.written_inventory_list.select_from"));
        this.screen = screen;
    }

    @Override
    public void onPress() {
        if (uuid != null)
            Minecraft.getInstance().setScreen(new InventoryListScreen(uuid, screen));
    }

    public void setUUID(Object uuid) {
        this.uuid = uuid;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int p_282682_, int p_281714_, float p_282542_) {
        if (uuid != null)
            guiGraphics.renderItem(
                    ItemRegistry.WRITTEN_INVENTORY_LIST.get().getDefaultInstance(),
                    this.getX(),
                    this.getY()
            );
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput p_259858_) {
        p_259858_.add(NarratedElementType.HINT, this.getMessage());
    }

    public Component getTooltipComponent() {
        return Component.translatable("gui.maid_storage_manager.written_inventory_list.select_from");
    }
}
