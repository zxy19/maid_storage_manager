package studio.fantasyit.maid_storage_manager.menu.craft.altar;

import com.github.tartaricacid.touhoulittlemaid.crafting.AltarRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.menu.container.CountSlot;
import studio.fantasyit.maid_storage_manager.menu.container.FilterSlot;
import studio.fantasyit.maid_storage_manager.menu.craft.base.AbstractCraftMenu;
import studio.fantasyit.maid_storage_manager.network.CraftGuideGuiPacket;
import studio.fantasyit.maid_storage_manager.registry.GuiRegistry;
import studio.fantasyit.maid_storage_manager.util.RecipeUtil;

import java.util.Optional;

public class AltarCraftMenu extends AbstractCraftMenu<AltarCraftMenu> {
    float ppcost = -1;

    public AltarCraftMenu(int p_38852_, Player player) {
        super(GuiRegistry.CRAFT_GUIDE_MENU_ALTAR.get(), p_38852_, player);
    }

    @Override
    protected void addFilterSlots() {
        int i = 0;
        this.addSlot(new FilterSlot(stepDataContainer,
                i++,
                28,
                93)
        );
        this.addSlot(new FilterSlot(stepDataContainer,
                i++,
                28,
                73)
        );
        this.addSlot(new FilterSlot(stepDataContainer,
                i++,
                48,
                53)
        );
        this.addSlot(new FilterSlot(stepDataContainer,
                i++,
                68,
                53)
        );
        this.addSlot(new FilterSlot(stepDataContainer,
                i++,
                88,
                73)
        );
        this.addSlot(new FilterSlot(stepDataContainer,
                i++,
                88,
                93)
        );
        this.addSlot(new FilterSlot(stepDataContainer,
                i++,
                119,
                42,
                true
        ));
        this.addDataSlot(new CountSlot(stepDataContainer.getCountMutable(6), stepDataContainer));
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

    @Override
    public void recalculateRecipe() {
        Optional<AltarRecipe> recipe = RecipeUtil.getAltarRecipe(player.level(), RecipeUtil.wrapAltarRecipeInventory(stepDataContainer.step.getInput()));
        recipe.ifPresentOrElse(craftingRecipe -> {
            ItemStack resultItem = craftingRecipe.getResultItem(player.level().registryAccess());
            stepDataContainer.setItemNoTrigger(6, resultItem);
            stepDataContainer.setCount(6, resultItem.getCount());
            ppcost = craftingRecipe.getPowerCost();
        }, () -> {
            stepDataContainer.setItemNoTrigger(6, ItemStack.EMPTY);
            ppcost = -1;
        });
    }
}
