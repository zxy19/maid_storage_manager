package studio.fantasyit.maid_storage_manager.data;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.storage.Storage;

import java.util.ArrayList;
import java.util.List;

public class InventoryItem implements INBTSerializable<CompoundTag> {
    public ItemStack itemStack;
    public int totalCount;
    public List<Pair<Storage, Integer>> posAndSlot;

    public InventoryItem(ItemStack itemStack, int totalCount, List<Pair<Storage, Integer>> posAndSlot) {
        this.itemStack = itemStack.copyWithCount(1);
        this.totalCount = totalCount;
        this.posAndSlot = posAndSlot;
    }

    public InventoryItem(ItemStack itemStack, int totalCount) {
        this.itemStack = itemStack.copyWithCount(1);
        this.totalCount = totalCount;
        this.posAndSlot = new ArrayList<>();
    }

    public void addCount(Storage pos, int second) {
        posAndSlot.add(new Pair<>(pos, second));
        totalCount += second;
    }

    public Pair<ItemStack, Integer> toPair() {
        return new Pair<>(itemStack, totalCount);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.put("itemStack", itemStack.serializeNBT());
        tag.putInt("totalCount", totalCount);
        ListTag list = new ListTag();
        for (int i = 0; i < posAndSlot.size(); i++) {
            CompoundTag posAndSlotTag = new CompoundTag();
            posAndSlotTag.put("pos", posAndSlot.get(i).getA().toNbt());
            posAndSlotTag.putInt("count", posAndSlot.get(i).getB());
            list.add(posAndSlotTag);
        }
        tag.put("posCount", list);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        itemStack = ItemStack.of(nbt.getCompound("itemStack"));
        totalCount = nbt.getInt("totalCount");
        ListTag list = nbt.getList("posCount", 10);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tmp = list.getCompound(i);
            posAndSlot.add(new Pair<>(Storage.fromNbt(tmp.getCompound("pos")),
                    tmp.getInt("count")));
        }
    }
    public static InventoryItem fromNbt(CompoundTag tag){
        InventoryItem inventoryItem = new InventoryItem(ItemStack.EMPTY, 0);
        inventoryItem.deserializeNBT(tag);
        return inventoryItem;
    }


}
