package studio.fantasyit.maid_storage_manager.menu.craft.furnace;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraftforge.common.ForgeHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideRenderData;
import studio.fantasyit.maid_storage_manager.menu.container.FilterSlot;
import studio.fantasyit.maid_storage_manager.menu.container.ISaveFilter;
import studio.fantasyit.maid_storage_manager.menu.craft.base.ICraftGuiPacketReceiver;
import studio.fantasyit.maid_storage_manager.menu.craft.base.StepDataContainer;
import studio.fantasyit.maid_storage_manager.menu.craft.common.CommonCraftMenu;
import studio.fantasyit.maid_storage_manager.menu.craft.common.CommonStepDataContainer;
import studio.fantasyit.maid_storage_manager.network.CraftGuideGuiPacket;
import studio.fantasyit.maid_storage_manager.registry.GuiRegistry;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;
import studio.fantasyit.maid_storage_manager.util.RecipeUtil;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class FurnaceCraftMenu extends AbstractContainerMenu implements ISaveFilter, ICraftGuiPacketReceiver {

    public static class SimpleSlot extends DataSlot {
        Consumer<Integer> set;
        Supplier<Integer> get;

        public SimpleSlot(Consumer<Integer> set, Supplier<Integer> get) {
            this.set = set;
            this.get = get;
        }

        @Override
        public int get() {
            return get.get();
        }

        @Override
        public void set(int p_39402_) {
            set.accept(p_39402_);
        }
    }

    Player player;
    ItemStack target;
    CraftGuideData craftGuideData;
    StepDataContainer stepDataContainer = null;

    public FurnaceCraftMenu(int p_38852_, Player player) {
        super(GuiRegistry.CRAFT_GUIDE_MENU_FURNACE.get(), p_38852_);
        this.player = player;
        target = player.getMainHandItem();
        craftGuideData = CraftGuideData.fromItemStack(target);
        stepDataContainer = new StepDataContainer(craftGuideData.getSteps().get(0), this);
        addFilterSlots();
        addPlayerSlots();
        addSpecialSlots();
    }

    private void addFilterSlots() {
        int i = 0;
        this.addSlot(new FilterSlot(stepDataContainer,
                i++,
                45,
                45)
        );
        this.addSlot(new FilterSlot(stepDataContainer,
                i++,
                45,
                81)
        );
        this.addSlot(new FilterSlot(stepDataContainer,
                i++,
                105,
                63
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
        addDataSlot(new CommonCraftMenu.SimpleSlot(
                t -> stepDataContainer.setCount(0, t),
                () -> stepDataContainer.getCount(0)
        ));
        addDataSlot(new CommonCraftMenu.SimpleSlot(
                t -> stepDataContainer.setCount(1, t),
                () -> stepDataContainer.getCount(1)
        ));
        addDataSlot(new CommonCraftMenu.SimpleSlot(
                t -> stepDataContainer.setCount(2, t),
                () -> stepDataContainer.getCount(2)
        ));
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
            container.setItemNoTrigger(slot, insert);
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
                        stepDataContainer.setItemNoTrigger(toPlace, slot.getItem().copyWithCount(1));
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
                    ItemStack stack = ItemStack.of( list.getCompound(i));
                    stepDataContainer.setItemNoTrigger(0, stack);
                }
                if (stepDataContainer.getItem(1).isEmpty()) {
                    stepDataContainer.setItemNoTrigger(1, Items.COAL.getDefaultInstance());
                }
                save();
            }
            case SET_ITEM -> {
                if (data != null) {
                    this.getSlot(key).set(ItemStack.of(data));
                    save();
                }
            }

            case COUNT -> {
                stepDataContainer.setCount(key, value);
                stepDataContainer.setChanged();
            }
        }
    }

    public void recalculateRecipe() {
        if (ForgeHooks.getBurnTime(stepDataContainer.getItem(1), RecipeType.SMELTING) == 0) {
            stepDataContainer.setItemNoTrigger(1, ItemStack.EMPTY);
            return;
        }
        Optional<SmeltingRecipe> recipe = RecipeUtil.getSmeltingRecipe(player.level(), stepDataContainer.getItem(0).copyWithCount(1));
        recipe.ifPresentOrElse(craftingRecipe -> {
            ItemStack resultItem = craftingRecipe.getResultItem(player.level().registryAccess());
            stepDataContainer.setItemNoTrigger(2, resultItem);
            stepDataContainer.setCount(2, resultItem.getCount() * stepDataContainer.getCount(0));
        }, () -> {
            stepDataContainer.setItemNoTrigger(2, ItemStack.EMPTY);
        });
    }
}
