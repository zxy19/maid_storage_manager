package studio.fantasyit.maid_storage_manager.items;

import com.github.tartaricacid.touhoulittlemaid.api.bauble.IMaidBauble;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskIdle;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.api.communicate.ICommunicatable;
import studio.fantasyit.maid_storage_manager.api.communicate.data.CommunicatePlan;
import studio.fantasyit.maid_storage_manager.api.communicate.data.CommunicateRequest;
import studio.fantasyit.maid_storage_manager.api.communicate.data.CommunicateWish;
import studio.fantasyit.maid_storage_manager.api.communicate.wish.IActionWish;
import studio.fantasyit.maid_storage_manager.communicate.CommunicateUtil;
import studio.fantasyit.maid_storage_manager.communicate.data.ConfigurableCommunicateData;
import studio.fantasyit.maid_storage_manager.communicate.data.TaskDefaultCommunicate;
import studio.fantasyit.maid_storage_manager.menu.communicate.CommunicateMarkMenu;
import studio.fantasyit.maid_storage_manager.registry.DataComponentRegistry;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
        baubleItem.set(DataComponentRegistry.COMMUNICATE_LAST_TASK, maid.getTask().getUid());
        int cd = baubleItem.getOrDefault(DataComponentRegistry.COMMUNICATE_CD, 0);
        if (cd > 0) {
            if (CommunicateUtil.hasLastResult(maid) && baubleItem.has(DataComponentRegistry.COMMUNICATE_LAST_WORK_UUID)) {
                Pair<UUID, Boolean> lastResult = CommunicateUtil.getLastResult(maid);
                if (lastResult.getA().equals(baubleItem.get(DataComponentRegistry.COMMUNICATE_LAST_WORK_UUID) && lastResult.getB()) {
                    baubleItem.set(DataComponentRegistry.COMMUNICATE_CD, Config.communicateCDFinish);
                }
                CommunicateUtil.clearLastResult(maid);
            }
            baubleItem.set(DataComponentRegistry.COMMUNICATE_CD,  cd - 1);
            return;
        } else {
            baubleItem.set(DataComponentRegistry.COMMUNICATE_CD,  Config.communicateCDFail);
        }
        ConfigurableCommunicateData data = getDataFrom(baubleItem, maid);
        if (data == null)
            return;
        List<IActionWish> iActionWishes = data.buildWish(maid);
        if (iActionWishes.isEmpty()) {
            tag.putInt("cd", Config.communicateCDNoTarget);
            return;
        }
        ItemStack workCard = getWorkCardItem(baubleItem);
        Optional<CommunicatePlan> communicatePlan = CommunicateUtil.sendCommunicateWishAndGetPlan(
                maid,
                new CommunicateWish(maid, iActionWishes),
                plan -> workCard.isEmpty() || WorkCardItem.hasBauble(plan.handler(), workCard)
        );
        communicatePlan.ifPresentOrElse(plan -> {
            if (plan.handler().getTask() instanceof ICommunicatable ic) {
                CommunicateRequest communicateRequest = CommunicateRequest.create(plan, maid);
                ic.startCommunicate(plan.handler(), communicateRequest);
                tag.putUUID("last_task", communicateRequest.requestId());
            }
        }, () -> tag.putInt("cd", Config.communicateCDNoTarget));
    }

    public static ConfigurableCommunicateData getDataFrom(ItemStack stack, @Nullable EntityMaid maid) {
        if (!stack.is(ItemRegistry.CONFIGURABLE_COMMUNICATE_MARK))
            return null;
        if (!isManual(stack)) {
            if (maid == null) {
                return TaskDefaultCommunicate.get(Optional.ofNullable(getLastTaskId(stack)).orElse(TaskIdle.UID));
            }
            return TaskDefaultCommunicate.get(maid.getTask().getUid());
        }
        return stack.get(DataComponentRegistry.COMMUNICATE_DATA);
    }

    public static boolean isManual(ItemStack stack) {
        return stack.getOrDefault(DataComponentRegistry.COMMUNICATE_MANUAL, false);
    }

    public static ResourceLocation getLastTaskId(ItemStack stack) {
        if (!stack.has(DataComponentRegistry.COMMUNICATE_LAST_TASK))
            return null;
        return stack.get(DataComponentRegistry.COMMUNICATE_LAST_TASK);
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
                serverPlayer.openMenu(this, (buffer) -> {
                    buffer.writeInt(-1);
                });
            return InteractionResultHolder.consume(player.getItemInHand(p_41434_));
        } else {
            return InteractionResultHolder.pass(player.getItemInHand(p_41434_));
        }
    }

    public static ItemStack getWorkCardItem(ItemStack item) {
        if (!item.is(ItemRegistry.CONFIGURABLE_COMMUNICATE_MARK))
            return ItemStack.EMPTY;
        return item.getOrDefault(DataComponentRegistry.COMMUNICATE_WORK_CARD, ItemStack.EMPTY);
    }

    public static void setWorkCardItem(ItemStack item, ItemStack workCard) {
        item.set(DataComponentRegistry.COMMUNICATE_WORK_CARD, workCard);
    }
}
