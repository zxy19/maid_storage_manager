package studio.fantasyit.maid_storage_manager.menu.craft.anvil;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.menu.container.FilterSlot;
import studio.fantasyit.maid_storage_manager.menu.craft.base.AbstractCraftMenu;
import studio.fantasyit.maid_storage_manager.network.CraftGuideGuiPacket;
import studio.fantasyit.maid_storage_manager.registry.GuiRegistry;

public class AnvilCraftMenu extends AbstractCraftMenu<AnvilCraftMenu> {
    AnvilMenu anvilMenu;
    public int xpCost = -1;

    public AnvilCraftMenu(int p_38852_, Player player) {
        super(GuiRegistry.CRAFT_GUIDE_MENU_ANVIL.get(), p_38852_, player);
        anvilMenu = new AnvilMenu(p_38852_, player.getInventory());
    }

    @Override
    protected void addFilterSlots() {
        this.addSlot(new FilterSlot(stepDataContainer,
                0,
                28,
                73
        ));
        this.addSlot(new FilterSlot(stepDataContainer,
                1,
                77,
                73
        ));
        this.addSlot(new FilterSlot(stepDataContainer,
                2,
                135,
                73,
                true
        ));
    }
    @Override
    public void handleGuiPacket(CraftGuideGuiPacket.Type type, int key, int value, @Nullable CompoundTag data) {
        switch (type) {
            case SET_ALL_INPUT -> {
                ListTag list = data.getList("inputs", 10);
                for (int i = 0; i < list.size(); i++) {
                    CompoundTag tag = list.getCompound(i);
                    ItemStack stack = ItemStack.of(tag);
                    stepDataContainer.setItem(i, stack);
                }
                save();
            }
            case SET_ITEM -> {
                if (data != null) {
                    this.getSlot(key).set(ItemStack.of(data));
                    save();
                }
            }
            case EXTRA -> {
                stepDataContainer.step.setExtraData(data);
                save();
            }
        }
    }

    public void recalculateRecipe() {
        if(this.anvilMenu == null) return;
        CompoundTag extra = stepDataContainer.step.getExtraData();
        String name = extra.getString("name");
        anvilMenu.setItem(0, 0, stepDataContainer.getItem(0));
        anvilMenu.setItem(1, 0, stepDataContainer.getItem(1));
        anvilMenu.setItemName(name);
        anvilMenu.createResult();
        stepDataContainer.setItemNoTrigger(2, anvilMenu.getSlot(2).getItem());
        xpCost = anvilMenu.getCost();
    }
}
