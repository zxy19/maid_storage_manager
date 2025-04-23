package studio.fantasyit.maid_storage_manager.menu.craft.stone_cutter;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.menu.container.CountSlot;
import studio.fantasyit.maid_storage_manager.menu.container.FilterSlot;
import studio.fantasyit.maid_storage_manager.menu.craft.base.AbstractCraftMenu;
import studio.fantasyit.maid_storage_manager.network.CraftGuideGuiPacket;
import studio.fantasyit.maid_storage_manager.registry.GuiRegistry;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;
import studio.fantasyit.maid_storage_manager.util.RecipeUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StoneCutterCraftMenu extends AbstractCraftMenu<StoneCutterCraftMenu> {
    SimpleContainer displayOnlySlots = new SimpleContainer(15);
    List<ItemStack> availableItems = new ArrayList<>();
    public int page = 0;
    public int maxPage = 1;

    public StoneCutterCraftMenu(int p_38852_, Player player) {
        super(GuiRegistry.CRAFT_GUIDE_MENU_STONE_CUTTER.get(), p_38852_, player);
        recalculateRecipe();
    }


    @Override
    protected void addFilterSlots() {
        this.addSlot(new FilterSlot(stepDataContainer,
                0,
                47,
                39
        ));
        this.addSlot(new FilterSlot(stepDataContainer,
                1,
                109,
                39,
                true
        ));
    }

    @Override
    protected void addSpecialSlots() {
        this.addDataSlot(new CountSlot(stepDataContainer.getCountMutable(0), stepDataContainer));
        this.addDataSlot(new CountSlot(stepDataContainer.getCountMutable(1), stepDataContainer));
    }

    @Override
    public void save() {
        if (player.level().isClientSide) {
            if (Minecraft.getInstance().screen instanceof StoneCutterCraftScreen screen) {
                screen.handleGuiPacket(CraftGuideGuiPacket.Type.SET_ITEM, 0, 0, new CompoundTag());
            }
        }
        super.save();
    }

    @Override
    public void handleGuiPacket(CraftGuideGuiPacket.Type type, int key, int value, @Nullable CompoundTag data) {
        switch (type) {
            case SET_ITEM -> {
                if (data != null) {
                    this.getSlot(key).set(ItemStack.of(data));
                    recalculateRecipe();
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


    @Override
    public void recalculateRecipe() {
        if (this.displayOnlySlots == null) return;
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
