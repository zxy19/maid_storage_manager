package studio.fantasyit.maid_storage_manager.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.items.data.FilterItemStackList;
import studio.fantasyit.maid_storage_manager.menu.FilterMenu;
import studio.fantasyit.maid_storage_manager.registry.DataComponentRegistry;

import java.util.List;

public class FilterListItem extends HangUpItem implements MenuProvider {
    public static final FilterItemStackList.Immutable EMPTY = new FilterItemStackList().toImmutable();

    public FilterListItem() {
        super(
                new Properties()
                        .stacksTo(1)
                        .component(DataComponentRegistry.FILTER_ITEMS, new FilterItemStackList().toImmutable())
        );
    }

    public static boolean matchNbt(ItemStack mainHandItem) {
        return mainHandItem.getOrDefault(DataComponentRegistry.FILTER_MATCH_TAG, false);
    }

    public static boolean blackList(ItemStack mainHandItem) {
        return mainHandItem.getOrDefault(DataComponentRegistry.FILTER_BLACK_MODE, false);
    }


    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, @NotNull Player player, @NotNull InteractionHand p_41434_) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            if (!serverPlayer.isShiftKeyDown())
                serverPlayer.openMenu(this, (buffer) -> {
                });
            return InteractionResultHolder.consume(player.getItemInHand(p_41434_));
        } else {
            return InteractionResultHolder.pass(player.getItemInHand(p_41434_));
        }
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext p_339594_, List<Component> toolTip, TooltipFlag p_41424_) {
        super.appendHoverText(itemStack, p_339594_, toolTip, p_41424_);
        toolTip.add(Component.translatable("tooltip.maid_storage_manager.filter_list.desc").withStyle(ChatFormatting.GRAY));

        if (blackList(itemStack))
            toolTip.add(Component.translatable("tooltip.maid_storage_manager.filter_list.black_mode"));
        else
            toolTip.add(Component.translatable("tooltip.maid_storage_manager.filter_list.white_mode"));


        List<ItemStack> list = itemStack.getOrDefault(DataComponentRegistry.FILTER_ITEMS, new FilterItemStackList().toImmutable()).list();
        for (ItemStack itemstack : list) {
            if (itemstack.isEmpty()) continue;
            Component component = Component.translatable("gui.maid_storage_manager.filter_list.item",
                    itemstack.getHoverName().getString());
            toolTip.add(component);
        }
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("gui.maid_storage_manager.filter_list.title");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int p_39954_, Inventory p_39955_, Player p_39956_) {
        return new FilterMenu(p_39954_, p_39956_);
    }
}
