package studio.fantasyit.maid_storage_manager.maid;

import com.github.tartaricacid.touhoulittlemaid.entity.chatbubble.ChatBubbleManger;
import com.github.tartaricacid.touhoulittlemaid.entity.chatbubble.ChatText;
import com.github.tartaricacid.touhoulittlemaid.entity.chatbubble.MaidChatBubbles;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.cache.Cache;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ChatTexts {
    static Consumer<Consumer<Cache<Integer, ChatText>>> innerChatTextCache = null;

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
    }

    public static void send(EntityMaid maid, String key) {
        innerChatTextCache.accept(cache -> cache.invalidate(maid.getId()));
        maid.setChatBubble(new MaidChatBubbles(MaidChatBubbles.EMPTY, MaidChatBubbles.EMPTY, MaidChatBubbles.EMPTY));
        ChatBubbleManger.addInnerChatText(maid, key);
    }

    public static String CHAT_CHEST_FULL = "chat_bubbles.maid_storage_manager.chest_full";
    public static String CHAT_MISSING = "chat_bubbles.maid_storage_manager.missing";
    public static String CHAT_RESORT = "chat_bubbles.maid_storage_manager.resort";
    public static String CHAT_CRAFT_WORK = "chat_bubbles.maid_storage_manager.crafting";
    public static String CHAT_CRAFT_CALCULATE = "chat_bubbles.maid_storage_manager.craft_calculated";
    public static String CHAT_REQUEST_FINISH = "chat_bubbles.maid_storage_manager.request_finish";
    public static String CHAT_REQUEST_FAIL = "chat_bubbles.maid_storage_manager.request_fail";
    public static String CHAT_REQUEST_START = "chat_bubbles.maid_storage_manager.request_start";
    public static String CHAT_CRAFT_GATHER = "chat_bubbles.maid_storage_manager.crafting_gathering";
    public static String CHAT_CRAFT_STEP = "chat_bubbles.maid_storage_manager.crafting_step";
    public static String CHAT_CRAFT_RESULT = "chat_bubbles.maid_storage_manager.crafting_result";
}
