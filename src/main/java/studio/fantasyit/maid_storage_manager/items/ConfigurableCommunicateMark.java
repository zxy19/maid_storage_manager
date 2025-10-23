package studio.fantasyit.maid_storage_manager.items;

import com.github.tartaricacid.touhoulittlemaid.api.bauble.IMaidBauble;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.MutableInt;
import studio.fantasyit.maid_storage_manager.api.communicate.ICommunicatable;
import studio.fantasyit.maid_storage_manager.api.communicate.data.CommunicatePlan;
import studio.fantasyit.maid_storage_manager.api.communicate.data.CommunicateRequest;
import studio.fantasyit.maid_storage_manager.api.communicate.data.CommunicateWish;
import studio.fantasyit.maid_storage_manager.api.communicate.wish.IActionWish;
import studio.fantasyit.maid_storage_manager.communicate.CommunicateUtil;
import studio.fantasyit.maid_storage_manager.communicate.ConfigurableCommunicateData;
import studio.fantasyit.maid_storage_manager.communicate.TaskDefaultCommunicate;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ConfigurableCommunicateMark extends Item implements IMaidBauble {

    public ConfigurableCommunicateMark() {
        super(new Properties());
    }

    @Override
    public void onTick(EntityMaid maid, ItemStack baubleItem) {
        if (CommunicateUtil.hasCommunicateHolder(maid))
            return;
        CompoundTag tag = baubleItem.getOrCreateTag();
        int cd = tag.getInt("cd");
        if (cd > 0) {
            tag.putInt("cd", cd - 1);
            return;
        } else {
            tag.putInt("cd", 600);
        }
        ConfigurableCommunicateData data = getDataFrom(baubleItem, maid);
        List<IActionWish> iActionWishes = data.buildWish(maid);
        Optional<CommunicatePlan> communicatePlan = CommunicateUtil.sendCommunicateWishAndGetPlan(
                maid,
                new CommunicateWish(maid, iActionWishes),
                plan -> true
        );
        communicatePlan.ifPresent(plan -> {
            if (plan.handler() instanceof ICommunicatable ic) {
                ic.startCommunicate(plan.handler(), new CommunicateRequest(plan, maid, plan.handler(), UUID.randomUUID(), new MutableInt()));
            }
        });
    }

    public static ConfigurableCommunicateData getDataFrom(ItemStack stack, EntityMaid maid) {
        if (!stack.is(ItemRegistry.CONFIGURABLE_COMMUNICATE_MARK.get()))
            return null;
        if (!isManual(stack))
            return TaskDefaultCommunicate.get(maid.getTask().getUid());
        assert stack.getTag() != null;
        return ConfigurableCommunicateData.fromNbt(stack.getTag().getCompound("data"));
    }

    public static boolean isManual(ItemStack stack) {
        if (!stack.hasTag()) return false;
        assert stack.getTag() != null;
        return stack.getTag().getBoolean("manual");
    }
}
