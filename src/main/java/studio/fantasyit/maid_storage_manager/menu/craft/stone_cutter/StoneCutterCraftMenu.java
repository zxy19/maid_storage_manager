package studio.fantasyit.maid_storage_manager.menu.craft.stone_cutter;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.StonecutterRecipe;
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
import studio.fantasyit.maid_storage_manager.util.RecipeUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StoneCutterCraftMenu extends AbstractContainerMenu implements ISaveFilter, ICraftGuiPacketReceiver {
    Player player;
    ItemStack target;
    CraftGuideData craftGuideData;
    StepDataContainer stepDataContainer = null;
    SimpleContainer displayOnlySlots = new SimpleContainer(15);
    List<ItemStack> availableItems = new ArrayList<>();
    public int page = 0;
    public int maxPage = 1;

    public StoneCutterCraftMenu(int p_38852_, Player player) {
        super(GuiRegistry.CRAFT_GUIDE_MENU_STONE_CUTTER.get(), p_38852_);
        this.player = player;
        target = player.getMainHandItem();
        craftGuideData = CraftGuideData.fromItemStack(target);
        stepDataContainer = new StepDataContainer(craftGuideData.getSteps().get(0), this);
        addFilterSlots();
        addPlayerSlots();
        addSpecialSlots();
        revalidRecipe();
    }


    private void addFilterSlots() {
        this.addSlot(new FilterSlot(stepDataContainer,
                0,
                47,
                39
        ));
        this.addSlot(new FilterSlot(stepDataContainer,
                1,
                109,
                39
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
        if (player.level().isClientSide) {
            if (Minecraft.getInstance().screen instanceof StoneCutterCraftScreen screen) {
                screen.handleGuiPacket(CraftGuideGuiPacket.Type.SET_ITEM, 0, 0, new CompoundTag());
            }
        }
        revalidRecipe();
        stepDataContainer.save();
        craftGuideData.saveToItemStack(target);
        CraftGuideRenderData.recalculateItemStack(target);
    }

    @Override
    public void handleGuiPacket(CraftGuideGuiPacket.Type type, int key, int value, @Nullable CompoundTag data) {
        switch (type) {
            case SET_ITEM -> {
                if (data != null) {
                    this.getSlot(key).set(ItemStack.of(data));
                    revalidRecipe();
                    save();
                }
            }
            case PAGE_UP, PAGE_DOWN -> {
                page = value;
                reArrangeSlotItem();
            }
            case COUNT -> {
                stepDataContainer.setCount(key, value);
                stepDataContainer.setChanged();
            }
        }
    }

    private void reArrangeSlotItem() {
        if (page >= maxPage) {
            page = maxPage - 1;
        }
        for (int i = 0; i < 15; i++) {
            int index = page * 5 + i;
            if (index < availableItems.size()) {
                this.displayOnlySlots.setItem(i, availableItems.get(index));
            } else {
                this.displayOnlySlots.setItem(i, ItemStack.EMPTY);
            }
        }
    }


    private void revalidRecipe() {
        if (!stepDataContainer.getItem(0).isEmpty()) {
            List<StonecutterRecipe> recipe = RecipeUtil.getStonecuttingRecipe(player.level(), stepDataContainer.getItem(0));
            if (!recipe.isEmpty()) {
                availableItems = recipe.stream().map(re -> re.getResultItem(player.level().registryAccess())).toList();
                Optional<ItemStack> first = availableItems.stream().filter(
                        itemStack -> ItemStackUtil.isSame(stepDataContainer.getItem(1), itemStack, stepDataContainer.matchTag)
                ).findAny();
                if (first.isEmpty()) {
                    stepDataContainer.setItemNoTrigger(1, ItemStack.EMPTY);
                } else {
                    stepDataContainer.setCount(1, stepDataContainer.getCount(0) * first.get().getCount());
                }

                maxPage = (Math.max(availableItems.size() - 15, 0) + 4) / 5 + 1;
                reArrangeSlotItem();
                return;
            }
        }
        availableItems = new ArrayList<>();
        maxPage = 1;
        reArrangeSlotItem();
    }
}
