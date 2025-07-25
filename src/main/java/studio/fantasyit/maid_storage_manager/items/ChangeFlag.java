package studio.fantasyit.maid_storage_manager.items;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import studio.fantasyit.maid_storage_manager.items.data.TargetList;
import studio.fantasyit.maid_storage_manager.maid.ChatTexts;
import studio.fantasyit.maid_storage_manager.maid.memory.AbstractTargetMemory;
import studio.fantasyit.maid_storage_manager.maid.task.StorageManageTask;
import studio.fantasyit.maid_storage_manager.registry.DataComponentRegistry;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;
import studio.fantasyit.maid_storage_manager.util.StorageAccessUtil;

import java.util.List;

public class ChangeFlag extends Item {
    public ChangeFlag() {
        super(
                new Properties().stacksTo(1)
                        .component(DataComponentRegistry.TARGETS, new TargetList().toImmutable())
        );
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        return this.useOn(context);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!context.getLevel().isClientSide && context.getPlayer() instanceof ServerPlayer serverPlayer && serverPlayer.isShiftKeyDown()) {
            BlockPos clickedPos = context.getClickedPos();
            Direction side = context.getClickedFace();
            Target validTarget = MaidStorage.getInstance().isValidTarget((ServerLevel) context.getLevel(), serverPlayer, clickedPos, side);
            if (validTarget != null) {
                ItemStack item = serverPlayer.getMainHandItem();
                TargetList targets = item.getOrDefault(DataComponentRegistry.TARGETS, new TargetList().toImmutable()).toMutable();
                List<Target> list = targets.targets();
                boolean found = false;
                for (int i = 0; i < list.size(); i++) {
                    Target storage = list.get(i);
                    if (storage.equals(validTarget)) {
                        list.remove(i);
                        found = true;
                        break;
                    } else if (storage.pos.equals(validTarget.pos)) {
                        list.set(i, validTarget);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    list.add(validTarget.withoutSide());
                }
                item.set(DataComponentRegistry.TARGETS, targets.toImmutable());
            }
            return InteractionResult.CONSUME;
        } else {
            return InteractionResult.PASS;
        }
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack itemStack, Player player, LivingEntity living, InteractionHand hand) {
        if (!player.level().isClientSide && hand == InteractionHand.MAIN_HAND && living instanceof EntityMaid maid) {
            ServerLevel level = (ServerLevel) player.level();
            if (maid.getOwner() != null
                    && maid.getOwner().getUUID().equals(player.getUUID())
                    && maid.getTask().getUid().equals(StorageManageTask.TASK_ID)) {
                List<Target> storages = getStorages(itemStack);
                if (storages.size() == 0) {
                    return InteractionResult.CONSUME;
                }
                storages.forEach(interactedTarget -> {
                    Target target;
                    List<Target> possibleTargets = StorageAccessUtil.findTargetRewrite(level, maid, interactedTarget.withoutSide(), false);
                    if (possibleTargets.contains(interactedTarget))
                        target = interactedTarget;
                    else if (possibleTargets.size() > 0)
                        target = possibleTargets.get(0);
                    else {
                        clearVisForMemories((ServerLevel) player.level(), maid, interactedTarget);
                        return;
                    }
                    Target storage = MemoryUtil.getViewedInventory(maid).ambitiousPos(level, target);
                    clearVisForMemories((ServerLevel) player.level(), maid, storage);
                    MemoryUtil.getViewedInventory(maid).addMarkChanged(storage);
                });
                player.sendSystemMessage(Component.translatable("interaction.flag_changed", storages.size()));
                clearStorages(itemStack);
                MemoryUtil.clearTarget(maid);
                ChatTexts.send(maid, ChatTexts.CHAT_CHECK_MARK_CHANGED);
                return InteractionResult.SUCCESS;
            }
        }
        return super.interactLivingEntity(itemStack, player, living, hand);
    }

    public static List<Target> getStorages(ItemStack stack) {
        return stack.getOrDefault(DataComponentRegistry.TARGETS, new TargetList().toImmutable()).targets();
    }

    public static void clearStorages(ItemStack stack) {
        stack.set(DataComponentRegistry.TARGETS, new TargetList().toImmutable());
    }

    public static void clearVisForMemories(ServerLevel level, EntityMaid maid, Target storage) {
        clearVisForMemories(level, MemoryUtil.getRequestProgress(maid), storage);
        clearVisForMemories(level, MemoryUtil.getViewedInventory(maid), storage);
        clearVisForMemories(level, MemoryUtil.getCrafting(maid), storage);
        clearVisForMemories(level, MemoryUtil.getPlacingInv(maid), storage);
        clearVisForMemories(level, MemoryUtil.getResorting(maid), storage);
    }

    public static void clearVisForMemories(ServerLevel level, AbstractTargetMemory memory, Target storage) {
        memory.removeVisitedPos(storage);
        StorageAccessUtil.checkNearByContainers(level, storage.getPos(), pos -> {
            memory.removeVisitedPos(storage.sameType(pos, null));
        });
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext p_339594_, List<Component> tooltip, TooltipFlag p_41424_) {
        super.appendHoverText(itemStack, p_339594_, tooltip, p_41424_);
        tooltip.add(Component.translatable("tooltip.maid_storage_manager.change_flag.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.maid_storage_manager.change_flag.storages", getStorages(itemStack).size()));
    }

}
