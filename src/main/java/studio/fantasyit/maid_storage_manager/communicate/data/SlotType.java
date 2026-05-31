package studio.fantasyit.maid_storage_manager.communicate.data;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.inventory.handler.BaubleItemHandler;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.transfer.CombinedResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.menu.base.ImageAsset;
import studio.fantasyit.maid_storage_manager.util.InvUtil;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public enum SlotType {
    ALL(Identifier.fromNamespaceAndPath(MaidStorageManager.MODID, "slot/empty_slot_all")),
    HEAD(InventoryMenu.EMPTY_ARMOR_SLOT_HELMET),
    CHEST(InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE),
    LEGS(InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS),
    FEET(InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS),
    MAIN_HAND(Identifier.fromNamespaceAndPath("minecraft", "item/empty_slot_sword")),
    OFF_HAND(InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD),
    FLOWER(Identifier.fromNamespaceAndPath(TouhouLittleMaid.MOD_ID, "slot/empty_back_show_slot")),
    ETA(Identifier.fromNamespaceAndPath(MaidStorageManager.MODID, "slot/empty_slot_eta")),
    BAUBLE(Identifier.fromNamespaceAndPath(MaidStorageManager.MODID, "slot/empty_bauble_slot"));

    private final ImageAsset icon;

    SlotType(@Nullable Identifier icon) {
        this.icon = icon == null ? null : new ImageAsset(Identifier.fromNamespaceAndPath(
                icon.getNamespace(),
                "textures/" + icon.getPath() + ".png"
        ), 0, 0, 16, 16, 16, 16);
    }

    public List<ItemStack> getItemStacks(EntityMaid maid) {
        List<ItemStack> list = new ArrayList<>();
        switch (this) {
            case ALL -> {
                CombinedResourceHandler<ItemResource> availableInv = maid.getAvailableInv(false);
                for (int i = 0; i < availableInv.size(); i++) {
                    list.add(ItemUtil.getStack(availableInv, i));
                }
            }
            case HEAD -> list.add(maid.getItemBySlot(EquipmentSlot.HEAD));
            case CHEST -> list.add(maid.getItemBySlot(EquipmentSlot.CHEST));
            case LEGS -> list.add(maid.getItemBySlot(EquipmentSlot.LEGS));
            case FEET -> list.add(maid.getItemBySlot(EquipmentSlot.FEET));
            case MAIN_HAND -> list.add(maid.getItemBySlot(EquipmentSlot.MAINHAND));
            case OFF_HAND -> list.add(maid.getItemBySlot(EquipmentSlot.OFFHAND));
            case BAUBLE -> {
                BaubleItemHandler bauble = maid.getMaidBauble();
                for (int i = 0; i < bauble.size(); i++) {
                    list.add(ItemUtil.getStack(bauble, i));
                }
            }
            case FLOWER -> {
                CombinedResourceHandler<ItemResource> inv = maid.getAvailableBackpackInv();
                if (inv.size() > 5)
                    list.add(ItemUtil.getStack(inv, 5));
            }
            case ETA -> {
                CombinedResourceHandler<ItemResource> inv = maid.getAvailableBackpackInv();
                for (int i = 0; i < inv.size(); i++) {
                    if (i != 5)
                        list.add(ItemUtil.getStack(inv, i));
                }
            }
        }
        return list;
    }

    public Optional<Integer> processSlotItemsAndGetIsFinished(EntityMaid maid, int startIndex, BiFunction<ItemStack, Integer, ItemStack> process) {
        switch (this) {
            case ALL -> {
                CombinedResourceHandler<ItemResource> availableInv = maid.getAvailableInv(false);
                return resetSlotItemWithProcessAndCheckIfAnyChanged(process, availableInv, startIndex);
            }
            case HEAD -> maid.setItemSlot(EquipmentSlot.HEAD, process.apply(maid.getItemBySlot(EquipmentSlot.HEAD), 0));
            case CHEST ->
                    maid.setItemSlot(EquipmentSlot.CHEST, process.apply(maid.getItemBySlot(EquipmentSlot.CHEST), 0));
            case LEGS -> maid.setItemSlot(EquipmentSlot.LEGS, process.apply(maid.getItemBySlot(EquipmentSlot.LEGS), 0));
            case FEET -> maid.setItemSlot(EquipmentSlot.FEET, process.apply(maid.getItemBySlot(EquipmentSlot.FEET), 0));
            case MAIN_HAND ->
                    maid.setItemSlot(EquipmentSlot.MAINHAND, process.apply(maid.getItemBySlot(EquipmentSlot.MAINHAND), 0));
            case OFF_HAND ->
                    maid.setItemSlot(EquipmentSlot.OFFHAND, process.apply(maid.getItemBySlot(EquipmentSlot.OFFHAND), 0));
            case BAUBLE -> {
                BaubleItemHandler bauble = maid.getMaidBauble();
                return resetBaubleSlotItemWithProcessAndCheckIfAnyChanged(process, bauble, startIndex);
            }
            case FLOWER -> {
                CombinedResourceHandler<ItemResource> inv = maid.getAvailableBackpackInv();
                if (inv.size() > 5) {
                    ItemStack oldStack = ItemUtil.getStack(inv, 5);
                    ItemStack newStack = process.apply(oldStack, 0);
                    if (!ItemStack.matches(oldStack, newStack)) {
                        replaceSlot(inv, 5, newStack);
                    }
                }
            }
            case ETA -> {
                CombinedResourceHandler<ItemResource> inv = maid.getAvailableBackpackInv();
                if (inv.size() <= 6) {
                    // FIXME: TLM 26.1 - CombinedResourceHandler doesn't support setStackInSlot.
                    // Cannot create sub-range wrapper with set capability. Needs migration.
                } else {
                    // FIXME: TLM 26.1 - Cannot compose RangedResourceHandlers into CombinedResourceHandler safely for write ops.
                }
            }
        }
        return Optional.empty();
    }

    public void iterItemExceptSlotForMaid(EntityMaid maid, Function<ItemStack, ItemStack> process) {
        // FIXME: TLM 26.1 - getAvailableInv returns CombinedResourceHandler which has no setStackInSlot.
        // Needs migration to use Transaction-based slot manipulation.
        if (this == ALL) return;
        // CombinedResourceHandler<ItemResource> inv = maid.getAvailableInv(true);
        // for (int i = 0; i < inv.size(); i++) {
        //     if (this == MAIN_HAND && i == 0) continue;
        //     if (this == OFF_HAND && i == 1) continue;
        //     if (this == FLOWER && i == 7) continue;
        //     replaceSlot(inv, i, process.apply(ItemUtil.getStack(inv, i)));
        // }
    }

    private static void replaceSlot(CombinedResourceHandler<ItemResource> handler, int index, ItemStack newStack) {
        try (Transaction tx = Transaction.open(null)) {
            ItemResource oldResource = handler.getResource(index);
            int oldAmount = (int) handler.getAmountAsLong(index);
            if (oldAmount > 0 && !oldResource.isEmpty()) {
                handler.extract(index, oldResource, oldAmount, tx);
            }
            if (!newStack.isEmpty()) {
                handler.insert(index, ItemResource.of(newStack), newStack.getCount(), tx);
            }
            tx.commit();
        }
    }

    private Optional<Integer> resetSlotItemWithProcessAndCheckIfAnyChanged(BiFunction<ItemStack, Integer, ItemStack> process, CombinedResourceHandler<ItemResource> handler, int startIndex) {
        for (int i = startIndex; i < handler.size(); i++) {
            int oCount = (int) handler.getAmountAsLong(i);
            ItemStack t = process.apply(ItemUtil.getStack(handler, i), i);
            replaceSlot(handler, i, t);
            if (t.getCount() != oCount)
                return Optional.of(i);
        }
        return Optional.empty();
    }

    private Optional<Integer> resetBaubleSlotItemWithProcessAndCheckIfAnyChanged(BiFunction<ItemStack, Integer, ItemStack> process, BaubleItemHandler bauble, int startIndex) {
        for (int i = startIndex; i < bauble.size(); i++) {
            int oCount = (int) bauble.getAmountAsLong(i);
            ItemStack t = process.apply(ItemUtil.getStack(bauble, i), i);
            bauble.set(i, ItemResource.of(t), t.getCount());
            if (t.getCount() != oCount)
                return Optional.of(i);
        }
        return Optional.empty();
    }

    public ItemStack tryPlaceItemIn(ItemStack itemStack, EntityMaid maid) {
        return switch (this) {
            case ALL -> InvUtil.tryPlace(maid.getAvailableInv(true), itemStack);
            case HEAD -> placeArmorSlot(itemStack, maid, EquipmentSlot.HEAD);
            case CHEST -> placeArmorSlot(itemStack, maid, EquipmentSlot.CHEST);
            case LEGS -> placeArmorSlot(itemStack, maid, EquipmentSlot.LEGS);
            case FEET -> placeArmorSlot(itemStack, maid, EquipmentSlot.FEET);
            case MAIN_HAND -> placeArmorSlot(itemStack, maid, EquipmentSlot.MAINHAND);
            case OFF_HAND -> placeArmorSlot(itemStack, maid, EquipmentSlot.OFFHAND);
            case BAUBLE -> InvUtil.tryPlace(maid.getMaidBauble(), itemStack);
            case FLOWER -> {
                CombinedResourceHandler<ItemResource> backpackInv = maid.getAvailableBackpackInv();
                if (backpackInv.size() > 5) {
                    ItemStack stackInSlot = ItemUtil.getStack(backpackInv, 5);
                    if (stackInSlot.isEmpty()) {
                        replaceSlot(backpackInv, 5, itemStack);
                        yield ItemStack.EMPTY;
                    }
                    if (ItemStackUtil.isSame(stackInSlot, itemStack, ItemStackUtil.MATCH_TYPE.MATCHING)) {
                        int finallyCount = Math.min(itemStack.getMaxStackSize(), itemStack.getCount() + stackInSlot.getCount());
                        replaceSlot(backpackInv, 5, itemStack.copyWithCount(finallyCount));
                        yield itemStack.copyWithCount(itemStack.getCount() - finallyCount);
                    }
                }
                yield itemStack;
            }
            case ETA -> {
                CombinedResourceHandler<ItemResource> inv = maid.getAvailableBackpackInv();
                // FIXME: TLM 26.1 - Cannot create CombinedResourceHandler from RangedResourceHandler sub-ranges for write ops.
                yield InvUtil.tryPlace(inv, itemStack);
            }
        };
    }

    private ItemStack placeArmorSlot(ItemStack itemStack, EntityMaid maid, EquipmentSlot slot) {
        if (maid.getItemBySlot(slot).isEmpty() || ItemStackUtil.isSame(maid.getItemBySlot(slot), itemStack, ItemStackUtil.MATCH_TYPE.MATCHING)) {
            int finallyCount = Math.min(itemStack.getMaxStackSize(), itemStack.getCount() + maid.getItemBySlot(slot).getCount());
            maid.setItemSlot(slot, itemStack.copyWithCount(finallyCount));
            return itemStack.copyWithCount(itemStack.getCount() - finallyCount);
        } else return itemStack;
    }

    public @Nullable ImageAsset icon() {
        return this.icon;
    }

    public Component getName() {
        return Component.translatable("slot.maid_storage_manager.communicate." + this.name().toLowerCase());
    }

    @OnlyIn(Dist.CLIENT)
    public void drawGold(GuiGraphicsExtractor graphics, int x, int y) {
        // FIXME: TLM 26.1 - GuiGraphicsExtractor.flush() and setColor() removed.
        // Needs migration to new rendering pipeline (Blaze3D render state / RenderPipelines).
        if (icon == null) return;
        icon.blit(graphics, x, y);
        // graphics.flush();
        // graphics.setColor(1.69f, 1.69f, 0.04f, 1.0f);
        // icon.blit(graphics, x + 1, y + 1);
        // graphics.flush();
        // icon.blit(graphics, x + 1, y);
        // graphics.flush();
        // graphics.setColor(2.57f, 2.03f, 0.07f, 1.0f);
        // icon.blit(graphics, x, y);
        // graphics.flush();
        // graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
}