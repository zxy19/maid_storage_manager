package studio.fantasyit.maid_storage_manager.menu.craft.anvil;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideRenderData;
import studio.fantasyit.maid_storage_manager.menu.container.FilterSlot;
import studio.fantasyit.maid_storage_manager.menu.container.ISaveFilter;
import studio.fantasyit.maid_storage_manager.menu.craft.base.ICraftGuiPacketReceiver;
import studio.fantasyit.maid_storage_manager.menu.craft.base.StepDataContainer;
import studio.fantasyit.maid_storage_manager.menu.craft.common.CommonStepDataContainer;
import studio.fantasyit.maid_storage_manager.network.CraftGuideGuiPacket;
import studio.fantasyit.maid_storage_manager.registry.GuiRegistry;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

public class AnvilCraftMenu extends AbstractContainerMenu implements ISaveFilter, ICraftGuiPacketReceiver {
    Player player;
    ItemStack target;
    CraftGuideData craftGuideData;
    StepDataContainer stepDataContainer = null;
    AnvilMenu anvilMenu;
    public int xpCost = -1;

    public AnvilCraftMenu(int p_38852_, Player player) {
        super(GuiRegistry.CRAFT_GUIDE_MENU_ANVIL.get(), p_38852_);
        this.player = player;
        target = player.getMainHandItem();
        craftGuideData = CraftGuideData.fromItemStack(target);
        stepDataContainer = new StepDataContainer(craftGuideData.getSteps().get(0), this);
        anvilMenu = new AnvilMenu(p_38852_, player.getInventory());
        addFilterSlots();
        addPlayerSlots();
        addSpecialSlots();
        recalculateRecipe();
    }

    private void addFilterSlots() {
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
                73
        ));
    }

    private void addPlayerSlots() {
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

    private void addSpecialSlots() {

    }

    @Override
    public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
        if (slotId >= 0 && this.getSlot(slotId) instanceof FilterSlot fs && fs.container instanceof CommonStepDataContainer container) {
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
