package studio.fantasyit.maid_storage_manager.menu.craft.common;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideRenderData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.menu.container.FilterSlot;
import studio.fantasyit.maid_storage_manager.menu.container.ISaveFilter;
import studio.fantasyit.maid_storage_manager.menu.container.NoPlaceFilterSlot;
import studio.fantasyit.maid_storage_manager.menu.craft.base.ICraftGuiPacketReceiver;
import studio.fantasyit.maid_storage_manager.network.CraftGuideGuiPacket;
import studio.fantasyit.maid_storage_manager.registry.GuiRegistry;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class CommonCraftMenu extends AbstractContainerMenu implements ISaveFilter, ICraftGuiPacketReceiver {
    Player player;
    ItemStack target;
    CraftGuideData craftGuideData;
    List<CommonStepDataContainer> steps;
    List<List<FilterSlot>> pageSlots = new ArrayList<>();
    List<NoPlaceFilterSlot> targetBlockSlots = new ArrayList<>();
    public int page = 0;

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


    public CommonCraftMenu(int p_38852_, Player player) {
        super(GuiRegistry.CRAFT_GUIDE_MENU_COMMON.get(), p_38852_);
        this.player = player;
        target = player.getMainHandItem();
        craftGuideData = CraftGuideData.fromItemStack(target);
        steps = new ArrayList<>();
        for (int i = 0; i < craftGuideData.getSteps().size(); i++) {
            CraftGuideStepData step = craftGuideData.getSteps().get(i);
            steps.add(new CommonStepDataContainer(step, this));
        }
        addFilterSlots();
        addPlayerSlots();
        addSpecialSlots();
    }

    private static final int[] SLOT_Y = new int[]{28, 71, 114};
    private static final int[] SLOT_X = new int[]{38, 56, 74, 94, 113};

    private void addFilterSlots() {
        for (int i = 0; i < craftGuideData.getSteps().size(); i++) {
            CommonStepDataContainer commonStepDataContainer = steps.get(i);
            int index = pageSlots.size();
            pageSlots.add(new ArrayList<>());

            int c = 0;
            for (int j = 0; j < 3; j++) {
                FilterSlot slot = (FilterSlot) addSlot(new FilterSlot(commonStepDataContainer, c++, SLOT_X[j], SLOT_Y[i % 3]));
                pageSlots.get(index).add(slot);
                if (j == 1 && steps.get(i).inputCount > 0 && steps.get(i).outputCount > 0) {
                    slot.setActive(false);
                }
                if (index >= 3) slot.setActive(false);
            }

            BlockState state = craftGuideData.getStepByIdx(i).getStorage().getBlockStateInLevel(player.level());
            NoPlaceFilterSlot slot = (NoPlaceFilterSlot) addSlot(new NoPlaceFilterSlot(SLOT_X[4], SLOT_Y[i % 3], state.getBlock().asItem().getDefaultInstance(), i));
            targetBlockSlots.add(slot);
            if (index >= 3) slot.setActive(false);
        }
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
        for (int i = 0; i < craftGuideData.getSteps().size(); i++) {
            CommonStepDataContainer commonStepDataContainer = steps.get(i);
            for (int j = 0; j < 3; j++) {
                int finalJ = j;
                addDataSlot(new SimpleSlot(
                        t -> commonStepDataContainer.setCount(finalJ, t),
                        () -> commonStepDataContainer.getCount(finalJ)
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
                CommonStepDataContainer target = null;
                int toPlace = 0;
                for (int i = 0; i < steps.size(); i++) {
                    CraftGuideStepData step = steps.get(i).step;
                    for (int j = 0; j < steps.get(i).getContainerSize(); j++) {
                        if (steps.get(i).getItem(j).isEmpty()) {
                            if (target == null) {
                                target = steps.get(i);
                                toPlace = j;
                            }
                        } else if (ItemStackUtil.isSameInCrafting(steps.get(i).getItem(j), slot.getItem())) {
                            found = true;
                        }
                    }
                }

                if (!found) {
                    if (target != null) {
                        target.setItemNoTrigger(toPlace, slot.getItem().copyWithCount(1));
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
        if (this.player.level().isClientSide) return;
        for (CommonStepDataContainer commonStepDataContainer : steps)
            commonStepDataContainer.save();
        craftGuideData.saveToItemStack(target);
        CraftGuideRenderData.recalculateItemStack(target);
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
                pageSlots.get(key).forEach(slot -> slot.setActive(false));
                targetBlockSlots.remove(key);
                pageSlots.remove(key);
                steps.remove(key);
                craftGuideData.getSteps().remove(key);
                if (page * 3 >= steps.size())
                    setPage(page - 1);
                save();
            }
            case DOWN -> {
                if (key < steps.size() - 1)
                    swapStep(key, key + 1);
            }
            case UP -> {
                if (key > 0)
                    swapStep(key, key - 1);
            }
            case SET_MODE -> {
                if (data != null) {
                    ResourceLocation action = ResourceLocation.fromNamespaceAndPath(data.getString("ns"), data.getString("id"));
                    steps.get(key).setAction(action);
                    save();
                }
            }
            case OPTIONAL -> {
                steps.get(key).optional = !steps.get(key).optional;
                save();
            }
            case SET_ITEM -> {
                if (data != null) {
                    this.getSlot(key).set(ItemStack.of(data));
                    save();
                }
            }
            case COUNT -> {
                if (getSlot(key).container instanceof CommonStepDataContainer commonStepDataContainer) {
                    commonStepDataContainer.setCount(getSlot(key).getContainerSlot(), value);
                    commonStepDataContainer.setChanged();
                }
            }
            case EXTRA -> {
                if (data != null) {
                    steps.get(key).step.setExtraData(data);
                    save();
                }
            }
            case SIDE -> {
                if (value == -1)
                    steps.get(key).step.storage.side = null;
                else
                    steps.get(key).step.storage.side = Direction.values()[value];
                save();
            }
            case SET_ALL_INPUT -> {
                ListTag inputTag = data.getList("inputs", Tag.TAG_COMPOUND);
                ListTag outputTag = data.getList("outputs", Tag.TAG_COMPOUND);
                int inputId = 0;
                int outputId = 0;
                for (CommonStepDataContainer step : steps) {
                    for (int i = 0; i < step.step.actionType.inputCount(); i++) {
                        if (inputId < inputTag.size()) {
                            ItemStack tmp = ItemStack.of(inputTag.getCompound(inputId));
                            step.setItemNoTrigger(i, tmp);
                            step.setCount(i, tmp.getCount());
                            inputId++;
                        }
                    }
                    for (int i = 0; i < step.step.actionType.outputCount(); i++) {
                        if (outputId < outputTag.size()) {
                            int inputOffset = step.padCount + step.inputCount;
                            ItemStack tmp = ItemStack.of(outputTag.getCompound(outputId));
                            step.setItemNoTrigger(inputOffset + i, tmp);
                            step.setCount(inputOffset + i, tmp.getCount());
                            outputId++;
                        }
                    }
                }
                save();
            }
        }
        recalculateSlots();
    }

    private void recalculateSlots() {
        for (int i = 0; i < steps.size(); i++) {
            List<FilterSlot> filterSlots = pageSlots.get(i);
            for (FilterSlot filterSlot : filterSlots) {
                filterSlot.y = SLOT_Y[i % 3];
                filterSlot.setActive(isIdCurrentPage(i));
            }
            targetBlockSlots.get(i).y = SLOT_Y[i % 3];
            if (steps.get(i).inputCount > 0 && steps.get(i).outputCount > 0) {
                pageSlots.get(i).get(1).setActive(false);
            }
        }
    }

    private void swapStep(int i, int j) {
        if (i == j) return;
        CommonStepDataContainer tmpStep = steps.get(i);
        steps.set(i, steps.get(j));
        steps.set(j, tmpStep);

        List<FilterSlot> tmpPageSlot = pageSlots.get(i);
        pageSlots.set(i, pageSlots.get(j));
        pageSlots.set(j, tmpPageSlot);

        CraftGuideStepData tmpStepData = craftGuideData.getSteps().get(i);
        craftGuideData.getSteps().set(i, craftGuideData.getSteps().get(j));
        craftGuideData.getSteps().set(j, tmpStepData);

        NoPlaceFilterSlot noPlaceFilterSlot = targetBlockSlots.get(i);
        targetBlockSlots.set(i, targetBlockSlots.get(j));
        targetBlockSlots.set(j, noPlaceFilterSlot);

        this.save();
    }

    private void setPage(int page) {
        if (page * 3 - 3 >= steps.size())
            page = steps.size() / 3;
        if (page < 0)
            page = 0;
        this.page = page;
        recalculateSlots();
    }

    public boolean isIdCurrentPage(int id) {
        return id >= page * 3 && id < page * 3 + 3;
    }
}
