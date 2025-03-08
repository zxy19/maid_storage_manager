package studio.fantasyit.maid_storage_manager.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.items.CraftGuide;
import studio.fantasyit.maid_storage_manager.menu.container.CountSlot;
import studio.fantasyit.maid_storage_manager.menu.container.FilterContainer;
import studio.fantasyit.maid_storage_manager.menu.container.FilterSlot;
import studio.fantasyit.maid_storage_manager.menu.container.ISaveFilter;
import studio.fantasyit.maid_storage_manager.network.ItemSelectorGuiPacket;
import studio.fantasyit.maid_storage_manager.registry.GuiRegistry;
import studio.fantasyit.maid_storage_manager.storage.Storage;
import studio.fantasyit.maid_storage_manager.util.RecipeUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class CraftGuideMenu extends AbstractContainerMenu implements ISaveFilter {
    Player player;
    ItemStack target;

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

    public static class TargetOps implements INBTSerializable<CompoundTag> {
        public List<Integer> slotIds;

        public FilterContainer items;
        public SimpleContainer blockItem;
        public boolean matchTag;
        Storage storage;
        Player player;
        CraftGuideMenu menu;

        public TargetOps(CraftGuideMenu menu, int slots) {
            this.menu = menu;
            this.player = menu.player;
            this.blockItem = new SimpleContainer(1);
            this.items = new FilterContainer(slots, menu);
            this.storage = null;
            this.slotIds = new ArrayList<>();
            matchTag = true;
            for (int i = 0; i < slots; i++) {
                slotIds.add(-1);
            }
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            tag.put(CraftGuide.TAG_OP_ITEMS, items.serializeNBT());
            if (storage != null)
                tag.put(CraftGuide.TAG_OP_STORAGE, storage.toNbt());
            tag.putBoolean(CraftGuide.TAG_OP_MATCH_TAG, matchTag);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            items.deserializeNBT(nbt.getList(CraftGuide.TAG_OP_ITEMS, ListTag.TAG_COMPOUND));
            if (nbt.contains(CraftGuide.TAG_OP_STORAGE))
                storage = Storage.fromNbt(nbt.getCompound(CraftGuide.TAG_OP_STORAGE));
            if (!nbt.contains(CraftGuide.TAG_OP_MATCH_TAG) || nbt.getBoolean(CraftGuide.TAG_OP_MATCH_TAG))
                matchTag = true;
            change();
        }

        protected void change() {
            if (storage != null) {
                ItemStack itemStack = player.level().getBlockState(storage.pos).getBlock().asItem().getDefaultInstance().copy();
                CompoundTag tag = itemStack.getOrCreateTag();
                tag.put("__storage", storage.toNbt());
                itemStack.setTag(tag);
                this.blockItem.setItem(0, itemStack);
            } else {
                this.blockItem.setItem(0, ItemStack.EMPTY);
            }
        }
    }

    public static class NoPlaceFilterSlot extends FilterSlot {

        private final TargetOps ops;

        public NoPlaceFilterSlot(Container handler, int index, int x, int y, TargetOps ops) {
            super(handler, index, x, y);
            this.ops = ops;
        }

        @Override
        public ItemStack safeInsert(ItemStack p_150657_, int p_150658_) {
            return p_150657_;
        }

        @Override
        public void set(ItemStack p_40240_) {
            if (p_40240_.isEmpty()) {
                ops.storage = null;
                ops.change();
            }
        }
    }

    public Map<Integer, FilterContainer> filters = new ConcurrentHashMap<>();
    public Map<Integer, Integer> iid = new ConcurrentHashMap<>();

    public TargetOps inputSlot1;
    public TargetOps inputSlot2;
    public TargetOps outputSlot;

    public CraftGuideMenu(int p_38852_, Player player) {
        super(GuiRegistry.CRAFT_GUIDE_MENU.get(), p_38852_);
        this.player = player;
        target = player.getMainHandItem();
        CompoundTag tag = target.getOrCreateTag();
        inputSlot1 = new TargetOps(this, 9);
        inputSlot2 = new TargetOps(this, 9);
        outputSlot = new TargetOps(this, 3);
        if (tag.contains(CraftGuide.TAG_INPUT_1)) {
            inputSlot1.deserializeNBT(tag.getCompound(CraftGuide.TAG_INPUT_1));
        }
        if (tag.contains(CraftGuide.TAG_INPUT_2)) {
            inputSlot2.deserializeNBT(tag.getCompound(CraftGuide.TAG_INPUT_2));
        }
        if (tag.contains(CraftGuide.TAG_OUTPUT)) {
            outputSlot.deserializeNBT(tag.getCompound(CraftGuide.TAG_OUTPUT));
        }
        addPlayerSlots();
        addFilterSlots();
        addSpecialSlots();
        recheckValidation();
    }

    public void save() {
        CompoundTag tag = target.getOrCreateTag();
        tag.put(CraftGuide.TAG_INPUT_1, inputSlot1.serializeNBT());
        tag.put(CraftGuide.TAG_INPUT_2, inputSlot2.serializeNBT());
        tag.put(CraftGuide.TAG_OUTPUT, outputSlot.serializeNBT());
        target.setTag(tag);
    }

    private void grid(int sx, int sy, int cols, int rows, TargetOps ops) {
        FilterContainer container = ops.items;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                FilterSlot slot = (FilterSlot) this.addSlot(new FilterSlot(container,
                        i * cols + j,
                        sx + j * 18,
                        sy + i * 18));
                filters.put(slot.index, container);
                ops.slotIds.set(i * cols + j, slot.index);
                iid.put(slot.index, i * cols + j);
                this.addDataSlot(new CountSlot(container.count[i * cols + j], container));
                this.addDataSlot(new SimpleSlot(
                        iii -> slot.setActive(iii == 1),
                        () -> (slot.isActive() ? 1 : 0)
                ));
            }
        }
    }

    private void recheckValidation(TargetOps ops) {
        if (ops.blockItem.isEmpty()) {
            ops.storage = null;
            ops.items.clearContent();
            filters.forEach((k, v) -> {
                if (v == ops.items) {
                    if (this.slots.get(k) instanceof FilterSlot fs) fs.setActive(false);
                }
            });
        } else {
            filters.forEach((k, v) -> {
                if (v == ops.items) {
                    if (this.slots.get(k) instanceof FilterSlot fs) fs.setActive(true);
                }
            });
        }
    }

    public void recheckValidation() {
        if (inputSlot1.blockItem.getItem(0).is(Items.CRAFTING_TABLE)) {
            inputSlot2.blockItem.setItem(0, ItemStack.EMPTY);
        }
        recheckValidation(inputSlot1);
        recheckValidation(inputSlot2);
        recheckValidation(outputSlot);
    }

    private void addFilterSlots() {
        grid(21, 19, 3, 3, inputSlot1);
        grid(21, 91, 3, 3, inputSlot2);
        grid(107, 55, 1, 3, outputSlot);
        addSlot(new NoPlaceFilterSlot(inputSlot1.blockItem, 0, 81, 34, inputSlot1));
        addSlot(new NoPlaceFilterSlot(inputSlot2.blockItem, 0, 81, 109, inputSlot2));
        addSlot(new NoPlaceFilterSlot(outputSlot.blockItem, 0, 81, 72, outputSlot));
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

    @Override
    public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
        if (slotId >= 0 && this.getSlot(slotId) instanceof NoPlaceFilterSlot fs) {
            fs.set(ItemStack.EMPTY);
            recheckValidation();
            recalcRecipe();
            save();
            return;
        }
        if (slotId >= 0 && this.getSlot(slotId) instanceof FilterSlot fs) {
            int slot = fs.getContainerSlot();
            FilterContainer container = filters.get(fs.index);
            if (clickTypeIn == ClickType.THROW)
                return;

            ItemStack held = getCarried();
            if (clickTypeIn == ClickType.CLONE) {
                if (player.isCreative() && held.isEmpty()) {
                    ItemStack stackInSlot = container.getItem(slot)
                            .copy();
                    stackInSlot.setCount(stackInSlot.getMaxStackSize());
                    setCarried(stackInSlot);
                    recalcRecipe();
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
            container.count[slot].setValue(1);
            getSlot(slotId).setChanged();
            recalcRecipe();
            save();
        } else {
            super.clicked(slotId, dragType, clickTypeIn, player);
        }
    }

    private void addSpecialSlots() {

    }

    @Override
    public void slotsChanged(Container p_38868_) {
        super.slotsChanged(p_38868_);
        recheckValidation();
        recalcRecipe();
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
                TargetOps target = null;
                int toPlace = 0;
                for (int i = 0; i < this.inputSlot1.items.getContainerSize(); i++)
                    if (ItemStack.isSameItemSameTags(this.inputSlot1.items.getItem(i), slot.getItem())) {
                        found = true;
                    } else if (target == null && this.inputSlot1.items.getItem(i).isEmpty()) {
                        target = this.inputSlot1;
                        toPlace = i;
                    }
                for (int i = 0; i < this.inputSlot2.items.getContainerSize(); i++)
                    if (ItemStack.isSameItemSameTags(this.inputSlot2.items.getItem(i), slot.getItem())) {
                        found = true;
                    } else if (target == null && this.inputSlot2.items.getItem(i).isEmpty()) {
                        target = this.inputSlot2;
                        toPlace = i;
                    }
                if (!found) {
                    if (target != null) {
                        target.items.setItem(toPlace, slot.getItem().copyWithCount(1));
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


    public void handleUpdate(ItemSelectorGuiPacket.SlotType type, int key, int value) {
        switch (type) {
            case COUNT -> {
                if (filters.containsKey(key) && iid.containsKey(key)) {
                    filters.get(key).count[iid.get(key)].setValue(value);
                    save();
                }
            }
        }
    }

    @Override
    public boolean canDragTo(Slot p_38945_) {
        return !(p_38945_ instanceof FilterSlot);
    }

    public void recalcRecipe() {
        if (inputSlot1.blockItem.getItem(0).is(Items.CRAFTING_TABLE.asItem())) {
            inputSlot2.blockItem.setItem(0, ItemStack.EMPTY);
            inputSlot2.items.clearContent();
            inputSlot2.storage = null;
            CraftingContainer container = RecipeUtil.wrapContainer(inputSlot1.items, 3, 3);
            RecipeUtil.getRecipe(player.level(), container)
                    .ifPresentOrElse(recipe -> {
                        outputSlot.blockItem.setItem(0, inputSlot1.blockItem.getItem(0));
                        outputSlot.storage = inputSlot1.storage;
                        ItemStack item = recipe.assemble(container, player.level().registryAccess());
                        outputSlot.items.setItem(0, item);
                        outputSlot.items.count[0].setValue(item.getCount());
                    }, () -> {
                        outputSlot.blockItem.setItem(0, ItemStack.EMPTY);
                        outputSlot.storage = null;
                        outputSlot.items.clearContent();
                    });
        } else if (inputSlot2.blockItem.getItem(0).is(Items.CRAFTING_TABLE.asItem())) {
            inputSlot2.blockItem.setItem(0, ItemStack.EMPTY);
            inputSlot2.storage = null;
        }
    }
}
