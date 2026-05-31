package studio.fantasyit.maid_storage_manager.menu.craft.anvil;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.menu.container.FilterSlot;
import studio.fantasyit.maid_storage_manager.menu.craft.base.AbstractCraftScreen;
import studio.fantasyit.maid_storage_manager.menu.craft.base.StepDataContainer;
import studio.fantasyit.maid_storage_manager.network.CraftGuideGuiPacket;

public class AnvilCraftScreen extends AbstractCraftScreen<AnvilCraftMenu> {
    private static final Identifier background = Identifier.fromNamespaceAndPath(MaidStorageManager.MODID, "textures/gui/craft/type/anvil.png");
    String name = "";
    EditBox nameBox;

    public AnvilCraftScreen(AnvilCraftMenu p_97741_, Inventory p_97742_, Component p_97743_) {
        super(p_97741_, p_97742_, p_97743_, background);
    }

    @Override
    protected void addButtons() {
        EditBox editBox = new EditBox(font, getGuiLeft() + 58, getGuiTop() + 50, 96, 16, Component.literal(""));
        CompoundTag extraData = menu.stepDataContainer.step.extraData;
        name = "";
        if (extraData != null && extraData.contains("name")) {
            name = extraData.getString("name").orElse("");
        }
        editBox.setValue(name);
        editBox.setBordered(false);
        editBox.setResponder(this::sendText);
        nameBox = editBox;
        this.addRenderableWidget(editBox);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);
        if (menu.xpCost != -1) {
            guiGraphics.text(font,
                    Component.translatable("gui.maid_storage_manager.craft_guide.anvil_xp_cost", menu.xpCost).getString(),
                    getGuiLeft() + 27,
                    getGuiTop() + 96,
                    0xffffff
            );
        }
    }

    public void sendText(String text) {
        if (text.equals(name)) return;
        name = text;
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putString("name", text);
        sendAndTriggerLocalPacket(new CraftGuideGuiPacket(CraftGuideGuiPacket.Type.EXTRA, 0, 0, compoundTag));
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == 256) {
            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.closeContainer();
            }
        }

        return this.nameBox.keyPressed(event) || this.nameBox.canConsumeInput() || super.keyPressed(event);
    }

    @Override
    public boolean mouseScrolled(double p_94686_, double p_94687_, double dx, double p_94688_) {
        @Nullable Slot slot = this.getSlotUnderMouse();
        if (slot instanceof FilterSlot filterSlot && filterSlot.container instanceof StepDataContainer sdc && !filterSlot.readonly) {
            MutableInt count = new MutableInt(sdc.getCount(filterSlot.getContainerSlot()));
            int dv = (int) (Math.abs(p_94688_) / p_94688_);
            if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), InputConstants.KEY_LSHIFT))
                dv *= 10;

            if (dv > 0) {
                if (count.addAndGet(dv) == 0) count.addAndGet(1);
            } else {
                if (count.addAndGet(dv) <= 0) count.setValue(1);
            }
            sendAndTriggerLocalPacket(
                    new CraftGuideGuiPacket(
                            CraftGuideGuiPacket.Type.COUNT,
                            filterSlot.getContainerSlot(),
                            count.getValue()
                    )
            );
        }
        return super.mouseScrolled(p_94686_, p_94687_, dx, p_94688_);
    }
}