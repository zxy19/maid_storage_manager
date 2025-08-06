package studio.fantasyit.maid_storage_manager.items;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitTrigger;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.advancement.AdvancementTypes;
import studio.fantasyit.maid_storage_manager.craft.work.ProgressData;
import studio.fantasyit.maid_storage_manager.data.MaidProgressData;
import studio.fantasyit.maid_storage_manager.event.RenderHandMapLikeEvent;
import studio.fantasyit.maid_storage_manager.registry.DataComponentRegistry;
import studio.fantasyit.maid_storage_manager.render.map_like.ProgressPadRender;

import java.util.List;
import java.util.UUID;

public class ProgressPad extends HangUpItem implements RenderHandMapLikeEvent.MapLikeRenderItem {
    public static final String TAG_BINDING_UUID = "binding_uuid";
    public static final String TAG_VIEWING = "viewing";
    public static final String TAG_STYLE = "style";


    public enum Viewing {
        WORKING,
        DONE,
        WAITING
    }

    public enum Style {
        NORMAL,
        SMALL
    }

    public ProgressPad() {
        super(new Properties().stacksTo(1));
    }

    public static @Nullable UUID getBindingUUID(ItemStack itemStack) {
        return itemStack.get(DataComponentRegistry.PROGRESS_PAD_BINDING);
    }

    public static void setBindingUUID(ItemStack itemStack, @Nullable UUID uuid) {
        itemStack.set(DataComponentRegistry.PROGRESS_PAD_BINDING, uuid);
    }

    public static Style getStyle(ItemStack itemStack) {
        return Style.valueOf(itemStack.getOrDefault(DataComponentRegistry.PROGRESS_PAD_STYLE, Style.NORMAL.name()));
    }

    public static void setStyle(ItemStack itemStack, Style style) {
        itemStack.set(DataComponentRegistry.PROGRESS_PAD_STYLE, style.name());
    }

    public static void rollStyle(ItemStack itemStack, ServerPlayer player, int value) {
        Style style = getStyle(itemStack);
        style = Style.values()[(style.ordinal() + Style.values().length + value) % Style.values().length];
        setStyle(itemStack, style);
    }

    public static Viewing getViewing(ItemStack itemStack) {
        return Viewing.valueOf(itemStack.getOrDefault(DataComponentRegistry.PROGRESS_PAD_VIEWING, Viewing.WORKING.name()));
    }

    public static void setViewing(ItemStack itemStack, Viewing viewing) {
        itemStack.set(DataComponentRegistry.PROGRESS_PAD_VIEWING, viewing.name());
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

                InitTrigger.MAID_EVENT.get().trigger((ServerPlayer) p_41399_, AdvancementTypes.PROGRESS_PAD);
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


    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext p_339594_, List<Component> toolTip, TooltipFlag p_41424_) {
        super.appendHoverText(itemStack, p_339594_, toolTip, p_41424_);
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

    @Override
    public boolean allowClickThrough() {
        return false;
    }
}
