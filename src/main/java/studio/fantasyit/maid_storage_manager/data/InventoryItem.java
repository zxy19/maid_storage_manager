package studio.fantasyit.maid_storage_manager.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.storage.Target;

import java.util.ArrayList;
import java.util.List;

public class InventoryItem implements INBTSerializable<CompoundTag> {
    public record PositionCount(Target pos, int count, boolean isCraftGuide) {
    }

    public ItemStack itemStack;
    public int totalCount;
    public List<PositionCount> posAndSlot;

    public InventoryItem(ItemStack itemStack, int totalCount, List<PositionCount> posAndSlot) {
        this.itemStack = itemStack.copyWithCount(1);
        this.totalCount = totalCount;
        this.posAndSlot = posAndSlot;
    }

    public InventoryItem(ItemStack itemStack, int totalCount) {
        this.itemStack = itemStack.copyWithCount(1);
        this.totalCount = totalCount;
        this.posAndSlot = new ArrayList<>();
    }

    public void addCount(Target pos, int second) {
        posAndSlot.add(new PositionCount(pos, second, false));
        totalCount += second;
    }

    public void addCraftGuidePos(Target pos) {
        posAndSlot.add(new PositionCount(pos, 0, true));
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
            posAndSlotTag.put("pos", posAndSlot.get(i).pos().toNbt());
            posAndSlotTag.putInt("count", posAndSlot.get(i).count());
            posAndSlotTag.putBoolean("isCraftGuide", posAndSlot.get(i).isCraftGuide());
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
            posAndSlot.add(new PositionCount(
                            Target.fromNbt(tmp.getCompound("pos")),
                            tmp.getInt("count"),
                            tmp.getBoolean("isCraftGuide")
                    )
            );
        }
    }

    public static InventoryItem fromNbt(CompoundTag tag) {
        InventoryItem inventoryItem = new InventoryItem(ItemStack.EMPTY, 0);
        inventoryItem.deserializeNBT(tag);
        return inventoryItem;
    }


}
