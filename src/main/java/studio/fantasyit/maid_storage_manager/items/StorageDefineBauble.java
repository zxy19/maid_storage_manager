package studio.fantasyit.maid_storage_manager.items;

import com.github.tartaricacid.touhoulittlemaid.api.bauble.IMaidBauble;
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
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;
import studio.fantasyit.maid_storage_manager.storage.Storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class StorageDefineBauble extends MaidInteractItem implements IMaidBauble {

    public static String TAG_STORAGES = "storages";
    public static String TAG_MODE = "mode";

    public static final String TAG_STORAGE_DEFINE = "storage_define";


    public enum Mode {
        APPEND,
        REPLACE,
        REPLACE_SPEC,
        REMOVE
    }

    public StorageDefineBauble() {
        super(new Properties().stacksTo(1));
    }

    public static Mode rollMode(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains(TAG_MODE)) {
            tag.putString(TAG_MODE, Mode.APPEND.name());
        }
        Mode newMode = Mode.values()[(Mode.valueOf(tag.getString(TAG_MODE)).ordinal() + 1) % Mode.values().length];
        tag.putString(TAG_MODE, newMode.name());
        stack.setTag(tag);
        return newMode;
    }

    public static void rollMode(ItemStack stack, ServerPlayer sender, int value) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains(TAG_MODE)) {
            tag.putString(TAG_MODE, Mode.APPEND.name());
        }
        int dv = value > 0 ? 1 : Mode.values().length - 1;
        Mode newMode = Mode.values()[(Mode.valueOf(tag.getString(TAG_MODE)).ordinal() + dv) % Mode.values().length];
        tag.putString(TAG_MODE, newMode.name());
        stack.setTag(tag);
    }

    public static Mode getMode(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains(TAG_MODE)) {
            tag.putString(TAG_MODE, Mode.APPEND.name());
        }
        stack.setTag(tag);
        return Mode.valueOf(tag.getString(TAG_MODE));
    }

    public static List<Storage> getStorages(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains(TAG_STORAGES)) {
            return List.of();
        }
        ListTag listTag = tag.getList(TAG_STORAGES, 10);
        List<Storage> storages = new ArrayList<>();
        for (int i = 0; i < listTag.size(); i++) {
            storages.add(Storage.fromNbt(listTag.getCompound(i)));
        }
        return storages;
    }


    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
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
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, @NotNull Player player, @NotNull InteractionHand p_41434_) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            Mode mode = rollMode(player.getItemInHand(p_41434_));
            serverPlayer.sendSystemMessage(Component.translatable("interaction.mode_" + switch (mode) {
                case APPEND -> "append";
                case REMOVE -> "remove";
                case REPLACE -> "replace";
                case REPLACE_SPEC -> "replace_spec";
            }));
            return InteractionResultHolder.consume(player.getItemInHand(p_41434_));
        } else {
            return InteractionResultHolder.consume(player.getItemInHand(p_41434_));
        }
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        return this.useOn(context);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack itemStack,
                                @Nullable Level p_41422_,
                                @NotNull List<Component> toolTip,
                                @NotNull TooltipFlag p_41424_) {
        super.appendHoverText(itemStack, p_41422_, toolTip, p_41424_);
        Mode mode = getMode(itemStack);
        int count = getStorages(itemStack).size();
        toolTip.add(Component.translatable("interaction.mode_" + switch (mode) {
            case APPEND -> "append";
            case REMOVE -> "remove";
            case REPLACE -> "replace";
            case REPLACE_SPEC -> "replace_spec";
        }));
        toolTip.add(Component.translatable("tooltip.maid_storage_manager.storage_define_bauble.count", count));
    }
}
