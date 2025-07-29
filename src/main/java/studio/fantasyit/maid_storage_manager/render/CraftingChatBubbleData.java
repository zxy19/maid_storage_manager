package studio.fantasyit.maid_storage_manager.render;

import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.chatbubble.IChatBubbleRenderer;
import com.github.tartaricacid.touhoulittlemaid.entity.chatbubble.IChatBubbleData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;

public class CraftingChatBubbleData implements IChatBubbleData {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(MaidStorageManager.MODID, "crafting");
    private final ResourceLocation bg;
    private final Component text;
    private final int barBackgroundColor;
    private final int barForegroundColor;
    private final int barForegroundColor1;
    private final double progress;
    private final double progress1;

    private CraftingChatBubbleData(Component text, int barBackgroundColor, int barForegroundColor, int barForegroundColor1, double progress, double progress1) {
        this.bg = TYPE_2;
        this.text = text;
        this.barBackgroundColor = barBackgroundColor;
        this.barForegroundColor = barForegroundColor;
        this.barForegroundColor1 = barForegroundColor1;
        this.progress = progress;
        this.progress1 = progress1;
    }

    public static CraftingChatBubbleData create(Component text,
                                                int barBackgroundColor,
                                                int barForegroundColor,
                                                int barForegroundColor1,
                                                double progress,
                                                double progress1
    ) {
        return new CraftingChatBubbleData(text, barBackgroundColor, barForegroundColor, barForegroundColor1, progress, progress1);
    }

    @Override
    public int existTick() {
        return 600;
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public int priority() {
        return 10;
    }

    @OnlyIn(Dist.CLIENT)
    private IChatBubbleRenderer renderer;

    @OnlyIn(Dist.CLIENT)
    @Override
    public IChatBubbleRenderer getRenderer(IChatBubbleRenderer.Position position) {
        if (this.renderer == null) {
            this.renderer = new CraftingChatBubbleRenderer(this.bg,
                    this.text,
                    this.barBackgroundColor,
                    this.barForegroundColor,
                    this.barForegroundColor1,
                    this.progress,
                    this.progress1
            );
        }

        return this.renderer;
    }


    public static class CraftingChatSerializer implements IChatBubbleData.ChatSerializer {
        public IChatBubbleData readFromBuff(FriendlyByteBuf buf) {
            return new CraftingChatBubbleData(
                    buf.readJsonWithCodec(ComponentSerialization.CODEC),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readDouble(),
                    buf.readDouble()
            );
        }

        public void writeToBuff(FriendlyByteBuf buf, IChatBubbleData data) {
            CraftingChatBubbleData textChat = (CraftingChatBubbleData) data;
            buf.writeJsonWithCodec(ComponentSerialization.CODEC, textChat.text);
            buf.writeInt(textChat.barBackgroundColor);
            buf.writeInt(textChat.barForegroundColor);
            buf.writeInt(textChat.barForegroundColor1);
            buf.writeDouble(textChat.progress);
            buf.writeDouble(textChat.progress1);
        }
    }
}
