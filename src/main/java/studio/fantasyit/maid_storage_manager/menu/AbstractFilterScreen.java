package studio.fantasyit.maid_storage_manager.menu;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import studio.fantasyit.maid_storage_manager.jei.IFilterScreen;

public abstract class AbstractFilterScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> implements IFilterScreen {
    public AbstractFilterScreen(T p_97741_, Inventory p_97742_, Component p_97743_) {
        super(p_97741_, p_97742_, p_97743_);
    }
}
