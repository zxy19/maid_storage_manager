package studio.fantasyit.maid_storage_manager.items.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

public record ItemStackData(CompoundTag itemStackData) {
    public static final ItemStackData EMPTY = new ItemStackData(new CompoundTag());
    public static Codec<ItemStackData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            CompoundTag.CODEC.fieldOf("itemStack").forGetter(ItemStackData::itemStackData)
    ).apply(instance, ItemStackData::new));

    public static StreamCodec<RegistryFriendlyByteBuf, ItemStackData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG,
            ItemStackData::itemStackData,
            ItemStackData::new
    );

    public ItemStackData(HolderLookup.Provider provider, ItemStack itemStack) {
        this(itemStack.isEmpty() ? new CompoundTag() : ItemStackUtil.saveStack(provider, itemStack));
    }

    @Override
    public int hashCode() {
        return itemStackData.hashCode();
    }

    public ItemStack itemStack(HolderLookup.Provider provider) {
        if (itemStackData.isEmpty())
            return ItemStack.EMPTY;
        return ItemStackUtil.parseStack(provider, itemStackData);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ItemStackData(CompoundTag stackData))
            return stackData.equals(this.itemStackData);
        return false;
    }
}
