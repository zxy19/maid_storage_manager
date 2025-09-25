package studio.fantasyit.maid_storage_manager.menu.craft.altar;

import com.github.tartaricacid.touhoulittlemaid.crafting.AltarRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.menu.container.CountSlot;
import studio.fantasyit.maid_storage_manager.menu.container.FilterSlot;
import studio.fantasyit.maid_storage_manager.menu.craft.base.AbstractCraftMenu;
import studio.fantasyit.maid_storage_manager.network.CraftGuideGuiPacket;
import studio.fantasyit.maid_storage_manager.registry.DataComponentRegistry;
import studio.fantasyit.maid_storage_manager.registry.GuiRegistry;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;
import studio.fantasyit.maid_storage_manager.util.RecipeUtil;

import java.util.ArrayList;
import java.util.List;
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

    @Override
    public void recalculateRecipe() {
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < stepDataContainer.inputCount; i++) {
            items.add(stepDataContainer.getItem(i));
        }
        Optional<RecipeHolder<AltarRecipe>> recipe = RecipeUtil.getAltarRecipe(player.level(), RecipeUtil.wrapAltarRecipeInventory(items));
        recipe.ifPresentOrElse(craftingRecipe -> {
            ItemStack resultItem = craftingRecipe.value().getResultItem(player.level().registryAccess());
            if (resultItem.has(DataComponentRegistry.TO_SPAWN_ITEMS)) {
                List<ItemStack> ii = new ArrayList<>();
                resultItem.get(DataComponentRegistry.TO_SPAWN_ITEMS).forEach(itemStack -> ItemStackUtil.addToList(ii, itemStack, ItemStackUtil.MATCH_TYPE.MATCHING));
                ii.stream().findFirst()
                        .ifPresentOrElse(
                                itemStack -> {
                                    stepDataContainer.setItemNoTrigger(6, itemStack);
                                    stepDataContainer.setCount(6, itemStack.getCount());
                                },
                                () -> {
                                    stepDataContainer.setItemNoTrigger(6, ItemStack.EMPTY);
                                }
                        );
            } else {
                stepDataContainer.setItemNoTrigger(6, resultItem);
                stepDataContainer.setCount(6, resultItem.getCount());
            }
            ppcost = craftingRecipe.value().getPower();
        }, () -> {
            stepDataContainer.setItemNoTrigger(6, ItemStack.EMPTY);
            ppcost = -1;
        });
    }
}
