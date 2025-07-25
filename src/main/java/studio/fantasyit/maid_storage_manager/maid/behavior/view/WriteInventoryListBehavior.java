package studio.fantasyit.maid_storage_manager.maid.behavior.view;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.advancement.AdvancementTypes;
import studio.fantasyit.maid_storage_manager.attachment.InventoryListData;
import studio.fantasyit.maid_storage_manager.registry.DataComponentRegistry;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.util.InvUtil;
import studio.fantasyit.maid_storage_manager.util.MathUtil;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

import java.util.Map;
import java.util.UUID;

public class WriteInventoryListBehavior extends Behavior<EntityMaid> {
    public WriteInventoryListBehavior() {
        super(Map.of());
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel p_22538_, @NotNull EntityMaid maid) {
        CombinedInvWrapper availableInv = maid.getAvailableInv(true);
        for (int i = 0; i < availableInv.getSlots(); i++) {
            if (availableInv.getStackInSlot(i).is(ItemRegistry.INVENTORY_LIST.get())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void start(@NotNull ServerLevel level, EntityMaid maid, long p_22542_) {
        CombinedInvWrapper availableInv = maid.getAvailableInv(true);
        for (int i = 0; i < availableInv.getSlots(); i++) {
            if (availableInv.getStackInSlot(i).is(ItemRegistry.INVENTORY_LIST.get())) {
                ItemStack itemStack = availableInv.extractItem(i, 1, false);
                if (itemStack.has(DataComponentRegistry.INVENTORY_UUID)) {
                    InventoryListData.get(level.getServer().overworld()).remove(itemStack.get(DataComponentRegistry.INVENTORY_UUID));
                }
                break;
            }
        }
        ItemStack item = ItemRegistry.WRITTEN_INVENTORY_LIST.get().getDefaultInstance().copyWithCount(1);
        UUID uuid = UUID.randomUUID();

        InventoryListData.get(level.getServer().overworld()).addWithCraftable(level.registryAccess(), uuid, MemoryUtil.getViewedInventory(maid).flatten());

        item.set(DataComponentRegistry.INVENTORY_UUID, uuid);
        item.set(DataComponentRegistry.INVENTORY_AUTHOR, maid.getName());
        item.set(DataComponentRegistry.INVENTORY_TIME, level.getDayTime());
        if (maid.getOwner() instanceof ServerPlayer player)
            InvUtil.throwItem(maid, item, MathUtil.getFromToWithFriction(maid, player.position()));
        else
            InvUtil.throwItem(maid, item);
        AdvancementTypes.triggerForMaid(maid, AdvancementTypes.STORAGE_LIST);
    }
}