package studio.fantasyit.maid_storage_manager.items;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
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
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.maid.ChatTexts;
import studio.fantasyit.maid_storage_manager.maid.memory.AbstractTargetMemory;
import studio.fantasyit.maid_storage_manager.maid.task.StorageManageTask;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;
import studio.fantasyit.maid_storage_manager.storage.Storage;
import studio.fantasyit.maid_storage_manager.util.InvUtil;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;
import studio.fantasyit.maid_storage_manager.util.MoveUtil;
import studio.fantasyit.maid_storage_manager.util.PosUtil;

import java.util.ArrayList;
import java.util.List;

public class ChangeFlag extends Item {
    public ChangeFlag() {
        super(new Properties().stacksTo(1));
    }

    public static final String TAG_STORAGES = "storages";

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        return this.useOn(context);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!context.getLevel().isClientSide && context.getPlayer() instanceof ServerPlayer serverPlayer) {
            BlockPos clickedPos = context.getClickedPos();
            Direction side = serverPlayer.isShiftKeyDown() ? null : context.getClickedFace();
            Storage validTarget = MaidStorage.getInstance().isValidTarget((ServerLevel) context.getLevel(), serverPlayer, clickedPos, side);
            if (validTarget != null) {
                ItemStack item = serverPlayer.getMainHandItem();
                CompoundTag tag = item.getOrCreateTag();
                if (!tag.contains(TAG_STORAGES)) {
                    tag.put(TAG_STORAGES, new ListTag());
                }
                ListTag list = tag.getList(TAG_STORAGES, 10);
                boolean found = false;
                for (int i = 0; i < list.size(); i++) {
                    Storage storage = Storage.fromNbt(list.getCompound(i));
                    if (storage.equals(validTarget)) {
                        list.remove(i);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    list.add(validTarget.toNbt());
                }
            }
            return InteractionResult.CONSUME;
        } else {
            return InteractionResult.CONSUME;
        }
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack itemStack, Player player, LivingEntity living, InteractionHand hand) {
        if (!player.level().isClientSide && hand == InteractionHand.MAIN_HAND && living instanceof EntityMaid maid) {
            ServerLevel level = (ServerLevel) player.level();
            if (maid.getOwner() != null
                    && maid.getOwner().getUUID().equals(player.getUUID())
                    && maid.getTask().getUid().equals(StorageManageTask.TASK_ID)) {
                List<Storage> storages = getStorages(itemStack);
                if (storages.size() == 0) {
                    return InteractionResult.CONSUME;
                }
                storages.forEach(interactedTarget -> {
                    Storage target;
                    List<Storage> possibleTargets = MoveUtil.findTargetRewrite(level, maid, interactedTarget.withoutSide());
                    if (possibleTargets.contains(interactedTarget))
                        target = interactedTarget;
                    else if (possibleTargets.size() > 0)
                        target = possibleTargets.get(0);
                    else
                        return;
                    Storage storage = MemoryUtil.getViewedInventory(maid).ambitiousPos(level, target);
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

    public static List<Storage> getStorages(ItemStack stack) {
        if (!stack.is(ItemRegistry.CHANGE_FLAG.get()) && !stack.hasTag())
            return List.of();
        List<Storage> storages = new ArrayList<>();
        ListTag tags = stack.getOrCreateTag().getList(TAG_STORAGES, 10);
        for (int i = 0; i < tags.size(); i++) {
            storages.add(Storage.fromNbt(tags.getCompound(i)));
        }
        return storages;
    }

    public static void clearStorages(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.put(TAG_STORAGES, new ListTag());
        stack.setTag(tag);
    }

    public static void clearVisForMemories(ServerLevel level, EntityMaid maid, Storage storage) {
        clearVisForMemories(level, MemoryUtil.getRequestProgress(maid), storage);
        clearVisForMemories(level, MemoryUtil.getViewedInventory(maid), storage);
        clearVisForMemories(level, MemoryUtil.getCrafting(maid), storage);
        clearVisForMemories(level, MemoryUtil.getPlacingInv(maid), storage);
        clearVisForMemories(level, MemoryUtil.getResorting(maid), storage);
    }

    public static void clearVisForMemories(ServerLevel level, AbstractTargetMemory memory, Storage storage) {
        memory.removeVisitedPos(storage);
        InvUtil.checkNearByContainers(level, storage.getPos(), pos -> {
            memory.removeVisitedPos(storage.sameType(pos, null));
        });
    }

    @Override
    public void appendHoverText(@NotNull ItemStack itemStack, @Nullable Level p_41422_, List<Component> tooltip, TooltipFlag p_41424_) {
        super.appendHoverText(itemStack, p_41422_, tooltip, p_41424_);
        CompoundTag tag = itemStack.getOrCreateTag();
        tooltip.add(Component.translatable("tooltip.maid_storage_manager.change_flag.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.maid_storage_manager.change_flag.storages", getStorages(itemStack).size()));
    }

}
