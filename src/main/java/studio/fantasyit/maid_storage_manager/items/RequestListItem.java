package studio.fantasyit.maid_storage_manager.items;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.api.IRequestTaskHandler;
import studio.fantasyit.maid_storage_manager.items.data.RequestItemStackList;
import studio.fantasyit.maid_storage_manager.menu.request.ItemSelectorMenu;
import studio.fantasyit.maid_storage_manager.registry.DataComponentRegistry;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;
import studio.fantasyit.maid_storage_manager.storage.Target;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class RequestListItem extends MaidInteractItem implements MenuProvider {

    public RequestListItem() {
        super(new Properties()
                .stacksTo(1)
                .component(DataComponentRegistry.REQUEST_ITEMS.get(), new RequestItemStackList().toImmutable())
        );
    }


    @Override
    public @NotNull InteractionResult use(Level level, @NotNull Player player, @NotNull InteractionHand p_41434_) {
        if (player.isShiftKeyDown()) return InteractionResult.PASS;
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            IRequestTaskHandler handler = IRequestTaskHandler.of(serverPlayer.getMainHandItem());
            if (handler == null || !handler.isVirtual(serverPlayer.getMainHandItem()))
                serverPlayer.openMenu(this, (buffer) -> {
                });
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack itemStack, Player player, LivingEntity entity, InteractionHand p_41401_) {
        if (!player.level().isClientSide() && p_41401_ == InteractionHand.MAIN_HAND) {
            if (player.isShiftKeyDown()) {
                if (itemStack.has(DataComponentRegistry.REQUEST_STORAGE_ENTITY)) {
                    itemStack.remove(DataComponentRegistry.REQUEST_STORAGE_ENTITY);
                } else {
                    if (itemStack.has(DataComponentRegistry.REQUEST_STORAGE_BLOCK))
                        itemStack.remove(DataComponentRegistry.REQUEST_STORAGE_BLOCK);
                    itemStack.set(DataComponentRegistry.REQUEST_STORAGE_ENTITY, entity.getUUID());
                }
                return InteractionResult.SUCCESS;
            } else if (entity instanceof EntityMaid) {
                IRequestTaskHandler handler = IRequestTaskHandler.of(itemStack);
                if (handler != null && !handler.hasAnyStorage(itemStack)) {
                    itemStack.remove(DataComponentRegistry.REQUEST_STORAGE_BLOCK);
                    itemStack.set(DataComponentRegistry.REQUEST_STORAGE_ENTITY, player.getUUID());
                }
            }
        }
        return super.interactLivingEntity(itemStack, player, entity, p_41401_);
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
        if (!context.getLevel().isClientSide() && context.getPlayer() instanceof ServerPlayer serverPlayer) {
            if (!serverPlayer.isShiftKeyDown()) return InteractionResult.PASS;
            BlockPos clickedPos = context.getClickedPos();
            Target validTarget = MaidStorage.getInstance().isValidTarget((ServerLevel) context.getLevel(), serverPlayer, clickedPos);
            if (validTarget != null) {
                ItemStack item = serverPlayer.getMainHandItem();
                if (item.has(DataComponentRegistry.REQUEST_STORAGE_ENTITY)) {
                    item.remove(DataComponentRegistry.REQUEST_STORAGE_ENTITY);
                }
                if (item.has(DataComponentRegistry.REQUEST_STORAGE_BLOCK)) {
                    Target storage = Objects.requireNonNull(item.get(DataComponentRegistry.REQUEST_STORAGE_BLOCK));
                    if (storage.getPos().equals(clickedPos) && storage.getSide().isPresent() && storage.getSide().get() == context.getClickedFace()) {
                        item.remove(DataComponentRegistry.REQUEST_STORAGE_BLOCK);
                        serverPlayer.sendSystemMessage(Component.translatable("interaction.clear_storage"));
                    } else {
                        if (storage.pos.equals(clickedPos)) {
                            storage.side = context.getClickedFace();
                        } else {
                            storage.pos = clickedPos;
                            storage.side = null;
                        }
                        serverPlayer.sendSystemMessage(Component.translatable("interaction.bind_storage", clickedPos.getX(), clickedPos.getY(), clickedPos.getZ()));
                        item.set(DataComponentRegistry.REQUEST_STORAGE_BLOCK, storage);
//                        TourGuideTrigger.trigger(serverPlayer, "request_list_bind");
                    }
                } else {
                    item.set(DataComponentRegistry.REQUEST_STORAGE_BLOCK, validTarget);
                    serverPlayer.sendSystemMessage(Component.translatable("interaction.bind_storage", clickedPos.getX(), clickedPos.getY(), clickedPos.getZ()));
                    //TODO bind Trigger
                }
            }
            return InteractionResult.SUCCESS;
        } else {
            if (Objects.requireNonNull(context.getPlayer()).isShiftKeyDown()) return InteractionResult.SUCCESS;
            return InteractionResult.PASS;
        }
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext p_339594_, TooltipDisplay tooltipDisplay, Consumer<Component> toolTip, TooltipFlag p_41424_) {
        super.appendHoverText(itemStack, p_339594_, tooltipDisplay, toolTip, p_41424_);

        toolTip.accept(Component.translatable("tooltip.maid_storage_manager.request_list.desc").withStyle(ChatFormatting.GRAY));

        if (Boolean.TRUE.equals(itemStack.get(DataComponentRegistry.REQUEST_VIRTUAL))) {
            toolTip.accept(Component.translatable("tooltip.maid_storage_manager.request_list.virtual").withStyle(ChatFormatting.RED));
        }
        if (itemStack.has(DataComponentRegistry.REQUEST_STORAGE_ENTITY)) {
            String tuuid = itemStack.get(DataComponentRegistry.REQUEST_STORAGE_ENTITY).toString().substring(0, 8);
            toolTip.accept(Component.translatable("tooltip.maid_storage_manager.request_list.entity", tuuid));
        } else if (itemStack.has(DataComponentRegistry.REQUEST_STORAGE_BLOCK)) {
            Target storage = itemStack.get(DataComponentRegistry.REQUEST_STORAGE_BLOCK);
            BlockPos storagePos = storage.getPos();
            toolTip.accept(Component.translatable("tooltip.maid_storage_manager.request_list.storage", storagePos.getX(), storagePos.getY(), storagePos.getZ()));
        } else {
            toolTip.accept(Component.translatable("tooltip.maid_storage_manager.request_list.no_storage"));
        }

        IRequestTaskHandler handler = IRequestTaskHandler.of(itemStack);
        RequestItemStackList.Immutable request = handler != null ? handler.getImmutableRequestData(itemStack) : null;
        if (request != null) {
            List<RequestItemStackList.ImmutableItem> list = request.list();
            for (int i = 0; i < list.size(); i++) {
                RequestItemStackList.ImmutableItem tmp = list.get(i);
                ItemStack itemstack = tmp.item();
                if (itemstack.isEmpty()) continue;


                Component component = Component.translatable("gui.maid_storage_manager.written_inventory_list.request_item_info",
                        itemstack.getHoverName().getString(),
                        tmp.collected(),
                        String.valueOf(tmp.requested() == -1 ? "*" : tmp.requested()));

                if (tmp.done()) {
                    if (tmp.collected() >= tmp.requested() || tmp.requested() == -1) {
                        component = component.copy().withStyle(ChatFormatting.GREEN);
                    } else {
                        component = component.copy().withStyle(ChatFormatting.RED);
                    }
                } else {
                    component = component.copy().withStyle(ChatFormatting.GRAY);
                }
                toolTip.accept(component);
            }
        }

        if (itemStack.getOrDefault(DataComponentRegistry.REQUEST_INTERVAL, 0) > 0) {
            if (itemStack.getOrDefault(DataComponentRegistry.REQUEST_CD_UNIT, false))
                toolTip.accept(Component.translatable("tooltip.maid_storage_manager.request_list.repeat_interval_second", itemStack.getOrDefault(DataComponentRegistry.REQUEST_INTERVAL, 0) / 20));
            else
                toolTip.accept(Component.translatable("tooltip.maid_storage_manager.request_list.repeat_interval_tick", itemStack.getOrDefault(DataComponentRegistry.REQUEST_INTERVAL, 0)));
            if (itemStack.getOrDefault(DataComponentRegistry.REQUEST_CD, 0) > 0) {
                int cd = itemStack.getOrDefault(DataComponentRegistry.REQUEST_CD, 0);
                if (itemStack.getOrDefault(DataComponentRegistry.REQUEST_CD_UNIT, false))
                    cd /= 20;
                toolTip.accept(Component.translatable("tooltip.maid_storage_manager.request_list.cooling_down", cd).withStyle(ChatFormatting.GREEN));
            }
        }
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.literal("");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int p_39954_, Inventory p_39955_, Player p_39956_) {
        ItemStack mainHand = p_39956_.getItemInHand(InteractionHand.MAIN_HAND);
        IRequestTaskHandler handler = IRequestTaskHandler.of(mainHand);
        if (handler != null && handler.isVirtual(mainHand))
            return null;
        return new ItemSelectorMenu(p_39954_, p_39956_);
    }
}
