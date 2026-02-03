package studio.fantasyit.maid_storage_manager.menu.craft.common;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.craft.CraftManager;
import studio.fantasyit.maid_storage_manager.craft.action.CraftAction;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideRenderData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.generator.algo.DummyCollector;
import studio.fantasyit.maid_storage_manager.craft.generator.type.base.IAutoCraftGuideGenerator;
import studio.fantasyit.maid_storage_manager.menu.container.FilterSlot;
import studio.fantasyit.maid_storage_manager.menu.container.ISaveFilter;
import studio.fantasyit.maid_storage_manager.menu.container.NoPlaceFilterSlot;
import studio.fantasyit.maid_storage_manager.menu.container.SimpleSlot;
import studio.fantasyit.maid_storage_manager.menu.craft.base.ICraftGuiPacketReceiver;
import studio.fantasyit.maid_storage_manager.network.CraftGuideGeneratorUpdate;
import studio.fantasyit.maid_storage_manager.network.CraftGuideGuiPacket;
import studio.fantasyit.maid_storage_manager.network.Network;
import studio.fantasyit.maid_storage_manager.registry.GuiRegistry;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CommonCraftMenu extends AbstractContainerMenu implements ISaveFilter, ICraftGuiPacketReceiver {
    Player player;
    ItemStack target;
    CraftGuideData craftGuideData;
    public int selectedIndex = -1;
    public int selectedGeneratorIndex = -1;
    public CommonStepDataContainer currentEditingItems = new CommonStepDataContainer(this);
    public FilterSlot[] filterSlots = new FilterSlot[4];
    public NoPlaceFilterSlot blockIndicator;
    public boolean isHandRelated = false;


    public DummyCollector generatedRecipes;
    public boolean generatedUpdated = false;
    public boolean hasGeneratorResult = false;
    public boolean selectedGenerator = false;


    public CommonCraftMenu(int p_38852_, Player player) {
        super(GuiRegistry.CRAFT_GUIDE_MENU_COMMON.get(), p_38852_);
        this.player = player;
        target = player.getMainHandItem();
        craftGuideData = CraftGuideData.fromItemStack(target);
        generatedRecipes = new DummyCollector(player.level().registryAccess());
        addFilterSlots();
        addPlayerSlots();
        addSpecialSlots();
        updateHasGeneratorAvailable();
    }

    private void addFilterSlots() {
        int sx = 113;
        int sy = 24;
        int dx = 20;
        int dy = 18;
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                FilterSlot slot = (FilterSlot) this.addSlot(new FilterSlot(currentEditingItems, x * 2 + y, sx + x * dx, sy + y * dy));
                filterSlots[x * 2 + y] = slot;
                slot.setActive(false);
            }
        }
        blockIndicator = (NoPlaceFilterSlot) this.addSlot(new NoPlaceFilterSlot(130, 70, ItemStack.EMPTY, 0));
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
        for (int i = 0; i < currentEditingItems.getContainerSize(); i++) {
            int finalI = i;
            addDataSlot(new SimpleSlot(
                    t -> currentEditingItems.setCount(finalI, t),
                    () -> currentEditingItems.getCount(finalI)
            ));
        }
        addDataSlot(new SimpleSlot(
                t -> isHandRelated = t == 1,
                () -> isHandRelated ? 1 : 0
        ));
        addDataSlot(new SimpleSlot(
                t -> craftGuideData.isNoOccupy(t != 0),
                () -> craftGuideData.isNoOccupy() ? 1 : 0
        ));
        addDataSlot(new SimpleSlot(
                t -> craftGuideData.isMergeable(t != 0),
                () -> craftGuideData.isMergeable() ? 1 : 0
        ));
        addDataSlot(new SimpleSlot(
                t -> hasGeneratorResult = t != 0,
                () -> hasGeneratorResult ? 1 : 0
        ));
    }

    @Override
    public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
        if (slotId >= 0 && this.getSlot(slotId) instanceof NoPlaceFilterSlot fs) {
            fs.set(ItemStack.EMPTY);
            save();
            return;
        }
        if (slotId >= 0 && this.getSlot(slotId) instanceof FilterSlot fs && fs.container == currentEditingItems) {
            int slot = fs.getContainerSlot();
            if (clickTypeIn == ClickType.THROW)
                return;

            ItemStack held = getCarried();
            if (clickTypeIn == ClickType.CLONE) {
                if (player.isCreative() && held.isEmpty()) {
                    ItemStack stackInSlot = currentEditingItems.getItem(slot)
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
            currentEditingItems.setItemNoTrigger(slot, insert);
            currentEditingItems.setCount(slot, 1);
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
        if (selectedIndex == -1) return ItemStack.EMPTY;
        if (slot.hasItem()) {
            if (slot instanceof FilterSlot fs) {
                fs.set(ItemStack.EMPTY);
            } else {
                boolean found = false;
                CommonStepDataContainer target = null;
                int toPlace = -1;
                for (int j = 0; j < currentEditingItems.getContainerSize(); j++) {
                    if (currentEditingItems.getItem(j).isEmpty()) {
                        if (toPlace == -1) {
                            toPlace = j;
                        }
                    } else if (ItemStackUtil.isSameInCrafting(currentEditingItems.getItem(j), slot.getItem())) {
                        found = true;
                    }
                }


                if (!found) {
                    if (toPlace != -1) {
                        currentEditingItems.setItemNoTrigger(toPlace, slot.getItem().copyWithCount(1));
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
        if (currentEditingItems.step != null && selectedIndex != -1 && player instanceof ServerPlayer sp) {
            currentEditingItems.save();
            Network.INSTANCE.send(
                    PacketDistributor.PLAYER.with(() -> sp),
                    new CraftGuideGuiPacket(CraftGuideGuiPacket.Type.SYNC, selectedIndex, 0, currentEditingItems.step.toCompound())
            );
        }
        craftGuideData.saveToItemStack(target);
        craftGuideData.buildInputAndOutputs();
        CraftGuideRenderData.recalculateItemStack(target);
    }

    @Override
    public void handleGuiPacket(CraftGuideGuiPacket.Type type, int key, int value, @Nullable CompoundTag data) {
        switch (type) {
            case REMOVE -> {
                craftGuideData.getSteps().remove(key);
                if (key == selectedIndex) {
                    selectedIndex = -1;
                    currentEditingItems.clearStep();
                }
                updateHasGeneratorAvailable();
                save();
            }
            case SELECT -> {
                if (key == craftGuideData.getSteps().size()) {
                    currentEditingItems.clearStep();
                    selectedIndex = -1;
                    blockIndicator.set(ItemStack.EMPTY);
                    recalcSlots();
                    recalculateGenerator();
                    selectedGenerator = true;
                } else {
                    selectedGenerator = false;
                    if (selectedIndex == key) {
                        currentEditingItems.clearStep();
                        selectedIndex = -1;
                        blockIndicator.set(ItemStack.EMPTY);
                        recalcSlots();
                        return;
                    }
                    selectedIndex = key;
                    currentEditingItems.setStep(craftGuideData.getSteps().get(key));
                    blockIndicator.set(player.level().getBlockState(craftGuideData.getSteps().get(key).storage.pos).getBlock().asItem().getDefaultInstance());
                    isHandRelated = craftGuideData.getSteps().get(key).actionType.hasMark(CraftAction.MARK_HAND_RELATED);
                    recalcSlots();
                }
            }
            case DOWN -> {
                if (selectedIndex < craftGuideData.getSteps().size() - 1 && selectedIndex != -1)
                    swapStep(selectedIndex, selectedIndex + 1);
                updateHasGeneratorAvailable();
            }
            case UP -> {
                if (selectedIndex > 0)
                    swapStep(selectedIndex, selectedIndex - 1);
                updateHasGeneratorAvailable();
            }
            case SET_MODE -> {
                if (data != null) {
                    ResourceLocation action = new ResourceLocation(data.getString("ns"), data.getString("id"));
                    currentEditingItems.setAction(action);
                    recalcSlots();
                    save();
                }
            }
            case OPTION -> {
                if (data != null)
                    currentEditingItems.setOption(key, value, data.getString("value"));
                save();
            }
            case SET_ITEM -> {
                if (data != null) {
                    this.getSlot(key).set(ItemStackUtil.parseStack(data));
                    save();
                }
            }
            case COUNT -> {
                if (getSlot(key).container instanceof CommonStepDataContainer commonStepDataContainer) {
                    commonStepDataContainer.setCount(getSlot(key).getContainerSlot(), value);
                    commonStepDataContainer.setChanged();
                }
            }
            case SIDE -> {
                if (value == -1)
                    currentEditingItems.step.storage.side = null;
                else
                    currentEditingItems.step.storage.side = Direction.values()[value];
                save();
            }
            case SET_ALL_INPUT -> {
                if (player instanceof ServerPlayer sp) {
                    ListTag inputTag = data.getList("inputs", Tag.TAG_COMPOUND);
                    ListTag outputTag = data.getList("outputs", Tag.TAG_COMPOUND);
                    int inputId = 0;
                    int outputId = 0;
                    CommonStepDataContainer _tmp = new CommonStepDataContainer(this);
                    for (int id = 0; id < craftGuideData.steps.size(); id++) {
                        CommonStepDataContainer step = id == selectedIndex ? currentEditingItems : _tmp;
                        if (id != selectedIndex)
                            step.setStep(craftGuideData.steps.get(id));
                        for (int i = 0; i < step.step.actionType.inputCount(); i++) {
                            if (inputId < inputTag.size()) {
                                ItemStack tmp = ItemStackUtil.parseStack(inputTag.getCompound(inputId));
                                step.setItemNoTrigger(i, tmp);
                                step.setCount(i, tmp.getCount());
                                inputId++;
                            }
                        }
                        for (int i = 0; i < step.step.actionType.outputCount(); i++) {
                            if (outputId < outputTag.size()) {
                                int inputOffset = step.padCount + step.inputCount;
                                ItemStack tmp = ItemStackUtil.parseStack(outputTag.getCompound(outputId));
                                step.setItemNoTrigger(inputOffset + i, tmp);
                                step.setCount(inputOffset + i, tmp.getCount());
                                outputId++;
                            }
                        }
                        if (id != selectedIndex) {
                            step.save();
                            Network.INSTANCE.send(
                                    PacketDistributor.PLAYER.with(() -> sp),
                                    new CraftGuideGuiPacket(CraftGuideGuiPacket.Type.SYNC, id, 0, step.step.toCompound())
                            );
                        }
                    }
                }
                save();
            }
            case SYNC -> {
                if (data != null)
                    craftGuideData.steps.set(key, CraftGuideStepData.fromCompound(data));
            }
            case SYNC_ALL -> {
                currentEditingItems.clearStep();
                if (data != null) {
                    craftGuideData = CraftGuideData.fromCompound(data);
                }
            }
            case GLOBAL -> {
                switch (key) {
                    case 0 -> craftGuideData.isMergeable(value == 1);
                    case 1 -> craftGuideData.isNoOccupy(value == 1);
                    default -> {
                    }
                }
                save();
            }
            case GENERATOR -> {
                if (generatedRecipes.craftGuideSuppliers.size() > value) {
                    List<Integer> counts = generatedRecipes.counts.get(value);
                    ListTag inputs = data.getList("inputs", Tag.TAG_COMPOUND);
                    List<ItemStack> inputsList = new ArrayList<>();
                    for (int i = 0; i < inputs.size(); i++) {
                        inputsList.add(ItemStackUtil.parseStack(inputs.getCompound(i)).copyWithCount(counts.get(i)));
                    }
                    CraftGuideData apply = generatedRecipes.craftGuideSuppliers.get(value).apply(inputsList);
                    if (apply != null) {
                        currentEditingItems.clearStep();
                        craftGuideData = apply;
                        this.selectedIndex = -1;
                        this.selectedGenerator = false;
                        recalcSlots();
                        Network.INSTANCE.send(
                                PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
                                new CraftGuideGuiPacket(CraftGuideGuiPacket.Type.SYNC_ALL, 0, 0, apply.toCompound(new CompoundTag()))
                        );
                    }
                }
                save();
            }
        }
    }

    private void updateHasGeneratorAvailable() {
        if (player.level().isClientSide)
            return;
        if (craftGuideData.getSteps().isEmpty())
            return;
        hasGeneratorResult = false;
        CraftGuideStepData step0 = craftGuideData.getSteps().get(0);
        BlockPos pos = step0.storage.pos;
        List<IAutoCraftGuideGenerator> autoCraftGuideGenerators = CraftManager.getInstance().getAutoCraftGuideGenerators();
        for (IAutoCraftGuideGenerator autoCraftGuideGenerator : autoCraftGuideGenerators) {
            if (autoCraftGuideGenerator.isBlockValid(player.level(), pos)) {
                hasGeneratorResult = true;
                return;
            }
        }
    }

    private void recalculateGenerator() {
        if (player.level().isClientSide)
            return;
        if (craftGuideData.getSteps().isEmpty())
            return;
        CraftGuideStepData step0 = craftGuideData.getSteps().get(0);
        BlockPos pos = step0.storage.pos;
        generatedRecipes.clear();
        List<IAutoCraftGuideGenerator> autoCraftGuideGenerators = CraftManager.getInstance().getAutoCraftGuideGenerators();
        for (IAutoCraftGuideGenerator autoCraftGuideGenerator : autoCraftGuideGenerators) {
            if (autoCraftGuideGenerator.isBlockValid(player.level(), pos)) {
                autoCraftGuideGenerator.generate(
                        new ArrayList<>(),
                        player.level(),
                        pos,
                        generatedRecipes,
                        new HashMap<>()
                );
            }
        }
        Network.INSTANCE.send(
                PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
                new CraftGuideGeneratorUpdate(generatedRecipes)
        );
    }


    private void recalcSlots() {
        for (int i = 0; i < filterSlots.length; i++) {
            if (i < currentEditingItems.inputCount) {
                this.filterSlots[i].setActive(true);
            } else if (i < currentEditingItems.inputCount + currentEditingItems.padCount) {
                this.filterSlots[i].setActive(false);
            } else {
                this.filterSlots[i].setActive(true);
            }
        }
    }

    private void swapStep(int i, int j) {
        if (i == j) return;
        CraftGuideStepData tmpStepData = craftGuideData.getSteps().get(i);
        craftGuideData.getSteps().set(i, craftGuideData.getSteps().get(j));
        craftGuideData.getSteps().set(j, tmpStepData);

        if (selectedIndex == i)
            selectedIndex = j;
        else if (selectedIndex == j)
            selectedIndex = i;

        this.save();
    }
}
