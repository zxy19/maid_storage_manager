package studio.fantasyit.maid_storage_manager.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public class CraftGuideGuiPacket {


    public enum Type {
        COUNT,
        SET_MODE,
        SYNC,
        GENERATOR_RESULT,
        UP,
        DOWN,
        REMOVE,
        SET_ITEM,
        SET_ALL_INPUT,
        GLOBAL,
        SELECT,
        OPTION,
        EXTRA,
        PAGE_UP,
        PAGE_DOWN,
        SIDE,
        GENERATOR
    }

    public final Type type;
    public final int key;
    public final int value;
    public final CompoundTag data;

    public CraftGuideGuiPacket(Type type, int i) {
        this(type, i, 0, new CompoundTag());
    }

    public CraftGuideGuiPacket(Type type, int key, CompoundTag data) {
        this(type, key, 0, data);
    }

    public CraftGuideGuiPacket(Type type, int key, int value) {
        this(type, key, value, new CompoundTag());
    }

    public static CompoundTag singleValue(String value) {
        CompoundTag tag = new CompoundTag();
        tag.putString("value", value);
        return tag;
    }

    public static String getStringFrom(CompoundTag tag) {
        return tag.getString("value");
    }

    public CraftGuideGuiPacket(Type type, int key, int value, CompoundTag data) {
        this.type = type;
        this.key = key;
        this.value = value;
        this.data = data;
    }

    public CraftGuideGuiPacket(FriendlyByteBuf buffer) {
        this.type = Type.values()[buffer.readInt()];
        this.key = buffer.readInt();
        this.value = buffer.readInt();
        this.data = buffer.readNbt();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(type.ordinal());
        buffer.writeInt(key);
        buffer.writeInt(value);
        buffer.writeNbt(data);
    }
}
