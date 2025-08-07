package studio.fantasyit.maid_storage_manager.items;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitTrigger;
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
import studio.fantasyit.maid_storage_manager.advancement.AdvancementTypes;
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
    public static final String TAG_STYLE = "style";

    public enum Selecting {
        Viewing,
        Style,
        Merge
    }

    public enum Viewing {
        WORKING,
        DONE,
        WAITING
    }

    public enum Style {
        NORMAL,
        SMALL
    }

    public enum Merge {
        NONE,
        OVERFLOW_ONLY,
        ALWAYS
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

    public static Style getStyle(ItemStack itemStack) {
        if (!itemStack.hasTag())
            return Style.NORMAL;
        CompoundTag tag = Objects.requireNonNull(itemStack.getTag());
        if (!tag.contains(TAG_STYLE))
            return Style.NORMAL;
        return Style.valueOf(tag.getString(TAG_STYLE));
    }

    public static void setStyle(ItemStack itemStack, Style style) {
        CompoundTag tag = Objects.requireNonNull(itemStack.getTag());
        tag.putString(TAG_STYLE, style.name());
        itemStack.setTag(tag);
    }

    public static void rollStyle(ItemStack itemStack, ServerPlayer player, int value) {
        Style style = getStyle(itemStack);
        style = Style.values()[(style.ordinal() + Style.values().length + value) % Style.values().length];
        player.sendSystemMessage(Component.translatable("interaction.progress_pad.style." + style.name().toLowerCase()), false);
        setStyle(itemStack, style);
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
        serverPlayer.sendSystemMessage(Component.translatable("interaction.progress_pad.viewing." + selectId.name().toLowerCase()), false);
        setViewing(itemInHand, selectId);
    }

    public static Merge getMerge(ItemStack itemStack) {
        if (!itemStack.hasTag())
            return Merge.OVERFLOW_ONLY;
        CompoundTag tag = Objects.requireNonNull(itemStack.getTag());
        if (!tag.contains("merge"))
            return Merge.OVERFLOW_ONLY;
        return Merge.valueOf(tag.getString("merge"));
    }

    public static void setMerge(ItemStack itemStack, Merge merge) {
        CompoundTag tag = itemStack.getOrCreateTag();
        tag.putString("merge", merge.name());
        itemStack.setTag(tag);
    }

    public static void rollMerge(ItemStack itemStack, ServerPlayer player, int value) {
        Merge merge = getMerge(itemStack);
        merge = Merge.values()[(merge.ordinal() + value + Merge.values().length) % Merge.values().length];
        player.sendSystemMessage(Component.translatable("interaction.progress_pad.merge." + merge.name().toLowerCase()), false);
        setMerge(itemStack, merge);
    }

    public static Selecting getSelecting(ItemStack itemStack) {
        if (!itemStack.hasTag())
            return Selecting.Viewing;
        CompoundTag tag = Objects.requireNonNull(itemStack.getTag());
        if (!tag.contains("selecting"))
            return Selecting.Viewing;
        return Selecting.valueOf(tag.getString("selecting"));
    }

    public static void setSelecting(ItemStack itemStack, Selecting selecting) {
        CompoundTag tag = itemStack.getOrCreateTag();
        tag.putString("selecting", selecting.name());
        itemStack.setTag(tag);
    }

    public static void rollSelecting(ItemStack itemStack, ServerPlayer player, int value) {
        Selecting selecting = getSelecting(itemStack);
        selecting = Selecting.values()[(selecting.ordinal() + value + Selecting.values().length) % Selecting.values().length];
        player.sendSystemMessage(Component.translatable("interaction.progress_pad.selecting." + selecting.name().toLowerCase()), false);
        setSelecting(itemStack, selecting);
    }

    public static void rollValue(ItemStack itemStack, ServerPlayer player, int value) {
        switch (getSelecting(itemStack)) {
            case Viewing -> rollViewing(itemStack, player, value);
            case Style -> rollStyle(itemStack, player, value);
            case Merge -> rollMerge(itemStack, player, value);
        }
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack itemStack, Player p_41399_, LivingEntity entity, InteractionHand p_41401_) {
        if (!p_41399_.level().isClientSide && p_41401_ == InteractionHand.MAIN_HAND && entity instanceof EntityMaid maid) {
            if (p_41399_.getUUID().equals(maid.getOwner().getUUID())) {
                setBindingUUID(itemStack, maid.getUUID());

                InitTrigger.MAID_EVENT.trigger((ServerPlayer) p_41399_, AdvancementTypes.PROGRESS_PAD);
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
        ProgressData data = MaidProgressData.getByMaid(ProgressData.ProgressMeta.fromItemStack(stack));
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
        if (bindingUUID != null) {
            ProgressData byMaid = MaidProgressData.getByMaid(ProgressData.ProgressMeta.fromItemStack(itemStack));
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

    @Override
    public boolean allowClickThrough() {
        return false;
    }
}
