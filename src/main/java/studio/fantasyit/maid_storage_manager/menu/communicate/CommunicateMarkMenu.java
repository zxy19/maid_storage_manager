package studio.fantasyit.maid_storage_manager.menu.communicate;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.communicate.data.ConfigurableCommunicateData;
import studio.fantasyit.maid_storage_manager.communicate.data.SlotType;
import studio.fantasyit.maid_storage_manager.communicate.data.TaskDefaultCommunicate;
import studio.fantasyit.maid_storage_manager.items.ConfigurableCommunicateTerminal;
import studio.fantasyit.maid_storage_manager.menu.container.FilterContainer;
import studio.fantasyit.maid_storage_manager.menu.container.FilterSlot;
import studio.fantasyit.maid_storage_manager.menu.container.ISaveFilter;
import studio.fantasyit.maid_storage_manager.menu.container.SimpleSlot;
import studio.fantasyit.maid_storage_manager.network.CommunicateMarkGuiPacket;
import studio.fantasyit.maid_storage_manager.network.Network;
import studio.fantasyit.maid_storage_manager.registry.GuiRegistry;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.function.Consumer;

public class CommunicateMarkMenu extends AbstractContainerMenu implements ISaveFilter {
    Player player;
    ItemStack itemStack;

    FilterContainer filteredItems;
    ConfigurableCommunicateData data;
    ConfigurableCommunicateData.Item item;
    SimpleContainer workCard = new SimpleContainer(1);
    boolean isManual;
    int selected = 0;
    Consumer<CommunicateMarkGuiPacket> screenPacketHandler = null;


    public CommunicateMarkMenu(int windowId, Player player) {
        super(GuiRegistry.COMMUNICATE_MARK_MENU.get(), windowId);
        this.player = player;
        itemStack = player.getMainHandItem();
        if (itemStack.is(ItemRegistry.CONFIGURABLE_COMMUNICATE_MARK.get()))
            data = ConfigurableCommunicateTerminal.getDataFrom(player.getMainHandItem(), null);
        data = ConfigurableCommunicateData.toFixedLength(data);
        isManual = ConfigurableCommunicateTerminal.isManual(itemStack);
        item = data.items.get(selected);
        workCard.setItem(0, ConfigurableCommunicateTerminal.getWorkCardItem(itemStack));
        initSlots();
        addPlayerSlots();
        updateCurrentSelected();
    }

    public void save() {
        if (player.level().isClientSide)
            return;
        for (int i = 0; i < 8; i++)
            item.requires.set(i, filteredItems.getItem(i));
        data.items.set(selected, item);
        CompoundTag tag = itemStack.getOrCreateTag();
        tag.putBoolean("manual", isManual);
        if (isManual)
            tag.put("data", data.toNbt());
        else if (tag.contains("data"))
            tag.remove("data");
        tag.putInt("cd", 0);
        itemStack.setTag(tag);
        ConfigurableCommunicateTerminal.setWorkCardItem(itemStack, workCard.getItem(0));
        sendPacketToOpposite(new CommunicateMarkGuiPacket(
                CommunicateMarkGuiPacket.Type.DATA,
                selected,
                0,
                item.toNbt()
        ));
    }

    public void setScreenPacketHandler(Consumer<CommunicateMarkGuiPacket> screenPacketHandler) {
        this.screenPacketHandler = screenPacketHandler;
    }

    public void sendPacketToOpposite(CommunicateMarkGuiPacket packet) {
        if (player.level().isClientSide)
            Network.INSTANCE.sendToServer(packet);
        else
            Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), packet);
    }

    private void initSlots() {
        filteredItems = new FilterContainer(8, this);
        for (int i = 0; i < 8; i++) {
            this.addSlot(new FilterSlot(filteredItems, i, 76 + i % 4 * 18, 104 + i / 4 * 18));
        }
        this.addSlot(new Slot(workCard, 0, 139, 19) {
            @Override
            public boolean mayPlace(ItemStack p_40231_) {
                return p_40231_.is(ItemRegistry.WORK_CARD.get());
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }

            @Override
            public void setChanged() {
                super.setChanged();
                save();
            }
        });
        this.addDataSlot(new SimpleSlot(i -> selected = i, () -> selected));
        this.addDataSlot(new SimpleSlot(i -> isManual = i == 1, () -> isManual ? 1 : 0));
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


    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player p_38941_, int p_38942_) {
        Slot slot = this.getSlot(p_38942_);
        if (slot.hasItem()) {
            if (slot instanceof FilterSlot fs) {
                fs.set(ItemStack.EMPTY);
            } else if (slot.getItem().is(ItemRegistry.WORK_CARD.get()) && workCard.isEmpty()) {
                workCard.setItem(0, slot.getItem().copyWithCount(1));
                ItemStack itemStack1 = slot.getItem().copy();
                itemStack1.shrink(1);
                return itemStack1;
            } else {
                int containerSize = this.filteredItems.getContainerSize();
                boolean found = false;
                for (int i = 0; i < containerSize; i++)
                    if (ItemStack.isSameItemSameTags(this.filteredItems.getItem(i), slot.getItem()))
                        found = true;
                if (!found) {
                    for (int i = 0; i < containerSize; i++) {
                        if (this.filteredItems.getItem(i).isEmpty()) {
                            this.filteredItems.setItem(i, slot.getItem().copyWithCount(1));
                            break;
                        }
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    public void handlePacket(CommunicateMarkGuiPacket p) {
        switch (p.type) {
            case MAX -> item.max = p.value;
            case MIN -> item.min = p.value;
            case THRESHOLD -> item.thresholdCount = p.value;
            case SET_ITEM -> {
                item.requires.set(p.key, ItemStackUtil.parseStack(p.data));
                updateCurrentSelected();
            }
            case SET_ITEMS -> {
                ListTag list = p.data.getList("list", CompoundTag.TAG_COMPOUND);
                for (int i = 0; i < Math.min(list.size(), item.requires.size()); i++) {
                    ItemStack stack = ItemStackUtil.parseStack(list.getCompound(i));
                    item.requires.set(i, stack);
                }
                updateCurrentSelected();
            }
            case MATCH -> item.match = ItemStackUtil.MATCH_TYPE.values()[p.value];
            case WHITE_MODE -> item.whiteMode = p.value == 1;
            case DATA -> {
                data.items.set(p.key, ConfigurableCommunicateData.Item.fromNbt(p.data));
                if (selected == p.key) {
                    item = data.items.get(selected);
                }
                updateCurrentSelected();
            }
            case SLOT -> item.slot = SlotType.values()[p.value];
            case MANUAL -> isManual = p.value == 1;
            case SELECT -> {
                selected = p.key;
                updateCurrentSelected();
            }
            case USE_ID -> {
                ResourceLocation id = ResourceLocation.tryParse(CommunicateMarkGuiPacket.getStringFrom(p.data));
                ConfigurableCommunicateData configurableCommunicateData = TaskDefaultCommunicate.get(id);
                if (configurableCommunicateData != null) {
                    data = ConfigurableCommunicateData.toFixedLength(ConfigurableCommunicateData.fromNbt(configurableCommunicateData.toNbt()));
                    for (int i = 0; i < data.items.size(); i++) {
                        if (i != selected)
                            sendPacketToOpposite(new CommunicateMarkGuiPacket(CommunicateMarkGuiPacket.Type.DATA, i, data.items.get(i).toNbt()));
                    }
                    item = data.items.get(selected);
                    updateCurrentSelected();
                }
                isManual = !TaskDefaultCommunicate.DUMMY_AUTO_DETECT_TASK.equals(id);
            }
        }
        if (p.type != CommunicateMarkGuiPacket.Type.DATA)
            save();
        if (screenPacketHandler != null)
            screenPacketHandler.accept(p);
    }

    public void updateCurrentSelected() {
        item = data.items.get(selected);
        for (int i = 0; i < 8; i++)
            filteredItems.setItemNoTrigger(i, item.requires.get(i));
    }

    @Override
    public boolean stillValid(Player p_38874_) {
        return p_38874_.getMainHandItem() == itemStack;
    }

    @Override
    public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
        if (slotId >= 0 && this.getSlot(slotId) instanceof FilterSlot fs) {
            int slot = fs.getContainerSlot();
            if (clickTypeIn == ClickType.THROW)
                return;

            ItemStack held = getCarried();
            if (clickTypeIn == ClickType.CLONE) {
                if (player.isCreative() && held.isEmpty()) {
                    ItemStack stackInSlot = filteredItems.getItem(slot).copy();
                    setCarried(stackInSlot);
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
            filteredItems.setItem(slot, insert);
        } else {
            super.clicked(slotId, dragType, clickTypeIn, player);
        }
    }
}
