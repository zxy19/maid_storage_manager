package studio.fantasyit.maid_storage_manager.maid;

import com.github.tartaricacid.touhoulittlemaid.entity.chatbubble.ChatBubbleManger;
import com.github.tartaricacid.touhoulittlemaid.entity.chatbubble.ChatText;
import com.github.tartaricacid.touhoulittlemaid.entity.chatbubble.MaidChatBubbles;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.cache.Cache;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Consumer;

public class ChatTexts {

    @FunctionalInterface
    interface fAddInnerCharTextWithParam {
        void accept(EntityMaid maid, String key, String... param);
    }

    static Consumer<Consumer<Cache<Integer, ChatText>>> innerChatTextCache = null;
    static fAddInnerCharTextWithParam addInnerCharTextWithParam = null;

    static {
        try {
            Field field = ChatBubbleManger.class.getDeclaredField("INNER_CHAT_TEXT_CACHE");
            field.setAccessible(true);
            innerChatTextCache = (consumer) -> {
                try {
                    Cache<Integer, ChatText> cache = (Cache<Integer, ChatText>) field.get(null);
                    consumer.accept(cache);
                } catch (IllegalAccessException e) {
                }
            };
        } catch (NoSuchFieldException e) {

        }
        try {
            Method method = ChatBubbleManger.class.getDeclaredMethod("maid_storage_manager$addInnerChatText", EntityMaid.class, String.class, String[].class);
            method.setAccessible(true);
            addInnerCharTextWithParam = (maid, key, param) -> {
                try {
                    method.invoke(null, maid, key, param);
                } catch (InvocationTargetException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            };
        } catch (NoSuchMethodException e) {

        }
    }

    public static void send(EntityMaid maid, String key) {
        innerChatTextCache.accept(cache -> cache.invalidate(maid.getId()));
        maid.setChatBubble(new MaidChatBubbles(MaidChatBubbles.EMPTY, MaidChatBubbles.EMPTY, MaidChatBubbles.EMPTY));
        ChatBubbleManger.addInnerChatText(maid, key);
    }

    public static void send(EntityMaid maid, String key, String... param) {
        innerChatTextCache.accept(cache -> cache.invalidate(maid.getId()));
        maid.setChatBubble(new MaidChatBubbles(MaidChatBubbles.EMPTY, MaidChatBubbles.EMPTY, MaidChatBubbles.EMPTY));
        addInnerCharTextWithParam.accept(maid, key, param);
    }

    public static String fromComponent(Component component) {
        if (component instanceof TranslatableContents tc)
            return "{" + tc.getKey() + "}";
        return component.getString();
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
}
