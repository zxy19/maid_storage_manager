package studio.fantasyit.maid_storage_manager.menu.craft.base;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideRenderData;
import studio.fantasyit.maid_storage_manager.menu.container.FilterSlot;
import studio.fantasyit.maid_storage_manager.menu.container.ISaveFilter;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

abstract public class AbstractCraftMenu<T extends AbstractCraftMenu<?>> extends AbstractContainerMenu implements ISaveFilter, ICraftGuiPacketReceiver {
    ItemStack target;
    protected Player player;
    protected CraftGuideData craftGuideData;
    public StepDataContainer stepDataContainer = null;

    public AbstractCraftMenu(MenuType<T> p_38851_, int p_38852_, Player player) {
        super(p_38851_, p_38852_);
        this.player = player;
        target = player.getMainHandItem();
        craftGuideData = CraftGuideData.fromItemStack(target);
        stepDataContainer = new StepDataContainer(craftGuideData.getSteps().get(0), this);
        addFilterSlots();
        addPlayerSlots();
        addSpecialSlots();
        recalculateRecipe();
    }

    abstract protected void addFilterSlots();

    protected void addPlayerSlots() {
        final int cellHeight = 18;
        final int cellWidth = 18;
        final int startY = 164;
        final int startX = 8;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(player.getInventory(),
                        9 + i * 9 + j,
                        startX + j * cellWidth,
                        startY + i * cellHeight));
            }
        }
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(player.getInventory(),
                    i,
                    startX + i * cellWidth,
                    startY + 58));
        }
    }

    protected void addSpecialSlots() {
    }

    @Override
    public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
        if (slotId >= 0 && this.getSlot(slotId) instanceof FilterSlot fs && fs.container instanceof StepDataContainer container) {
            if (fs.readonly) return;
            int slot = fs.getContainerSlot();
            if (clickTypeIn == ClickType.THROW)
                return;

            ItemStack held = getCarried();
            if (clickTypeIn == ClickType.CLONE) {
                if (player.isCreative() && held.isEmpty()) {
                    ItemStack stackInSlot = container.getItem(slot)
                            .copy();
                    stackInSlot.setCount(stackInSlot.getMaxStackSize());
                    setCarried(stackInSlot);
                    save();
                    return;
                }
                return;
            }

            ItemStack insert;
            if (held.isEmpty()) {
                insert = ItemStack.EMPTY;
            } else {
                insert = held.copy();
                insert.setCount(1);
            }
            container.setItem(slot, insert);
            container.setCount(slot, 1);
            getSlot(slotId).setChanged();
            save();
        } else {
            super.clicked(slotId, dragType, clickTypeIn, player);
        }
    }

    @Override
    public void slotsChanged(Container p_38868_) {
        if (stepDataContainer == null) return;
        super.slotsChanged(p_38868_);
        save();
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player p_38941_, int p_38942_) {
        Slot slot = this.getSlot(p_38942_);
        if (slot.hasItem()) {
            if (slot instanceof FilterSlot fs) {
                fs.set(ItemStack.EMPTY);
            } else {
                boolean found = false;
                int toPlace = -1;
                for (int j = 0; j < stepDataContainer.getContainerSize(); j++) {
                    if (stepDataContainer.getItem(j).isEmpty()) {
                        if (toPlace == -1) {
                            toPlace = j;
                        }
                    } else if (ItemStackUtil.isSame(stepDataContainer.getItem(j), slot.getItem(), stepDataContainer.matchTag)) {
                        found = true;
                    }
                }


                if (!found) {
                    if (toPlace != -1) {
                        stepDataContainer.setItem(toPlace, slot.getItem().copyWithCount(1));
                        save();
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.getMainHandItem() == target;
    }

    @Override
    public boolean canDragTo(Slot p_38945_) {
        return !(p_38945_ instanceof FilterSlot);
    }

    @Override
    public void save() {
        if (stepDataContainer == null) return;
        if (player.level().isClientSide) return;
        recalculateRecipe();
        stepDataContainer.save();
        craftGuideData.saveToItemStack(target);
        CraftGuideRenderData.recalculateItemStack(target);
        this.broadcastChanges();
    }

    abstract public void recalculateRecipe();
}
