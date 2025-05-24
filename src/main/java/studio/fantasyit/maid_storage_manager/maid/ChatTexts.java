package studio.fantasyit.maid_storage_manager.maid;

import com.github.tartaricacid.touhoulittlemaid.entity.chatbubble.implement.ProgressChatBubbleData;
import com.github.tartaricacid.touhoulittlemaid.entity.chatbubble.implement.TextChatBubbleData;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.network.chat.Component;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChatTexts {
    protected static final ConcurrentHashMap<UUID, Long> chatTexts = new ConcurrentHashMap<>();

    public static void send(EntityMaid maid, String key) {
        send(maid, Component.translatable(key));
    }

    public static void progress(EntityMaid maid, Component component, double progress) {
        if (chatTexts.containsKey(maid.getUUID())) {
            if (maid.getChatBubbleManager().getChatBubble(chatTexts.get(maid.getUUID())) != null)
                maid.getChatBubbleManager().removeChatBubble(chatTexts.get(maid.getUUID()));
            chatTexts.remove(maid.getUUID());
        }
        ProgressChatBubbleData textChatBubbleData = ProgressChatBubbleData.create(70,
                ProgressChatBubbleData.TYPE_1,
                999,
                component,
                0xffffffff,
                0xff1e88e5,
                progress,
                true);
        long l = maid.getChatBubbleManager().addChatBubble(textChatBubbleData);
        chatTexts.put(maid.getUUID(), l);
    }

    public static void send(EntityMaid maid, Component component) {
        if (chatTexts.containsKey(maid.getUUID())) {
            if (maid.getChatBubbleManager().getChatBubble(chatTexts.get(maid.getUUID())) != null)
                maid.getChatBubbleManager().removeChatBubble(chatTexts.get(maid.getUUID()));
            chatTexts.remove(maid.getUUID());
        }
        TextChatBubbleData textChatBubbleData = TextChatBubbleData.create(70, component, TextChatBubbleData.TYPE_1, 999);
        long l = maid.getChatBubbleManager().addChatBubble(textChatBubbleData);
        chatTexts.put(maid.getUUID(), l);
    }

    public static final String CHAT_CHEST_FULL = "chat_bubbles.maid_storage_manager.chest_full";
    public static final String CHAT_MISSING = "chat_bubbles.maid_storage_manager.missing";
    public static final String CHAT_RESORT = "chat_bubbles.maid_storage_manager.resort";
    public static final String CHAT_CRAFT_WORK = "chat_bubbles.maid_storage_manager.crafting";
    public static final String CHAT_CRAFT_WORK_PROGRESS = "chat_bubbles.maid_storage_manager.crafting_progress";
    public static final String CHAT_CRAFT_CALCULATE = "chat_bubbles.maid_storage_manager.craft_calculated";
    public static final String CHAT_CRAFTING_FAIL = "chat_bubbles.maid_storage_manager.crafting_fail";
    public static final String CHAT_CRAFTING_SUCCESS = "chat_bubbles.maid_storage_manager.crafting_success";
    public static final String CHAT_REQUEST_SUCCESS = "chat_bubbles.maid_storage_manager.request_finish";
    public static final String CHAT_REQUEST_FAIL = "chat_bubbles.maid_storage_manager.request_fail";
    public static final String CHAT_REQUEST_START = "chat_bubbles.maid_storage_manager.request_start";
    public static final String CHAT_CRAFT_GATHER = "chat_bubbles.maid_storage_manager.crafting_gathering";
    public static final String CHAT_CRAFT_GATHER_ITEMS = "chat_bubbles.maid_storage_manager.crafting_gathering_item";
    public static final String CHAT_CRAFT_STEP = "chat_bubbles.maid_storage_manager.crafting_step";
    public static final String CHAT_CHECK_MARK_CHANGED = "chat_bubbles.maid_storage_manager.check_mark_changed";
    public static final String CHAT_COWORK_ENABLE = "chat_bubbles.maid_storage_manager.cowork_enable";
    public static final String CHAT_COWORK_DISABLE = "chat_bubbles.maid_storage_manager.cowork_disable";
    public static final String CHAT_MOVING = "chat_bubbles.maid_storage_manager.moving";
    public static final String CHAT_MOVING_TAKEN = "chat_bubbles.maid_storage_manager.moving_taken";
}
