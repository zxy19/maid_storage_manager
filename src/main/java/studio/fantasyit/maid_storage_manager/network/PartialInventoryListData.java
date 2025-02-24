package studio.fantasyit.maid_storage_manager.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import oshi.util.tuples.Pair;

import java.util.List;
import java.util.UUID;

public class PartialInventoryListData {
    public final UUID key;
    List<Pair<String, Integer>> data;

    public PartialInventoryListData(UUID key, List<Pair<String, Integer>> data) {
        this.key = key;
        this.data = data;
    }

    public PartialInventoryListData(FriendlyByteBuf buffer) {
        this.key = buffer.readUUID();
        this.data = buffer.readNbt().getList("data", 10).stream().map(e -> {
            CompoundTag tmp = (CompoundTag) e;
            return new Pair<>(tmp.getString("key"), tmp.getInt("value"));
        }).toList();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeUUID(key);
        CompoundTag tmp = new CompoundTag();
        ListTag listTag = new ListTag();
        for (Pair<String, Integer> pair : data) {
            CompoundTag tmp1 = new CompoundTag();
            tmp1.putString("key", pair.getA());
            tmp1.putInt("value", pair.getB());
            listTag.add(tmp1);
        }
        tmp.put("data", listTag);
        buffer.writeNbt(tmp);
    }
}
