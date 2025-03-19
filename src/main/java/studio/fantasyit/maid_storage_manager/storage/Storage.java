package studio.fantasyit.maid_storage_manager.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class Storage {
    public static Codec<Storage> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("type").forGetter(Storage::getType),
                    BlockPos.CODEC.fieldOf("pos").forGetter(Storage::getPos),
                    Direction.CODEC.optionalFieldOf("side").forGetter(Storage::getSide)
            ).apply(instance, Storage::new)
    );
    public ResourceLocation type;
    public BlockPos pos;
    @Nullable
    public Direction side;

    public Storage(ResourceLocation type, BlockPos pos) {
        this(type, pos, Optional.empty());
    }

    public Storage(ResourceLocation type, BlockPos pos, Optional<Direction> side) {
        this(type, pos, side.orElse(null));
    }

    public Storage(ResourceLocation type, BlockPos pos, @Nullable Direction side) {
        this.type = type;
        this.pos = pos;
        this.side = side;
    }

    public static Storage fromNbt(CompoundTag nbt) {
        if (!nbt.contains("side"))
            return new Storage(
                    new ResourceLocation(nbt.getString("type")),
                    BlockPos.of(nbt.getLong("pos")),
                    Optional.empty()
            );
        return new Storage(
                new ResourceLocation(nbt.getString("type")),
                BlockPos.of(nbt.getLong("pos")),
                Direction.byName(nbt.getString("side"))
        );
    }

    public static @Nullable Storage fromStoreString(String str) {
        String[] split = str.split(",");
        if (split.length == 4 || split.length == 5)
            return new Storage(
                    new ResourceLocation(split[0]),
                    new BlockPos(Integer.parseInt(split[1]),
                            Integer.parseInt(split[2]),
                            Integer.parseInt(split[3])),
                    split.length == 5 ? Direction.byName(split[4]) : null
            );
        return null;
    }

    public Storage sameType(BlockPos pos, @Nullable Direction side) {
        return new Storage(type, pos, side);
    }

    public CompoundTag toNbt() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("type", type.toString());
        nbt.putLong("pos", pos.asLong());
        if (side != null)
            nbt.putString("side", side.getName());
        return nbt;
    }

    public ResourceLocation getType() {
        return type;
    }

    public BlockPos getPos() {
        return pos;
    }

    public Optional<Direction> getSide() {
        return Optional.ofNullable(side);
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
        if (obj instanceof Storage storage) {
            return storage.type.equals(this.type) &&
                    storage.pos.equals(this.pos) &&
                    storage.side == this.side;
        }
        return false;
    }

    public Storage withoutSide() {
        return new Storage(type, pos, Optional.empty());
    }
}
