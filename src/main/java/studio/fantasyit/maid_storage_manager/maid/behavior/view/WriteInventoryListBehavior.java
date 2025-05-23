package studio.fantasyit.maid_storage_manager.maid.behavior.view;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.advancement.AdvancementTypes;
import studio.fantasyit.maid_storage_manager.capability.InventoryListDataProvider;
import studio.fantasyit.maid_storage_manager.items.WrittenInvListItem;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
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
                availableInv.extractItem(i, 1, false);
                break;
            }
        }
        ItemStack item = ItemRegistry.WRITTEN_INVENTORY_LIST.get().getDefaultInstance().copyWithCount(1);
        UUID uuid = UUID.randomUUID();

        level.getCapability(InventoryListDataProvider.INVENTORY_LIST_DATA_CAPABILITY).ifPresent(inventoryListData -> {
            inventoryListData.set(uuid, MemoryUtil.getViewedInventory(maid).flatten());
        });

        CompoundTag tag = item.getOrCreateTag();
        tag.putUUID(WrittenInvListItem.TAG_UUID, uuid);
        tag.putString(WrittenInvListItem.TAG_AUTHOR, maid.getName().getString());
        tag.putLong(WrittenInvListItem.TAG_TIME, level.getDayTime());
        item.setTag(tag);
        ItemEntity itementity = new ItemEntity(level, maid.getX(), maid.getY(), maid.getZ(), item);
        maid.getMaxHeadXRot();
        Vec3 direction = Vec3.directionFromRotation(maid.getXRot(), maid.getYRot()).normalize().scale(0.5);
        itementity.setDeltaMovement(direction);
        itementity.setUnlimitedLifetime();
        level.addFreshEntity(itementity);
        AdvancementTypes.triggerForMaid(maid, AdvancementTypes.STORAGE_LIST);
    }
}
