package studio.fantasyit.maid_storage_manager.menu.craft.anvil;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.anti_ad.mc.ipn.api.IPNIgnore;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.menu.craft.base.AbstractCraftScreen;
import studio.fantasyit.maid_storage_manager.network.CraftGuideGuiPacket;
import yalter.mousetweaks.api.MouseTweaksDisableWheelTweak;

@MouseTweaksDisableWheelTweak
@IPNIgnore
public class AnvilCraftScreen extends AbstractCraftScreen<AnvilCraftMenu> {
    private static final ResourceLocation background = new ResourceLocation(MaidStorageManager.MODID, "textures/gui/craft/type/anvil.png");
    String name = "";
    EditBox nameBox;

    public AnvilCraftScreen(AnvilCraftMenu p_97741_, Inventory p_97742_, Component p_97743_) {
        super(p_97741_, p_97742_, p_97743_, background);
    }
    @Override
    protected void addButtons() {
        EditBox editBox = new EditBox(font, getGuiLeft() + 57, getGuiTop() + 46, 96, 16, Component.literal(""));
        CompoundTag extraData = menu.stepDataContainer.step.extraData;
        name = "";
        if (extraData != null && extraData.contains("name")) {
            name = extraData.getString("name");
        }
        editBox.setValue(name);
        editBox.setBordered(false);
        editBox.setResponder(this::sendText);
        nameBox = editBox;
        this.addRenderableWidget(editBox);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float p_97788_, int p_97789_, int p_97790_) {
        super.renderBg(guiGraphics, p_97788_, p_97789_, p_97790_);
        if (menu.xpCost != -1) {
            guiGraphics.drawString(font,
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
    public boolean keyPressed(int p_97765_, int p_97766_, int p_97767_) {
        if (p_97765_ == 256) {
            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.closeContainer();
            }
        }

        return this.nameBox.keyPressed(p_97765_, p_97766_, p_97767_) || this.nameBox.canConsumeInput() || super.keyPressed(p_97765_, p_97766_, p_97767_);
    }
}