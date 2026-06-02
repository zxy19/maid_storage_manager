package studio.fantasyit.maid_storage_manager.items.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

import java.util.Optional;

public record ItemStackData(Optional<ItemStackTemplate> ist) {
    public static final ItemStackData EMPTY = new ItemStackData(Optional.empty());

    public static ItemStackData of(ItemStack itemStack) {
        if (itemStack.isEmpty())
            return EMPTY;
        return new ItemStackData(Optional.of(ItemStackTemplate.fromNonEmptyStack(itemStack)));
    }

    public ItemStackData(ItemStack itemStack) {
        this(itemStack.isEmpty() ? Optional.empty()
                : Optional.of(ItemStackTemplate.fromNonEmptyStack(itemStack)));
    }

    public ItemStack itemStack() {
        return ist.map(ItemStackTemplate::create).orElse(ItemStack.EMPTY);
    }

    public static Codec<ItemStackData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStackTemplate.CODEC.optionalFieldOf("ist").forGetter(ItemStackData::ist)
    ).apply(instance, ItemStackData::new));

    public static StreamCodec<RegistryFriendlyByteBuf, ItemStackData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(ItemStackTemplate.STREAM_CODEC),
            ItemStackData::ist,
            ItemStackData::new
    );
}
