package studio.fantasyit.maid_storage_manager.mixin;

import com.github.tartaricacid.touhoulittlemaid.entity.chatbubble.ChatBubbleManger;
import com.github.tartaricacid.touhoulittlemaid.entity.chatbubble.ChatText;
import com.github.tartaricacid.touhoulittlemaid.entity.chatbubble.ChatTextType;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.cache.Cache;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mixin(ChatBubbleManger.class)
public abstract class ChatBubbleMgrMixin {
    @Shadow
    @Final
    private static Cache<Integer, ChatText> INNER_CHAT_TEXT_CACHE;

    @Shadow
    private static long getEndTime() {
        return 0;
    }

    @Unique
    private static void maid_storage_manager$addInnerChatText(EntityMaid maid, String key, String... param) {
        if (INNER_CHAT_TEXT_CACHE.getIfPresent(maid.getId()) == null) {
            List<String> paramList = new ArrayList<>(List.of(key));
            paramList.addAll(Arrays.asList(param));
            String actualKey = String.join(",", paramList);
            ChatText chatText = new ChatText(ChatTextType.TEXT, ChatText.EMPTY_ICON_PATH, String.format("{%s}", actualKey));
            INNER_CHAT_TEXT_CACHE.put(maid.getId(), chatText);
            maid.addChatBubble(getEndTime(), chatText);
        }
    }
}
