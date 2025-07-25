package studio.fantasyit.maid_storage_manager.items.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import studio.fantasyit.maid_storage_manager.storage.Target;

import java.util.ArrayList;
import java.util.List;

public class TargetList {
    public List<Target> targets;

    public TargetList() {
        this.targets = new ArrayList<>();
    }

    public TargetList(List<Target> targets) {
        this.targets = new ArrayList<>(targets);
    }

    public List<Target> targets() {
        return targets;
    }

    public Immutable toImmutable() {
        return new Immutable(targets);
    }

    public record Immutable(List<Target> targets) {
        public static Codec<Immutable> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Target.CODEC.listOf().fieldOf("targets").forGetter(Immutable::targets)
                ).apply(instance, Immutable::new)
        );
        public static StreamCodec<RegistryFriendlyByteBuf, Immutable> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.collection(
                        ArrayList::new,
                        Target.STREAM_CODEC
                ),
                Immutable::targets,
                Immutable::new
        );

        public TargetList toMutable() {
            return new TargetList(targets);
        }
    }
}
