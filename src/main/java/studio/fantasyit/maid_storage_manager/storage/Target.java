package studio.fantasyit.maid_storage_manager.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;

import java.util.Optional;

public class Target {
    public static final Identifier VIRTUAL_TYPE = Identifier.fromNamespaceAndPath(MaidStorageManager.MODID, "virtual");
    public static Codec<Target> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Identifier.CODEC.fieldOf("type").forGetter(Target::getType),
                    BlockPos.CODEC.fieldOf("pos").forGetter(Target::getPos),
                    Direction.CODEC.optionalFieldOf("side").forGetter(Target::getSide)
            ).apply(instance, Target::new)
    );
    public static StreamCodec<RegistryFriendlyByteBuf, Target> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC,
            Target::getType,
            BlockPos.STREAM_CODEC,
            Target::getPos,
            ByteBufCodecs.optional(Direction.STREAM_CODEC),
            Target::getSide,
            Target::new
    );
    public Identifier type;
    public BlockPos pos;
    @Nullable
    public Direction side;

    public Target(Identifier type, BlockPos pos) {
        this(type, pos, Optional.empty());
    }

    public Target(Identifier type, BlockPos pos, Optional<Direction> side) {
        this(type, pos, side.orElse(null));
    }

    public Target(Identifier type, BlockPos pos, @Nullable Direction side) {
        this.type = type;
        this.pos = pos;
        this.side = side;
    }

    public static Target fromNbt(CompoundTag nbt) {
        if (!nbt.contains("side"))
            return new Target(
                    Identifier.tryParse(nbt.getString("type").get()),
                    BlockPos.of(nbt.getLong("pos").get()),
                    Optional.empty()
            );
        return new Target(
                Identifier.tryParse(nbt.getString("type").get()),
                BlockPos.of(nbt.getLong("pos").get()),
                Direction.byName(nbt.getString("side").get())
        );
    }

    public static @Nullable Target fromStoreString(String str) {
        String[] split = str.split(",");
        if (split.length == 4 || split.length == 5)
            return new Target(
                    Identifier.tryParse(split[0]),
                    new BlockPos(Integer.parseInt(split[1]),
                            Integer.parseInt(split[2]),
                            Integer.parseInt(split[3])),
                    split.length == 5 ? Direction.byName(split[4]) : null
            );
        return null;
    }

    public static Target virtual(BlockPos clickedPos, @Nullable Direction clickedFace) {
        return new Target(
                VIRTUAL_TYPE,
                clickedPos,
                clickedFace
        );
    }

    public Target sameType(BlockPos pos, @Nullable Direction side) {
        return new Target(type, pos, side);
    }

    public CompoundTag toNbt() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("type", type.toString());
        nbt.putLong("pos", pos.asLong());
        if (side != null)
            nbt.putString("side", side.getName());
        return nbt;
    }

    public Identifier getType() {
        return type;
    }

    public BlockPos getPos() {
        return pos;
    }

    public Optional<Direction> getSide() {
        return Optional.ofNullable(side);
    }

    public Direction getSideV() {
        return side;
    }

    @Override
    public String toString() {
        return String.format("Storage[%s]at{%d,%d,%d}(%s)",
                type.toString(),
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                side == null ? "" : side.getName());
    }

    public String toStoreString() {
        return String.format("%s,%d,%d,%d,%s",
                type.toString(),
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                side == null ? "" : side.getName());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Target storage) {
            return storage.type.equals(this.type) &&
                    storage.pos.equals(this.pos) &&
                    storage.side == this.side;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.type.hashCode() * 19 + this.pos.hashCode() * 13 + (this.side == null ? 0 : this.side.hashCode()) * 7;
    }

    public Target withoutSide() {
        return new Target(type, pos, Optional.empty());
    }

    public BlockState getBlockStateInLevel(Level level) {
        return level.getBlockState(pos);
    }
}
