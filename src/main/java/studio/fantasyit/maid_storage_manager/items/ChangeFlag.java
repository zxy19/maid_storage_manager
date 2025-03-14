package studio.fantasyit.maid_storage_manager.items;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
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
import net.minecraft.world.item.context.UseOnContext;
import studio.fantasyit.maid_storage_manager.maid.ChatTexts;
import studio.fantasyit.maid_storage_manager.maid.memory.AbstractTargetMemory;
import studio.fantasyit.maid_storage_manager.maid.task.StorageManageTask;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;
import studio.fantasyit.maid_storage_manager.storage.Storage;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

import java.util.ArrayList;
import java.util.List;

public class ChangeFlag extends Item {
    public ChangeFlag() {
        super(new Properties());
    }

    public static final String TAG_STORAGES = "storages";


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
        if (hand == InteractionHand.MAIN_HAND && living instanceof EntityMaid maid) {
            if (maid.getOwner() != null
                    && maid.getOwner().getUUID().equals(player.getUUID())
                    && maid.getTask().getUid().equals(StorageManageTask.TASK_ID)) {
                getStorages(itemStack).forEach(storage -> {
                    clearVisForMemories(maid, storage);
                    MemoryUtil.getViewedInventory(maid).addMarkChanged(storage);
                });
                clearStorages(itemStack);
                ChatTexts.send(maid, ChatTexts.CHAT_CHECK_MARK_CHANGED);
                player.sendSystemMessage(Component.translatable("interaction.flag_changed"));
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

    public static void clearVisForMemories(EntityMaid maid, Storage storage) {
        clearVisForMemories(MemoryUtil.getRequestProgress(maid), storage);
        clearVisForMemories(MemoryUtil.getViewedInventory(maid), storage);
        clearVisForMemories(MemoryUtil.getCrafting(maid), storage);
        clearVisForMemories(MemoryUtil.getPlacingInv(maid), storage);
        clearVisForMemories(MemoryUtil.getResorting(maid), storage);
    }

    public static void clearVisForMemories(AbstractTargetMemory memory, Storage storage) {
        memory.removeVisitedPos(storage);
    }
}
