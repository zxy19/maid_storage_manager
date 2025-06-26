package studio.fantasyit.maid_storage_manager.integration.kubejs.binding;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.integration.kubejs.helper.CraftGuideOperator;
import studio.fantasyit.maid_storage_manager.integration.kubejs.helper.GeneratorConfigOperator;
import studio.fantasyit.maid_storage_manager.integration.kubejs.helper.TargetOperator;
import studio.fantasyit.maid_storage_manager.integration.kubejs.wrapped.item.KJSItemPair;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.maid.memory.ViewedInventoryMemory;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;
import studio.fantasyit.maid_storage_manager.util.RequestItemUtil;
import studio.fantasyit.maid_storage_manager.util.StorageAccessUtil;

import java.util.Arrays;

public class KJSMSMUtilities {
    public TargetOperator TARGETS = TargetOperator.INSTANCE;
    public CraftGuideOperator CRAFT_GUIDES = CraftGuideOperator.INSTANCE;
    public GeneratorConfigOperator GENERATOR_CONFIGS = GeneratorConfigOperator.INSTANCE;

    public ViewedInventoryMemory getViewed(EntityMaid maid) {
        return MemoryUtil.getViewedInventory(maid);
    }

    public boolean isValidTarget(EntityMaid maid, Target target) {
        return StorageAccessUtil.isValidTarget((ServerLevel) maid.level(), maid, target, false);
    }

    public ScheduleBehavior.Schedule getCurrentlyWorking(EntityMaid maid) {
        return MemoryUtil.getCurrentlyWorking(maid);
    }

    public ItemStack makeVirtualRequestToStorage(KJSItemPair[] items, Target target, String source) {
        return RequestItemUtil.makeVirtualItemStack(
                Arrays.stream(items).map(t -> t.stack.copyWithCount(t.count)).toList(),
                target,
                null,
                source
        );
    }

    public ItemStack makeVirtualRequestToEntity(KJSItemPair[] items, Entity target, String source) {
        return RequestItemUtil.makeVirtualItemStack(
                Arrays.stream(items).map(t -> t.stack.copyWithCount(t.count)).toList(),
                null,
                target,
                source
        );
    }

    public ItemStack makeVirtualRequest(KJSItemPair[] items, String source) {
        return RequestItemUtil.makeVirtualItemStack(
                Arrays.stream(items).map(t -> t.stack.copyWithCount(t.count)).toList(),
                null,
                null,
                source
        );
    }

    public ItemStack itemStack(Item item, int count) {
        return item.getDefaultInstance().copyWithCount(count);
    }
}
