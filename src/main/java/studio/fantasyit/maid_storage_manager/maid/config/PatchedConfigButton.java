package studio.fantasyit.maid_storage_manager.maid.config;

import com.github.tartaricacid.touhoulittlemaid.client.gui.widget.button.MaidConfigButton;
import net.minecraft.network.chat.Component;

public class PatchedConfigButton extends MaidConfigButton {
    public PatchedConfigButton(int x, int y, Component title, Component value, OnPress onLeftPressIn, OnPress onRightPressIn) {
        super(x, y, title, value, onLeftPressIn, onRightPressIn);
    }
}
