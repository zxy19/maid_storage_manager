package studio.fantasyit.maid_storage_manager.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.menu.communicate.CommunicateMarkMenu;

public class CommunicateMarkGuiPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<CommunicateMarkGuiPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(
                    MaidStorageManager.MODID, "communicate_mark_gui_packet"
            )
    );
    public static final StreamCodec<? super RegistryFriendlyByteBuf, CommunicateMarkGuiPacket> STREAM_CODEC = StreamCodec.of(
            (t, o) -> o.toBytes(t),
            CommunicateMarkGuiPacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public enum Type {
        MAX,
        MIN,
        THRESHOLD,
        SET_ITEM,
        SET_ITEMS,
        MATCH,
        WHITE_MODE,
        DATA,
        SLOT,
        SELECT,
        USE_ID,
        MANUAL
    }

    public final Type type;
    public final int key;
    public final int value;
    public final CompoundTag data;

    public CommunicateMarkGuiPacket(Type type, int i) {
        this(type, i, 0, new CompoundTag());
    }

    public CommunicateMarkGuiPacket(Type type, int key, CompoundTag data) {
        this(type, key, 0, data);
    }

    public CommunicateMarkGuiPacket(Type type, int key, int value) {
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

    public CommunicateMarkGuiPacket(Type type, int key, int value, CompoundTag data) {
        this.type = type;
        this.key = key;
        this.value = value;
        this.data = data;
    }

    public CommunicateMarkGuiPacket(FriendlyByteBuf buffer) {
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


    public static void handle(Player player, CommunicateMarkGuiPacket p) {
        if (player.containerMenu instanceof CommunicateMarkMenu menu)
            menu.handlePacket(p);
    }
}
