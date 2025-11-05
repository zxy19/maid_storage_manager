package studio.fantasyit.maid_storage_manager.items;

import com.github.tartaricacid.touhoulittlemaid.api.bauble.IMaidBauble;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskIdle;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.api.communicate.ICommunicatable;
import studio.fantasyit.maid_storage_manager.api.communicate.data.CommunicatePlan;
import studio.fantasyit.maid_storage_manager.api.communicate.data.CommunicateRequest;
import studio.fantasyit.maid_storage_manager.api.communicate.data.CommunicateWish;
import studio.fantasyit.maid_storage_manager.api.communicate.wish.IActionWish;
import studio.fantasyit.maid_storage_manager.communicate.CommunicateUtil;
import studio.fantasyit.maid_storage_manager.communicate.data.ConfigurableCommunicateData;
import studio.fantasyit.maid_storage_manager.communicate.data.TaskDefaultCommunicate;
import studio.fantasyit.maid_storage_manager.menu.communicate.CommunicateMarkMenu;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.List;
import java.util.Optional;

public class ConfigurableCommunicateTerminal extends MaidInteractItem implements IMaidBauble, MenuProvider {

    public ConfigurableCommunicateTerminal() {
        super(new Properties());
    }


    @Override
    public void onTick(EntityMaid maid, ItemStack baubleItem) {
        if (maid.level().isClientSide)
            return;
        if (CommunicateUtil.hasCommunicateHolder(maid)) {
            if (!CommunicateUtil.getCommunicateHolder(maid).isValid())
                CommunicateUtil.clearHolder(maid);
            return;
        }
        CompoundTag tag = baubleItem.getOrCreateTag();
        tag.putString("task", maid.getTask().getUid().toString());
        int cd = tag.getInt("cd");
        if (cd > 0) {
            tag.putInt("cd", cd - 1);
            return;
        } else {
            tag.putInt("cd", 600);
        }
        ConfigurableCommunicateData data = getDataFrom(baubleItem, maid);
        if (data == null)
            return;
        List<IActionWish> iActionWishes = data.buildWish(maid);
        ItemStack workCard = getWorkCardItem(baubleItem);
        Optional<CommunicatePlan> communicatePlan = CommunicateUtil.sendCommunicateWishAndGetPlan(
                maid,
                new CommunicateWish(maid, iActionWishes),
                plan -> workCard.isEmpty() || WorkCardItem.hasBauble(plan.handler(), workCard)
        );
        communicatePlan.ifPresent(plan -> {
            if (plan.handler().getTask() instanceof ICommunicatable ic) {
                ic.startCommunicate(plan.handler(), CommunicateRequest.create(plan, maid));
            }
        });
    }

    public static ConfigurableCommunicateData getDataFrom(ItemStack stack, @Nullable EntityMaid maid) {
        if (!stack.is(ItemRegistry.CONFIGURABLE_COMMUNICATE_MARK.get()))
            return null;
        if (!isManual(stack)) {
            if (maid == null) {
                return TaskDefaultCommunicate.get(Optional.ofNullable(getLastTaskId(stack)).orElse(TaskIdle.UID));
            }
            return TaskDefaultCommunicate.get(maid.getTask().getUid());
        }
        assert stack.getTag() != null;
        return ConfigurableCommunicateData.fromNbt(stack.getTag().getCompound("data"));
    }

    public static boolean isManual(ItemStack stack) {
        if (!stack.hasTag()) return false;
        assert stack.getTag() != null;
        return stack.getTag().getBoolean("manual");
    }

    public static ResourceLocation getLastTaskId(ItemStack stack) {
        if (!stack.hasTag()) return null;
        assert stack.getTag() != null;
        return ResourceLocation.tryParse(stack.getTag().getString("task"));
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.empty();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int p_39954_, Inventory p_39955_, Player p_39956_) {
        return new CommunicateMarkMenu(p_39954_, p_39956_);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, @NotNull Player player, @NotNull InteractionHand p_41434_) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            if (!serverPlayer.isShiftKeyDown())
                NetworkHooks.openScreen(serverPlayer, this, (buffer) -> {
                    buffer.writeInt(-1);
                });
            return InteractionResultHolder.consume(player.getItemInHand(p_41434_));
        } else {
            return InteractionResultHolder.pass(player.getItemInHand(p_41434_));
        }
    }

    public static ItemStack getWorkCardItem(ItemStack item) {
        if (!item.is(ItemRegistry.CONFIGURABLE_COMMUNICATE_MARK.get()))
            return ItemStack.EMPTY;
        if (!item.hasTag())
            return ItemStack.EMPTY;
        assert item.getTag() != null;
        return ItemStackUtil.parseStack(item.getTag().getCompound("work_card"));
    }

    public static void setWorkCardItem(ItemStack item, ItemStack workCard) {
        if (!item.is(ItemRegistry.CONFIGURABLE_COMMUNICATE_MARK.get()))
            return;
        item.getOrCreateTag().put("work_card", ItemStackUtil.saveStack(workCard));
    }
}
