package studio.fantasyit.maid_storage_manager.util;

import com.github.tartaricacid.touhoulittlemaid.ai.manager.entity.LLMCallback;
import com.github.tartaricacid.touhoulittlemaid.ai.manager.entity.MaidAIChatManager;
import com.github.tartaricacid.touhoulittlemaid.ai.service.llm.LLMClient;
import com.github.tartaricacid.touhoulittlemaid.ai.service.llm.LLMConfig;
import com.github.tartaricacid.touhoulittlemaid.ai.service.llm.LLMMessage;
import com.github.tartaricacid.touhoulittlemaid.ai.service.llm.LLMSite;
import com.github.tartaricacid.touhoulittlemaid.ai.service.llm.openai.response.FunctionToolCall;
import com.github.tartaricacid.touhoulittlemaid.ai.service.llm.openai.response.ToolCall;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.util.CappedQueue;
import com.google.common.collect.Lists;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageContext;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class RequestItemUtil {
    public static void stopJobAndStoreOrThrowItem(EntityMaid maid, @Nullable IStorageContext storeTo, @Nullable Entity targetEntity) {
        Level level = maid.level();
        ItemStack reqList = maid.getMainHandItem();
        CompoundTag tag = reqList.getOrCreateTag();
        if (tag.getBoolean(RequestListItem.TAG_VIRTUAL)) {
            if (tag.getString(RequestListItem.TAG_VIRTUAL_SOURCE).equals("AI")) {
                sendToolResponseB(maid, reqList);
            }
            //虚拟的，不用额外处理
        }
        //1.1 尝试扔给目标实体
        else if (tag.getInt(RequestListItem.TAG_REPEAT_INTERVAL) <= 0 && targetEntity != null) {
            Vec3 targetDir = MathUtil.getFromToWithFriction(maid.position(), targetEntity.position(), 0.6);
            tag.putBoolean(RequestListItem.TAG_IGNORE_TASK, true);
            reqList.setTag(tag);
            InvUtil.throwItem(maid, reqList, targetDir, true);
            //因为扔出去会被女仆秒捡起，添加一个CD
            MemoryUtil.setReturnToScheduleAt(maid, level.getServer().getTickCount() + 80);
        }
        //1.2 尝试放入指定位置。例外：如果有循环请求任务，那么不会存入目标容器.
        else if (tag.getInt(RequestListItem.TAG_REPEAT_INTERVAL) > 0 || storeTo == null || !InvUtil.tryPlace(storeTo, reqList).isEmpty()) {
            //没能成功，尝试背包
            if (tag.getInt(RequestListItem.TAG_REPEAT_INTERVAL) > 0) {
                tag.putInt(RequestListItem.TAG_COOLING_DOWN, tag.getInt(RequestListItem.TAG_REPEAT_INTERVAL));
            } else {
                tag.putBoolean(RequestListItem.TAG_IGNORE_TASK, true);
            }
            reqList.setTag(tag);
            if (!InvUtil.tryPlace(maid.getAvailableInv(false), reqList).isEmpty()) {
                //背包也没空。。扔地上站未来
                InvUtil.throwItem(maid, reqList);
            }
        }
        MemoryUtil.getRequestProgress(maid).clearTarget();
        MemoryUtil.getRequestProgress(maid).stopWork();
        maid.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
    }

    /**
     * 工具AI回调2
     *
     * @param maid
     * @param reqList
     */
    private static void sendToolResponseB(EntityMaid maid, ItemStack reqList) {
        MaidAIChatManager aiChatManager = maid.getAiChatManager();
        LLMSite llmSite = aiChatManager.getLLMSite();
        ServerPlayer owner = (ServerPlayer) maid.getOwner();
        if (llmSite == null || owner == null) return;
        LLMClient client = llmSite.client();
        StringBuilder sb = new StringBuilder();
        CompoundTag tag = reqList.getOrCreateTag();
        ListTag list = tag.getList(RequestListItem.TAG_ITEMS, ListTag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag itemTag = list.getCompound(i);
            if (!itemTag.contains(RequestListItem.TAG_ITEMS_ITEM)) continue;

            ItemStack itemstack = ItemStack.of(itemTag.getCompound(RequestListItem.TAG_ITEMS_ITEM));
            if (itemstack.isEmpty()) continue;

            int collected = itemTag.getInt(RequestListItem.TAG_ITEMS_COLLECTED);
            int requested = itemTag.getInt(RequestListItem.TAG_ITEMS_REQUESTED);


            if (itemTag.getBoolean(RequestListItem.TAG_ITEMS_DONE)) {
                sb.append("[结束]");
            } else {
                sb.append("[进行中]");
            }

            sb.append(itemstack.getHoverName().getString());
            sb.append(Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(itemstack.getItem())));
            sb.append(" 成功收集了");
            sb.append(collected);
            sb.append("个，计划需要");
            sb.append(requested == -1 ? "无限" : requested);
            sb.append("个");
            sb.append("\n");
        }
        FunctionToolCall functionToolCall = new FunctionToolCall("stock", "{}");
        String id = UUID.randomUUID().toString();
        ToolCall toolCall = new ToolCall(id, functionToolCall);
        List<LLMMessage> llmMessages = aiChatManager.getSetting().map(s -> {
            String setting = s.getSetting(maid, owner.getLanguage());
            CappedQueue<LLMMessage> history = aiChatManager.getHistory();
            List<LLMMessage> chatList = Lists.newArrayList();
            chatList.add(LLMMessage.systemChat(maid, setting));
            // 倒序遍历，将历史对话加载进去
            history.getDeque().descendingIterator().forEachRemaining(chatList::add);
            return chatList;
        }).orElse(Lists.newArrayList());
        llmMessages.add(LLMMessage.userChat(maid, "请查询，然后告诉我上一个任务的完成情况"));
        llmMessages.add(LLMMessage.assistantChat(maid, "查询任务情况", List.of(toolCall)));
        llmMessages.add(LLMMessage.toolChat(maid, sb.toString(), id));
        LLMCallback callback = new LLMCallback(aiChatManager, "请查询，然后告诉我上一个任务的完成情况");
        LLMConfig config = LLMConfig.normalChat(aiChatManager.getLLMModel(), maid);
        client.chat(llmMessages, config, callback);
    }

    public static ItemStack makeVirtualItemStack(List<ItemStack> list, @Nullable Target target, @Nullable Entity targetEntity, String virtual_source) {
        ItemStack itemStack = ItemRegistry.REQUEST_LIST_ITEM.get().getDefaultInstance().copy();
        CompoundTag tag = itemStack.getOrCreateTag();
        tag.putBoolean(RequestListItem.TAG_VIRTUAL, true);
        tag.putString(RequestListItem.TAG_VIRTUAL_SOURCE, virtual_source);
        ListTag listTag = new ListTag();
        for (int i = 0; i < 10; i++) {
            ItemStack item = i < list.size() ? list.get(i) : ItemStack.EMPTY;
            CompoundTag tmp = new CompoundTag();
            tmp.putInt(RequestListItem.TAG_ITEMS_REQUESTED, item.getCount());
            tmp.put(RequestListItem.TAG_ITEMS_ITEM, item.copyWithCount(1).save(new CompoundTag()));
            listTag.add(tmp);
        }
        tag.put(RequestListItem.TAG_ITEMS, listTag);
        tag.putBoolean(RequestListItem.TAG_BLACKMODE, false);
        tag.putBoolean(RequestListItem.TAG_STOCK_MODE, false);
        tag.putBoolean(RequestListItem.TAG_IGNORE_TASK, false);
        tag.putBoolean(RequestListItem.TAG_MATCH_TAG, false);
        tag.putInt(RequestListItem.TAG_REPEAT_INTERVAL, 0);
        if (target != null) {
            tag.put(RequestListItem.TAG_STORAGE, target.toNbt());
        } else if (targetEntity != null) {
            tag.putUUID(RequestListItem.TAG_STORAGE_ENTITY, targetEntity.getUUID());
        }
        tag.putUUID(RequestListItem.TAG_UUID, UUID.randomUUID());
        itemStack.setTag(tag);
        return itemStack;
    }
}
