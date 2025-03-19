package studio.fantasyit.maid_storage_manager.network;

import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;

import java.util.List;
import java.util.UUID;

public class PartialInventoryListData {
    public final UUID key;
    List<InventoryItem> data;

    public PartialInventoryListData(UUID key, List<InventoryItem> data) {
        this.key = key;
        this.data = data;
    }

    public PartialInventoryListData(FriendlyByteBuf buffer) {
        this.key = buffer.readUUID();
        this.data = buffer.readNbt()
                .getList("data", 10)
                .stream()
                .map(e -> InventoryItem.fromNbt((CompoundTag) e))
                .toList();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeUUID(key);
        CompoundTag tmp = new CompoundTag();
        ListTag listTag = new ListTag();
        for (InventoryItem inventoryItem : data) {
            listTag.add(inventoryItem.serializeNBT());
        }
        tmp.put("data", listTag);
        buffer.writeNbt(tmp);
    }
}
