package studio.fantasyit.maid_storage_manager.menu.craft.smithing;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SmithingRecipe;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.menu.container.FilterSlot;
import studio.fantasyit.maid_storage_manager.menu.craft.base.AbstractCraftMenu;
import studio.fantasyit.maid_storage_manager.network.CraftGuideGuiPacket;
import studio.fantasyit.maid_storage_manager.registry.GuiRegistry;
import studio.fantasyit.maid_storage_manager.util.RecipeUtil;

import java.util.Optional;

public class SmithingCraftMenu extends AbstractCraftMenu<SmithingCraftMenu> {

    public SmithingCraftMenu(int p_38852_, Player player) {
        super(GuiRegistry.CRAFT_GUIDE_MENU_SMITHING.get(), p_38852_,player);
    }

    @Override
    protected void addFilterSlots() {
        this.addSlot(new FilterSlot(stepDataContainer,
                0,
                34,
                69
        ));
        this.addSlot(new FilterSlot(stepDataContainer,
                1,
                52,
                69
        ));
        this.addSlot(new FilterSlot(stepDataContainer,
                2,
                70,
                69
        ));
        this.addSlot(new FilterSlot(stepDataContainer,
                3,
                126,
                69,
                true
        ));
    }
    @Override
    public void handleGuiPacket(CraftGuideGuiPacket.Type type, int key, int value, @Nullable CompoundTag data) {
        switch (type) {
            case SET_ALL_INPUT -> {
                ListTag list = data.getList("inputs", 10);
                for (int i = 0; i < list.size(); i++) {
                    CompoundTag tag = list.getCompound(i);
                    ItemStack stack = ItemStack.of(tag);
                    stepDataContainer.setItemNoTrigger(i, stack);
                }
                for (int i = list.size(); i < stepDataContainer.getContainerSize(); i++) {
                    stepDataContainer.setItemNoTrigger(i, ItemStack.EMPTY);
                }
                save();
            }
            case SET_ITEM -> {
                if (data != null) {
                    this.getSlot(key).set(ItemStack.of(data));
                    save();
                }
            }
        }
    }

    public void recalculateRecipe() {
        Optional<SmithingRecipe> recipe = RecipeUtil.getSmithingRecipe(player.level(), stepDataContainer.step.getInput());
        recipe.ifPresentOrElse(smithingRecipe -> {
            ItemStack resultItem = smithingRecipe.getResultItem(player.level().registryAccess());
            stepDataContainer.setItemNoTrigger(3, resultItem);
        }, () -> {
            stepDataContainer.setItemNoTrigger(3, ItemStack.EMPTY);
        });
    }
}
