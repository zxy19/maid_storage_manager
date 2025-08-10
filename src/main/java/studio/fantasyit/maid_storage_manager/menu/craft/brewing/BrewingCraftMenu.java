package studio.fantasyit.maid_storage_manager.menu.craft.brewing;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.menu.container.CountSlot;
import studio.fantasyit.maid_storage_manager.menu.container.FilterSlot;
import studio.fantasyit.maid_storage_manager.menu.craft.base.AbstractCraftMenu;
import studio.fantasyit.maid_storage_manager.network.CraftGuideGuiPacket;
import studio.fantasyit.maid_storage_manager.registry.GuiRegistry;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;
import studio.fantasyit.maid_storage_manager.util.RecipeUtil;

import java.util.Optional;

public class BrewingCraftMenu extends AbstractCraftMenu<BrewingCraftMenu> {

    public BrewingCraftMenu(int p_38852_, Player player) {
        super(GuiRegistry.CRAFT_GUIDE_MENU_BREWING.get(), p_38852_, player);
    }

    protected void addFilterSlots() {
        int i = 0;
        this.addSlot(new FilterSlot(stepDataContainer,
                        i++,
                        35,
                        65,
                        true
                )
        );
        this.addSlot(new FilterSlot(stepDataContainer,
                i++,
                79,
                72)
        );
        this.setSlotFilter(i - 1, BrewingRecipeRegistry::isValidInput);
        this.addSlot(new FilterSlot(stepDataContainer,
                i++,
                61,
                29)
        );
        this.setSlotFilter(i - 1, BrewingRecipeRegistry::isValidIngredient);
        this.addSlot(new FilterSlot(stepDataContainer,
                i++,
                124,
                72,
                true
        ));
    }


    @Override
    protected void addSpecialSlots() {
        this.addDataSlot(new CountSlot(stepDataContainer.getCountMutable(0), stepDataContainer));
        this.addDataSlot(new CountSlot(stepDataContainer.getCountMutable(1), stepDataContainer));
        this.addDataSlot(new CountSlot(stepDataContainer.getCountMutable(2), stepDataContainer));
        this.addDataSlot(new CountSlot(stepDataContainer.getCountMutable(3), stepDataContainer));
    }

    @Override
    public void handleGuiPacket(CraftGuideGuiPacket.Type type, int key, int value, @Nullable CompoundTag data) {
        switch (type) {
            case SET_ALL_INPUT -> {
                ListTag list = data.getList("inputs", 10);
                int dIndex = 0;
                if (list.size() > 3) {
                    dIndex = 2;
                }
                for (int i = dIndex; i < list.size(); i++) {
                    ItemStack stack = ItemStackUtil.parseStack(list.getCompound(i));
                    stepDataContainer.setItemNoTrigger(i + 1 - dIndex, stack);
                }
                save();
            }
            case SET_ITEM -> {
                if (data != null) {
                    this.getSlot(key).set(ItemStackUtil.parseStack(data));
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
        stepDataContainer.setItemNoTrigger(0, Items.BLAZE_POWDER.getDefaultInstance());
        ItemStack i1 = stepDataContainer.getItem(1);
        ItemStack i2 = stepDataContainer.getItem(2);
        Optional<IBrewingRecipe> recipe = RecipeUtil.getBrewingRecipe(player.level(), i1, i2);
        recipe.ifPresentOrElse(craftingRecipe -> {
            ItemStack resultItem = craftingRecipe.getOutput(i1, i2);
            stepDataContainer.setItemNoTrigger(3, resultItem);
            stepDataContainer.setCount(3, resultItem.getCount() * stepDataContainer.getCount(1));
        }, () -> {
            stepDataContainer.setItemNoTrigger(3, ItemStack.EMPTY);
        });
    }
}
