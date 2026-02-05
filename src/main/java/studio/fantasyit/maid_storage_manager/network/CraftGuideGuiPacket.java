package studio.fantasyit.maid_storage_manager.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;

public class CraftGuideGuiPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<CraftGuideGuiPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(
                    MaidStorageManager.MODID, "craft_guide_gui_packet"
            )
    );

    @Override
    public CustomPacketPayload.Type<CraftGuideGuiPacket> type() {
        return TYPE;
    }

    public static StreamCodec<ByteBuf, CraftGuideGuiPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            t -> t.type.name(),
            ByteBufCodecs.INT,
            t -> t.key,
            ByteBufCodecs.INT,
            t -> t.value,
            ByteBufCodecs.COMPOUND_TAG,
            t -> t.data,
            CraftGuideGuiPacket::new
    );

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

    public CraftGuideGuiPacket(String type, int key, int value, CompoundTag data) {
        this.type = Type.valueOf(type);
        this.key = key;
        this.value = value;
        this.data = data;
    }
}
