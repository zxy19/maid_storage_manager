package studio.fantasyit.maid_storage_manager.menu.craft.crafting_table;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
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

public class CraftingTableCraftMenu extends AbstractCraftMenu<CraftingTableCraftMenu> {
    public CraftingTableCraftMenu(int p_38852_, Player player) {
        super(GuiRegistry.CRAFT_GUIDE_MENU_CRAFTING_TABLE.get(), p_38852_, player);
    }

    @Override
    protected void addFilterSlots() {
        int sx = 31;
        int sy = 50;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                this.addSlot(new FilterSlot(stepDataContainer,
                        i * 3 + j,
                        sx + j * 18,
                        sy + i * 18));
            }
        }
        this.addSlot(new FilterSlot(stepDataContainer,
                9,
                120 + 4,
                64 + 4,
                true
        ));
        this.addDataSlot(new CountSlot(stepDataContainer.getCountMutable(9), stepDataContainer));
    }

    @Override
    public void handleGuiPacket(CraftGuideGuiPacket.Type type, int key, int value, @Nullable CompoundTag data) {
        switch (type) {
            case SET_ALL_INPUT -> {
                ListTag list = data.getList("inputs", 10);
                for (int i = 0; i < list.size(); i++) {
                    CompoundTag tag = list.getCompound(i);
                    ItemStack stack = ItemStackUtil.parseStack(registryAccess(), tag);
                    stepDataContainer.setItemNoTrigger(i, stack);
                }
                for (int i = list.size(); i < stepDataContainer.getContainerSize(); i++) {
                    stepDataContainer.setItemNoTrigger(i, ItemStack.EMPTY);
                }
                save();
            }
            case SET_ITEM -> {
                if (data != null) {
                    this.getSlot(key).set(ItemStackUtil.parseStack(registryAccess(), data));
                    save();
                }
            }
        }
    }

    public void recalculateRecipe() {
        Optional<RecipeHolder<CraftingRecipe>> recipe = RecipeUtil.getCraftingRecipe(player.level(), RecipeUtil.wrapCraftingContainer(stepDataContainer, 3, 3).asCraftInput());
        recipe.ifPresentOrElse(craftingRecipe -> {
            List<ItemStack> result = new ArrayList<>();
            result.add(craftingRecipe.value().assemble(RecipeUtil.wrapCraftingContainer(stepDataContainer, 3, 3).asCraftInput(), player.level().registryAccess()));
            NonNullList<ItemStack> remain = craftingRecipe.value().getRemainingItems(RecipeUtil.wrapCraftingContainer(stepDataContainer, 3, 3).asCraftInput());
            remain.forEach(i -> ItemStackUtil.addToList(result, i, true));
            for (int i = 0; i < stepDataContainer.outputCount; i++) {
                if (i < result.size()) {
                    stepDataContainer.setItemNoTrigger(i + stepDataContainer.inputCount, result.get(i));
                    stepDataContainer.setCount(i + stepDataContainer.inputCount, result.get(i).getCount());
                } else {
                    stepDataContainer.setItemNoTrigger(i + stepDataContainer.inputCount, ItemStack.EMPTY);
                    stepDataContainer.setCount(i + stepDataContainer.inputCount, 0);
                }
            }
        }, () -> {
            for (int i = 0; i < stepDataContainer.outputCount; i++) {
                stepDataContainer.setItemNoTrigger(i + stepDataContainer.inputCount, ItemStack.EMPTY);
                stepDataContainer.setCount(i + stepDataContainer.inputCount, 0);
            }
        });
    }
}
