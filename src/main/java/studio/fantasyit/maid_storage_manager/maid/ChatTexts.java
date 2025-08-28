package studio.fantasyit.maid_storage_manager.maid;

import com.github.tartaricacid.touhoulittlemaid.entity.chatbubble.implement.ProgressChatBubbleData;
import com.github.tartaricacid.touhoulittlemaid.entity.chatbubble.implement.TextChatBubbleData;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.network.chat.Component;
import studio.fantasyit.maid_storage_manager.render.CraftingChatBubbleData;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChatTexts {
    protected static final ConcurrentHashMap<UUID, Long> chatTexts = new ConcurrentHashMap<>();
    protected static final ConcurrentHashMap<UUID, Long> chatTextsSecondary = new ConcurrentHashMap<>();

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
                ProgressChatBubbleData.TYPE_2,
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
        TextChatBubbleData textChatBubbleData = TextChatBubbleData.create(70, component, TextChatBubbleData.TYPE_2, 999);
        long l = maid.getChatBubbleManager().addChatBubble(textChatBubbleData);
        chatTexts.put(maid.getUUID(), l);
    }

    public static void showSecondaryCrafting(EntityMaid maid, Component component, double progress, double progress1, boolean isFailure) {
        if (chatTextsSecondary.containsKey(maid.getUUID())) {
            if (maid.getChatBubbleManager().getChatBubble(chatTextsSecondary.get(maid.getUUID())) != null)
                maid.getChatBubbleManager().removeChatBubble(chatTextsSecondary.get(maid.getUUID()));
            chatTextsSecondary.remove(maid.getUUID());
        }

        CraftingChatBubbleData textChatBubbleData = CraftingChatBubbleData.create(
                component,
                0xffffffff,
                0xff1e88e5,
                isFailure ? 0xffe58590 : 0xff1e88e5,
                progress,
                progress1
        );
        long l = maid.getChatBubbleManager().addChatBubble(textChatBubbleData);
        chatTextsSecondary.put(maid.getUUID(), l);
    }

    public static void removeSecondary(EntityMaid maid) {
        if (chatTextsSecondary.containsKey(maid.getUUID())) {
            if (maid.getChatBubbleManager().getChatBubble(chatTextsSecondary.get(maid.getUUID())) != null)
                maid.getChatBubbleManager().removeChatBubble(chatTextsSecondary.get(maid.getUUID()));
            chatTextsSecondary.remove(maid.getUUID());
        }
    }
    public static void remove(EntityMaid maid) {
        if (chatTexts.containsKey(maid.getUUID())) {
            if (maid.getChatBubbleManager().getChatBubble(chatTexts.get(maid.getUUID())) != null)
                maid.getChatBubbleManager().removeChatBubble(chatTexts.get(maid.getUUID()));
            chatTexts.remove(maid.getUUID());
        }
    }

    public static final String CHAT_CHEST_FULL = "chat_bubbles.maid_storage_manager.chest_full";
    public static final String CHAT_MISSING = "chat_bubbles.maid_storage_manager.missing";
    public static final String CHAT_RESORT = "chat_bubbles.maid_storage_manager.resort";
    public static final String CHAT_CRAFT_WORK = "chat_bubbles.maid_storage_manager.crafting";
    public static final String CHAT_CRAFT_WORK_PROGRESS = "chat_bubbles.maid_storage_manager.crafting_progress";
    public static final String CHAT_CRAFT_CALCULATE = "chat_bubbles.maid_storage_manager.craft_calculated";
    public static final String CHAT_CRAFT_CALCULATE_NO_RESULT = "chat_bubbles.maid_storage_manager.craft_calculated_no_result";
    public static final String CHAT_CRAFT_GENERATING = "chat_bubbles.maid_storage_manager.craft_generating";
    public static final String CHAT_CRAFTING_FAIL = "chat_bubbles.maid_storage_manager.crafting_fail";
    public static final String CHAT_CRAFTING_SUCCESS = "chat_bubbles.maid_storage_manager.crafting_success";
    public static final String CHAT_REQUEST_SUCCESS = "chat_bubbles.maid_storage_manager.request_finish";
    public static final String CHAT_REQUEST_DISPATCH = "chat_bubbles.maid_storage_manager.request_dispatch_find";
    public static final String CHAT_REQUEST_FAIL = "chat_bubbles.maid_storage_manager.request_fail";
    public static final String CHAT_REQUEST_START = "chat_bubbles.maid_storage_manager.request_start";
    public static final String CHAT_CRAFT_DISPATCHED = "chat_bubbles.maid_storage_manager.crafting.dispatch_received";
    public static final String CHAT_CRAFT_GATHER = "chat_bubbles.maid_storage_manager.crafting_gathering";
    public static final String CHAT_CRAFT_GATHER_ITEMS = "chat_bubbles.maid_storage_manager.crafting_gathering_item";
    public static final String CHAT_CRAFT_RESCHEDULE = "chat_bubbles.maid_storage_manager.crafting_reschedule";
    public static final String CHAT_CRAFT_FAIL_WAITING = "chat_bubbles.maid_storage_manager.crafting_fail_waiting";

    public static final String CHAT_CHECK_MARK_CHANGED = "chat_bubbles.maid_storage_manager.check_mark_changed";
    public static final String CHAT_COWORK_ENABLE = "chat_bubbles.maid_storage_manager.cowork_enable";
    public static final String CHAT_COWORK_DISABLE = "chat_bubbles.maid_storage_manager.cowork_disable";
    public static final String CHAT_MOVING = "chat_bubbles.maid_storage_manager.moving";
    public static final String CHAT_MOVING_TAKEN = "chat_bubbles.maid_storage_manager.moving_taken";

    public static final String CHAT_SECONDARY_CRAFTING = "chat_bubbles.maid_storage_manager.secondary_crafting";

    public static final String CHAT_SECONDARY_CRAFTING_GATHER = "chat_bubbles.maid_storage_manager.secondary_crafting.gather";

    public static final String CHAT_SECONDARY_CRAFTING_WORK = "chat_bubbles.maid_storage_manager.secondary_crafting.work";

    public static final String CHAT_SECONDARY_CRAFTING_STATUS_NO_DISPATCHING = "chat_bubbles.maid_storage_manager.secondary_crafting.status_no_dispatch";
    public static final String CHAT_SECONDARY_CRAFTING_STATUS_SUB = "chat_bubbles.maid_storage_manager.secondary_crafting.status_sub";
    public static final String CHAT_SECONDARY_CRAFTING_STATUS_MAIN = "chat_bubbles.maid_storage_manager.secondary_crafting.status_main";
}
