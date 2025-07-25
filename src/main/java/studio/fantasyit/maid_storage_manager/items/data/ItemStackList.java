package studio.fantasyit.maid_storage_manager.items.data;


import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectImmutableList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ItemStackList {
    public List<ItemStack> list;

    public ItemStackList(List<ItemStack> list) {
        this.list = new ArrayList<>(list);
    }

    public ItemStackList() {
        this.list = new ArrayList<>();
        for (int i = 0; i < 20; i++)
            list.add(ItemStack.EMPTY);
    }

    public Immutable toImmutable() {
        return new Immutable(new ObjectImmutableList<>(list));
    }

    public record Immutable(List<ItemStack> list) {
        public static Codec<Immutable> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.list(ItemStack.CODEC).fieldOf("list").forGetter(Immutable::list)
        ).apply(instance, Immutable::new));
        public static StreamCodec<RegistryFriendlyByteBuf, Immutable> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.collection(
                        ArrayList::new,
                        ItemStack.STREAM_CODEC
                ),
                Immutable::list,
                Immutable::new
        );
        public ItemStackList toMutable() {
            return new ItemStackList(new ArrayList<>(list));
        }
    }
}