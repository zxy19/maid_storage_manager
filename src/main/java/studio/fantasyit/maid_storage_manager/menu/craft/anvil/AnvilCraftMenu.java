package studio.fantasyit.maid_storage_manager.menu.craft.anvil;

import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.menu.container.CountSlot;
import studio.fantasyit.maid_storage_manager.menu.container.FilterSlot;
import studio.fantasyit.maid_storage_manager.menu.container.SimpleSlot;
import studio.fantasyit.maid_storage_manager.menu.craft.base.AbstractCraftMenu;
import studio.fantasyit.maid_storage_manager.network.CraftGuideGuiPacket;
import studio.fantasyit.maid_storage_manager.registry.GuiRegistry;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.UUID;

public class AnvilCraftMenu extends AbstractCraftMenu<AnvilCraftMenu> {
    AnvilMenu anvilMenu;
    public int xpCost = -1;

    public AnvilCraftMenu(int p_38852_, Player player) {
        super(GuiRegistry.CRAFT_GUIDE_MENU_ANVIL.get(), p_38852_, player);
        anvilMenu = new AnvilMenu(p_38852_, player.getInventory());
        if (!player.level().isClientSide)
            recalculateRecipe();
    }

    @Override
    protected void addFilterSlots() {
        this.addSlot(new FilterSlot(stepDataContainer,
                0,
                28,
                73
        ));
        this.addSlot(new FilterSlot(stepDataContainer,
                1,
                77,
                73
        ));
        this.addSlot(new FilterSlot(stepDataContainer,
                2,
                135,
                73,
                true
        ));
        this.addDataSlot(new CountSlot(stepDataContainer.getCountMutable(0), stepDataContainer));
        this.addDataSlot(new CountSlot(stepDataContainer.getCountMutable(1), stepDataContainer));
        this.addDataSlot(new CountSlot(stepDataContainer.getCountMutable(2), stepDataContainer));
        this.addDataSlot(new SimpleSlot(t -> xpCost = t, () -> xpCost));
    }

    @Override
    public void handleGuiPacket(CraftGuideGuiPacket.Type type, int key, int value, @Nullable CompoundTag data) {
        switch (type) {
            case SET_ALL_INPUT -> {
                ListTag list = data.getList("inputs", 10);
                for (int i = 0; i < list.size(); i++) {
                    CompoundTag tag = list.getCompound(i);
                    ItemStack stack = ItemStackUtil.parseStack(registryAccess(), tag);
                    stepDataContainer.setItem(i, stack);
                    stepDataContainer.setCount(i, stack.getCount());
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
            case EXTRA -> {
                stepDataContainer.step.setExtraData(data);
                save();
            }
        }
    }

    public void recalculateRecipe() {
        if (player.level().isClientSide) return;
        if (this.anvilMenu == null) return;
        CompoundTag extra = stepDataContainer.step.getExtraData();
        String name = extra.getString("name");
        anvilMenu.setItem(0, 0, stepDataContainer.getItem(0).copyWithCount(stepDataContainer.getCount(0)));
        anvilMenu.setItem(1, 0, stepDataContainer.getItem(1).copyWithCount(stepDataContainer.getCount(1)));
        anvilMenu.setItemName(name);
        anvilMenu.createResult();
        stepDataContainer.setItemNoTrigger(2, anvilMenu.getSlot(2).getItem());
        stepDataContainer.setCount(2, anvilMenu.getSlot(2).getItem().getCount());
        xpCost = anvilMenu.getCost();

        Player tmpPlayer = FakePlayerFactory.get((ServerLevel) player.level(), new GameProfile(UUID.randomUUID(), "tmpPlayerForAnvilMenuCalc"));
        tmpPlayer.experienceLevel = 99999;
        anvilMenu.onTake(tmpPlayer, anvilMenu.getSlot(2).getItem());

        stepDataContainer.setItemNoTrigger(3, anvilMenu.getSlot(0).getItem());
        stepDataContainer.setCount(3, anvilMenu.getSlot(0).getItem().getCount());
        stepDataContainer.setItemNoTrigger(4, anvilMenu.getSlot(1).getItem());
        stepDataContainer.setCount(4, anvilMenu.getSlot(1).getItem().getCount());
    }
}
