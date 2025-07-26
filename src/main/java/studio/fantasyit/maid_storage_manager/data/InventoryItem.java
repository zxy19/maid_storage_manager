package studio.fantasyit.maid_storage_manager.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;

public class InventoryItem implements INBTSerializable<CompoundTag> {
    public record PositionCount(Target pos, int count, boolean isCraftGuide) {
        public static StreamCodec<RegistryFriendlyByteBuf, PositionCount> STREAM_CODEC = StreamCodec.composite(
                Target.STREAM_CODEC,
                PositionCount::pos,
                ByteBufCodecs.INT,
                PositionCount::count,
                ByteBufCodecs.BOOL,
                PositionCount::isCraftGuide,
                PositionCount::new
        );
    }

    public static StreamCodec<RegistryFriendlyByteBuf, InventoryItem> STREAM_CODEC = StreamCodec.composite(
            ItemStackUtil.OPTIONAL_STREAM_CODEC,
            t -> t.itemStack,
            ByteBufCodecs.INT,
            t -> t.totalCount,
            ByteBufCodecs.collection(
                    ArrayList::new,
                    PositionCount.STREAM_CODEC
            ),
            t -> t.posAndSlot,
            InventoryItem::new
    );

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
    public CompoundTag serializeNBT(HolderLookup.Provider t) {
        CompoundTag tag = new CompoundTag();
        tag.put("itemStack", itemStack.save(t));
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
    public void deserializeNBT(HolderLookup.Provider t, CompoundTag nbt) {
        itemStack = ItemStackUtil.parseStack(t, nbt.getCompound("itemStack"));
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

    public static InventoryItem fromNbt(HolderLookup.Provider t, CompoundTag tag) {
        InventoryItem inventoryItem = new InventoryItem(ItemStack.EMPTY, 0);
        inventoryItem.deserializeNBT(t, tag);
        return inventoryItem;
    }
}
