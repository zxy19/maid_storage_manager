package studio.fantasyit.maid_storage_manager.items;

import com.github.tartaricacid.touhoulittlemaid.api.bauble.IMaidBauble;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.api.communicate.ICommunicatable;
import studio.fantasyit.maid_storage_manager.api.communicate.context.ICommunicateContext;
import studio.fantasyit.maid_storage_manager.api.communicate.context.PlaceRequestAndSwapSlot;
import studio.fantasyit.maid_storage_manager.communicate.ConfigurableCommunicateData;
import studio.fantasyit.maid_storage_manager.communicate.TaskDefaultCommunicate;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.List;

public class ConfigurableCommunicateMark extends Item implements ICommunicatable, IMaidBauble {

    public ConfigurableCommunicateMark() {
        super(new Properties());
    }

    @Override
    public boolean willingCommunicate(EntityMaid wisher, @Nullable ItemStack wisherItemStack, EntityMaid handler) {
        if (wisherItemStack == null)
            return false;
        ConfigurableCommunicateData dataFrom = getDataFrom(wisherItemStack, wisher);
        if (dataFrom == null)
            return false;
        for (ConfigurableCommunicateData.Item item : dataFrom.items) {
            List<ItemStack> currentItemStacks = item.getItemStacks(wisher);
            List<ItemStack> markedItemStacks = item.itemStacks();
            for (ItemStack mi : markedItemStacks) {
                int count = mi.getCount();
                //对于每个标记物品，在背包中寻找对应物品。如果找到，黑名单，那么返回true。否则统计数量，如果数量超过白名单，那么返回true
                //对于出现的不符合标记的物品，白名单的情况下，也应该直接返回true。
                for (ItemStack ci : currentItemStacks) {
                    if (ItemStackUtil.isSame(ci, mi, item.match())) {
                        if (item.whiteMode())
                            count -= ci.getCount();
                        else
                            return true;
                    } else if (item.whiteMode())
                        return true;
                }
                if (count != 0)
                    return true;
            }
            if (item.whiteMode()) {
                for (ItemStack ci : currentItemStacks) {
                    boolean find = false;
                    for (ItemStack mi : markedItemStacks) {
                        if (ItemStackUtil.isSame(ci, mi, item.match()))
                            find = true;
                    }
                    if (!find)
                        return true;
                }
            }
        }
        return false;
    }

    @Override
    public @Nullable ICommunicateContext startCommunicate(EntityMaid wisher, @Nullable ItemStack wisherItemStack, EntityMaid handler) {
        assert wisherItemStack != null;
        return new PlaceRequestAndSwapSlot(getDataFrom(wisherItemStack, wisher));
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
