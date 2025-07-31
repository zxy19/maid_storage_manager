package studio.fantasyit.maid_storage_manager.items;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.craft.work.ProgressData;
import studio.fantasyit.maid_storage_manager.data.MaidProgressData;
import studio.fantasyit.maid_storage_manager.event.RenderHandMapLikeEvent;
import studio.fantasyit.maid_storage_manager.render.map_like.ProgressPadRender;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ProgressPad extends HangUpItem implements RenderHandMapLikeEvent.MapLikeRenderItem {
    public static final String TAG_BINDING_UUID = "binding_uuid";
    public static final String TAG_VIEWING = "viewing";

    public enum Viewing {
        WORKING,
        DONE,
        WAITING
    }

    public ProgressPad() {
        super(new Properties().stacksTo(1));
    }

    public static @Nullable UUID getBindingUUID(ItemStack itemStack) {
        if (!itemStack.hasTag())
            return null;
        CompoundTag tag = Objects.requireNonNull(itemStack.getTag());
        if (!tag.contains(TAG_BINDING_UUID))
            return null;
        return tag.getUUID(TAG_BINDING_UUID);
    }

    public static void setBindingUUID(ItemStack itemStack, @Nullable UUID uuid) {
        if (!itemStack.hasTag())
            itemStack.setTag(new CompoundTag());
        CompoundTag tag = Objects.requireNonNull(itemStack.getTag());
        if (uuid == null)
            tag.remove(TAG_BINDING_UUID);
        else
            tag.putUUID(TAG_BINDING_UUID, uuid);
    }

    public static Viewing getViewing(ItemStack itemStack) {
        if (!itemStack.hasTag()) return Viewing.WORKING;
        CompoundTag tag = Objects.requireNonNull(itemStack.getTag());
        if (!tag.contains(TAG_VIEWING))
            return Viewing.WORKING;
        return Viewing.valueOf(tag.getString(TAG_VIEWING));
    }

    public static void setViewing(ItemStack itemStack, Viewing viewing) {
        CompoundTag tag = itemStack.getOrCreateTag();
        tag.putString(TAG_VIEWING, viewing.name());
    }

    public static void rollViewing(ItemStack itemInHand, ServerPlayer serverPlayer, int value) {
        Viewing selectId = getViewing(itemInHand);
        selectId = Viewing.values()[((selectId.ordinal() + value + Viewing.values().length) % (Viewing.values().length))];
        setViewing(itemInHand, selectId);
    }


    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack itemStack, Player p_41399_, LivingEntity entity, InteractionHand p_41401_) {
        if (!p_41399_.level().isClientSide && p_41401_ == InteractionHand.MAIN_HAND && entity instanceof EntityMaid maid) {
            if (p_41399_.getUUID().equals(maid.getOwner().getUUID())) {
                setBindingUUID(itemStack, maid.getUUID());
                return InteractionResult.SUCCESS;
            }
        }
        return super.interactLivingEntity(itemStack, p_41399_, entity, p_41401_);
    }

    @Override
    public RenderHandMapLikeEvent.MapLikeRenderer getRenderer() {
        return ProgressPadRender.INSTANCE;
    }


    @Override
    public boolean available(ItemStack stack) {
        UUID bindingUUID = getBindingUUID(stack);
        if (bindingUUID == null) return false;
        ProgressData data = MaidProgressData.getByMaid(bindingUUID);
        return data != null;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!context.getLevel().isClientSide && getBindingUUID(context.getItemInHand()) == null)
            return InteractionResult.FAIL;
        return super.useOn(context);
    }


    public void appendHoverText(@NotNull ItemStack itemStack, @Nullable Level p_41422_, @NotNull List<Component> toolTip, @NotNull TooltipFlag p_41424_) {
        super.appendHoverText(itemStack, p_41422_, toolTip, p_41424_);
        UUID bindingUUID = getBindingUUID(itemStack);
        ProgressData byMaid = MaidProgressData.getByMaid(bindingUUID);
        if (bindingUUID != null) {
            if (byMaid == null)
                toolTip.add(Component.translatable("tooltip.maid_storage_manager.progress_pad.binding", bindingUUID.toString()));
            else
                toolTip.add(Component.translatable("tooltip.maid_storage_manager.progress_pad.binding", byMaid.maidName));
        }

        toolTip.add(switch (getViewing(itemStack)) {
            case WORKING -> Component.translatable("tooltip.maid_storage_manager.progress_pad.viewing_working");
            case DONE -> Component.translatable("tooltip.maid_storage_manager.progress_pad.viewing_done");
            case WAITING -> Component.translatable("tooltip.maid_storage_manager.progress_pad.viewing_waiting");
        });
    }
}
