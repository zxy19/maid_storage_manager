package studio.fantasyit.maid_storage_manager.menu.craft.common;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.jei.IFilterScreen;
import studio.fantasyit.maid_storage_manager.menu.container.FilterSlot;
import studio.fantasyit.maid_storage_manager.menu.container.ISaveFilter;
import studio.fantasyit.maid_storage_manager.menu.craft.base.ICraftGuiPacketReceiver;
import studio.fantasyit.maid_storage_manager.menu.craft.base.StepDataContainer;
import studio.fantasyit.maid_storage_manager.network.CraftGuideGuiPacket;
import studio.fantasyit.maid_storage_manager.network.Network;
import studio.fantasyit.maid_storage_manager.registry.GuiRegistry;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class CommonCraftMenu extends AbstractContainerMenu implements ISaveFilter, IFilterScreen, ICraftGuiPacketReceiver {
    Player player;
    ItemStack target;
    CraftGuideData craftGuideData;
    List<StepDataContainer> steps;
    List<List<FilterSlot>> pageSlots = new ArrayList<>();
    private int page = 0;


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

    public static class NoPlaceFilterSlot extends FilterSlot {
        public NoPlaceFilterSlot(int x, int y, ItemStack itemStack) {
            super(new SimpleContainer(1), 0, x, y);
            this.container.setItem(0, itemStack);
        }
    }

    public CommonCraftMenu(int p_38852_, Player player) {
        super(GuiRegistry.CRAFT_GUIDE_MENU.get(), p_38852_);
        this.player = player;
        target = player.getMainHandItem();
        craftGuideData = CraftGuideData.fromItemStack(target);
        for (int i = 0; i < craftGuideData.getSteps().size(); i++) {
            CraftGuideStepData step = craftGuideData.getSteps().get(i);
            steps.add(new StepDataContainer(step, this));
        }
        addFilterSlots();
        addPlayerSlots();
        addSpecialSlots();
    }

    private static final int[] SLOT_Y = new int[]{28, 71, 114};
    private static final int[] SLOT_X = new int[]{38, 56, 114, 131, 153};

    private void addFilterSlots() {
        for (int i = 0; i < craftGuideData.getSteps().size(); i++) {
            StepDataContainer stepDataContainer = steps.get(i);
            int index = pageSlots.size();
            pageSlots.add(new ArrayList<>());

            int c = 0;
            for (int j = 0; j < Math.max(stepDataContainer.getContainerSize(), 3); j++) {
                if (stepDataContainer.getContainerSize() <= 2 && j == 1) continue;
                FilterSlot slot = (FilterSlot) addSlot(new FilterSlot(stepDataContainer, c++, SLOT_X[j], SLOT_Y[i % 3]));
                pageSlots.get(index).add(slot);
                if (index >= 3) slot.setActive(false);
            }

            BlockState state = craftGuideData.getStepByIdx(i).getStorage().getBlockStateInLevel(player.level());
            FilterSlot slot = (FilterSlot) addSlot(new NoPlaceFilterSlot(SLOT_X[4], SLOT_Y[i % 3], state.getBlock().asItem().getDefaultInstance()));
            pageSlots.get(index).add(slot);
            if (index >= 3) slot.setActive(false);
        }
    }

    private void addPlayerSlots() {
        final int cellHeight = 18;
        final int cellWidth = 18;
        final int startY = 157;
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
        for (int i = 0; i < craftGuideData.getSteps().size(); i++) {
            StepDataContainer stepDataContainer = steps.get(i);
            for (int j = 0; j < Math.max(stepDataContainer.getContainerSize(), 3); j++) {
                int finalJ = j;
                addDataSlot(new SimpleSlot(
                        t -> stepDataContainer.setCount(finalJ, t),
                        () -> stepDataContainer.getCount(finalJ)
                ));
            }
        }
        addDataSlot(new SimpleSlot(
                t -> {
                    if (page != t) setPage(t);
                },
                () -> this.page
        ));
    }

    @Override
    public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
        if (slotId >= 0 && this.getSlot(slotId) instanceof NoPlaceFilterSlot fs) {
            fs.set(ItemStack.EMPTY);
            save();
            return;
        }
        if (slotId >= 0 && this.getSlot(slotId) instanceof FilterSlot fs && fs.container instanceof StepDataContainer container) {
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
                StepDataContainer target = null;
                int toPlace = 0;
                for (int i = 0; i < steps.size(); i++) {
                    CraftGuideStepData step = steps.get(i).step;
                    for (int j = 0; j < steps.get(i).getContainerSize(); j++) {
                        if (steps.get(i).getItem(j).isEmpty()) {
                            if (target == null) {
                                target = steps.get(i);
                                toPlace = i;
                            }
                        } else if (ItemStackUtil.isSame(steps.get(i).getItem(j), slot.getItem(), step.matchTag)) {
                            found = true;
                        }
                    }
                }

                if (!found) {
                    if (target != null) {
                        target.setItem(toPlace, slot.getItem().copyWithCount(1));
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
    public void accept(FilterSlot menu, ItemStack item) {
        menu.set(item.copyWithCount(1));
    }

    @Override
    public List<FilterSlot> getSlots() {
        return this.getSlots().stream().filter(slot -> slot instanceof FilterSlot && !(slot instanceof NoPlaceFilterSlot)).toList();
    }

    @Override
    public void save() {
        for (StepDataContainer stepDataContainer : steps)
            stepDataContainer.save();
        craftGuideData.saveToItemStack(target);
    }

    @Override
    public void handleGuiPacket(CraftGuideGuiPacket.Type type, int key, int value, @Nullable CompoundTag data) {
        switch (type) {
            case PAGE_UP -> {
                setPage(page - 1);
            }
            case PAGE_DOWN -> {
                setPage(page + 1);
            }
            case REMOVE -> {
                pageSlots.remove(key);
                steps.remove(key);
                craftGuideData.getSteps().remove(key);
                if (player instanceof ServerPlayer sp) {
                    Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> sp),
                            new CraftGuideGuiPacket(CraftGuideGuiPacket.Type.REMOVE, key)
                    );
                }
            }
            case DOWN -> {

            }
        }
    }

    private void swapStep(int i, int j) {
        if (i == j) return;

    }

    private void setPage(int page) {
        if (page * 3 - 3 >= steps.size())
            page = steps.size() / 3;
        if (page < 0)
            page = 0;
        this.page = page;
        for (int i = 0; i < pageSlots.size(); i++) {
            boolean show = i < page * 3 && i >= page * 3 - 3;
            for (FilterSlot slot : pageSlots.get(i)) {
                slot.setActive(show);
            }
        }
    }
}
