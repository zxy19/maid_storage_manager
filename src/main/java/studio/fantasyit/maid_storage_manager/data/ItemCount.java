package studio.fantasyit.maid_storage_manager.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

public record ItemCount(ItemStack item, int count) {
    public ItemCount(ItemStack item) {
        this(
                item.copyWithCount(1),
                item.getCount()
        );
    }

    public static ItemCount EMPTY = new ItemCount(ItemStack.EMPTY);

    public static ItemCount of(ItemStack item) {
        return new ItemCount(item);
    }

    public static ItemCount of(ItemStack item, int count) {
        return new ItemCount(item, count);
    }

    public static final Codec<ItemCount> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ItemStackUtil.OPTIONAL_CODEC_UNLIMITED.fieldOf("item").forGetter(ItemCount::item),
                    Codec.INT.fieldOf("count").forGetter(ItemCount::count)
            ).apply(instance, ItemCount::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemCount> STREAM_CODEC = StreamCodec.composite(
            ItemStack.OPTIONAL_STREAM_CODEC,
            ItemCount::item,
            ByteBufCodecs.INT,
            ItemCount::count,
            ItemCount::new
    );

    public ItemStack getFirst() {
        return item;
    }

    public int getSecond() {
        return count;
    }
}