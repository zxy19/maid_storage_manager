package studio.fantasyit.maid_storage_manager.network;

import net.minecraft.network.FriendlyByteBuf;

public class ItemSelectorGuiPacket {


    public enum SlotType{
        COUNT,
        MATCH_TAG,
        CLEAR,
        BLACKLIST,
    }
    public final SlotType type;
    public final int key;

    public final int value;

    public ItemSelectorGuiPacket(SlotType type, int key, int value) {
        this.type = type;
        this.key = key;
        this.value = value;
    }

    public ItemSelectorGuiPacket(FriendlyByteBuf buffer) {
        this.type = SlotType.values()[buffer.readInt()];
        this.key = buffer.readInt();
        this.value = buffer.readInt();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(type.ordinal());
        buffer.writeInt(key);
        buffer.writeInt(value);
    }
}
