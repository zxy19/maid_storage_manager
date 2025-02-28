package studio.fantasyit.maid_storage_manager.items;

import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.items.render.FilterListRenderer;
import studio.fantasyit.maid_storage_manager.menu.FilterMenu;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

public class FilterListItem extends Item implements MenuProvider {
    public static final String TAG_ITEMS = "items";
    public static final String TAG_ITEMS_ITEM = "item";
    public static final String TAG_MATCH_TAG = "match_tag";
    public static final String TAG_UUID = "uuid";
    public static final String TAG_BLACK_MODE = "black_mode";

    public FilterListItem() {
        super(
                new Properties()
                        .stacksTo(1)
        );
    }

    public static boolean matchNbt(ItemStack mainHandItem) {
        if (!mainHandItem.is(ItemRegistry.REQUEST_LIST_ITEM.get()))
            return false;
        if (!mainHandItem.hasTag())
            return false;
        CompoundTag tag = Objects.requireNonNull(mainHandItem.getTag());
        return tag.getBoolean(FilterListItem.TAG_MATCH_TAG);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, @NotNull Player player, @NotNull InteractionHand p_41434_) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, this, (buffer) -> {
            });
            return InteractionResultHolder.consume(player.getItemInHand(p_41434_));
        } else {
            return InteractionResultHolder.pass(player.getItemInHand(p_41434_));
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack itemStack,
                                @Nullable Level p_41422_,
                                @NotNull List<Component> toolTip,
                                @NotNull TooltipFlag p_41424_) {
        super.appendHoverText(itemStack, p_41422_, toolTip, p_41424_);
        toolTip.add(Component.translatable("tooltip.maid_storage_manager.filter_list.desc").withStyle(ChatFormatting.GRAY));
        if (!itemStack.hasTag()) {
            toolTip.add(Component.translatable("tooltip.maid_storage_manager.filter_list.no_tag"));
            return;
        }
        CompoundTag tag = Objects.requireNonNull(itemStack.getTag());

        if(tag.getBoolean(FilterListItem.TAG_BLACK_MODE))
            toolTip.add(Component.translatable("tooltip.maid_storage_manager.filter_list.black_mode"));
        else
            toolTip.add(Component.translatable("tooltip.maid_storage_manager.filter_list.white_mode"));


        if (!tag.contains(FilterListItem.TAG_ITEMS)) {
            toolTip.add(Component.translatable("tooltip.maid_storage_manager.request_list.open_gui_to_config"));
        } else {
            ListTag list = tag.getList(FilterListItem.TAG_ITEMS, ListTag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag itemTag = list.getCompound(i);
                if (!itemTag.contains(FilterListItem.TAG_ITEMS_ITEM)) continue;

                ItemStack itemstack = ItemStack.of(itemTag.getCompound(FilterListItem.TAG_ITEMS_ITEM));
                if (itemstack.isEmpty()) continue;

                Component component = Component.translatable("gui.maid_storage_manager.filter_list.item",
                        itemstack.getHoverName().getString());

                toolTip.add(component);
            }
        }
    }

    public static @NotNull UUID getUUID(ItemStack stack) {
        if (!stack.is(ItemRegistry.REQUEST_LIST_ITEM.get()))
            return UUID.randomUUID();
        if (!stack.hasTag())
            return UUID.randomUUID();
        CompoundTag tag = Objects.requireNonNull(stack.getTag());
        if (!tag.contains(TAG_UUID))
            tag.putUUID(TAG_UUID, UUID.randomUUID());
        return tag.getUUID(TAG_UUID);
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

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return FilterListRenderer.getInstance();
            }
        });
    }
}
