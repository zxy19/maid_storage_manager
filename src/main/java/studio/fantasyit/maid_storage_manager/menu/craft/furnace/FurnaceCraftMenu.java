package studio.fantasyit.maid_storage_manager.menu.craft.furnace;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.menu.container.CountSlot;
import studio.fantasyit.maid_storage_manager.menu.container.FilterSlot;
import studio.fantasyit.maid_storage_manager.menu.craft.base.AbstractCraftMenu;
import studio.fantasyit.maid_storage_manager.network.CraftGuideGuiPacket;
import studio.fantasyit.maid_storage_manager.registry.GuiRegistry;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;
import studio.fantasyit.maid_storage_manager.util.RecipeUtil;

import java.util.Optional;

public class FurnaceCraftMenu extends AbstractCraftMenu<FurnaceCraftMenu> {

    public FurnaceCraftMenu(int p_38852_, Player player) {
        super(GuiRegistry.CRAFT_GUIDE_MENU_FURNACE.get(), p_38852_, player);
    }

    protected void addFilterSlots() {
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
                63,
                true
        ));
    }


    @Override
    protected void addSpecialSlots() {
        this.addDataSlot(new CountSlot(stepDataContainer.getCountMutable(0), stepDataContainer));
        this.addDataSlot(new CountSlot(stepDataContainer.getCountMutable(1), stepDataContainer));
        this.addDataSlot(new CountSlot(stepDataContainer.getCountMutable(2), stepDataContainer));
    }

    @Override
    public void handleGuiPacket(CraftGuideGuiPacket.Type type, int key, int value, @Nullable CompoundTag data) {
        switch (type) {
            case SET_ALL_INPUT -> {
                ListTag list = data.getList("inputs", 10);
                for (int i = 0; i < list.size(); i++) {
                    ItemStack stack = ItemStackUtil.parseStack(registryAccess(), list.getCompound(i));
                    stepDataContainer.setItemNoTrigger(0, stack);
                }
                if (stepDataContainer.getItem(1).isEmpty()) {
                    stepDataContainer.setItemNoTrigger(1, Items.COAL.getDefaultInstance());
                }
                save();
            }
            case SET_ITEM -> {
                if (data != null) {
                    this.getSlot(key).set(ItemStackUtil.parseStack(registryAccess(), data));
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
        if (stepDataContainer.getItem(1).getBurnTime(RecipeType.SMELTING) == 0) {
            stepDataContainer.setItemNoTrigger(1, ItemStack.EMPTY);
            return;
        }
        Optional<RecipeHolder<SmeltingRecipe>> recipe = RecipeUtil.getSmeltingRecipe(player.level(), stepDataContainer.getItem(0).copyWithCount(1));
        recipe.ifPresentOrElse(craftingRecipe -> {
            ItemStack resultItem = craftingRecipe.value().getResultItem(player.level().registryAccess());
            stepDataContainer.setItemNoTrigger(2, resultItem);
            stepDataContainer.setCount(2, resultItem.getCount() * stepDataContainer.getCount(0));
        }, () -> {
            stepDataContainer.setItemNoTrigger(2, ItemStack.EMPTY);
        });
    }
}
