package studio.fantasyit.maid_storage_manager.util;

import com.github.tartaricacid.touhoulittlemaid.ai.manager.entity.LLMCallback;
import com.github.tartaricacid.touhoulittlemaid.ai.manager.entity.MaidAIChatManager;
import com.github.tartaricacid.touhoulittlemaid.ai.manager.setting.papi.PapiReplacer;
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
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.ai.AiUtils;
import studio.fantasyit.maid_storage_manager.api.communicate.data.CommunicateRequest;
import studio.fantasyit.maid_storage_manager.api.communicate.step.RequestItemStep;
import studio.fantasyit.maid_storage_manager.communicate.CommunicateUtil;
import studio.fantasyit.maid_storage_manager.craft.work.CraftLayerChain;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.maid.memory.AbstractTargetMemory;
import studio.fantasyit.maid_storage_manager.maid.memory.CraftMemory;
import studio.fantasyit.maid_storage_manager.network.JEIRequestResultPacket;
import studio.fantasyit.maid_storage_manager.network.Network;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageContext;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class RequestItemUtil {
    public static boolean isRequestTarget(ServerLevel level, EntityMaid maid, Target target) {
        Target storageBlock = RequestListItem.getStorageBlock(maid.getMainHandItem());
        if (storageBlock == null || target == null)
            return false;
        if (storageBlock.equals(target))
            return true;
        MutableBoolean result = new MutableBoolean(false);
        StorageAccessUtil.checkNearByContainers(
                level,
                target.getPos(),
                pos -> {
                    Target m = target.sameType(pos, null);
                    if (storageBlock.equals(m))
                        result.setTrue();
                }
        );
        return result.getValue();
    }

    public static void stopJobAndStoreOrThrowItem(EntityMaid maid, @Nullable IStorageContext storeTo, @Nullable Entity targetEntity) {
        Level level = maid.level();
        ItemStack reqList = maid.getMainHandItem();
        CompoundTag tag = reqList.getOrCreateTag();
        if (tag.getBoolean(RequestListItem.TAG_VIRTUAL)) {
            if (tag.getString(RequestListItem.TAG_VIRTUAL_SOURCE).equals("AI")) {
                sendToolResponseB(maid, reqList);
            } else if (tag.getString(RequestListItem.TAG_VIRTUAL_SOURCE).equals("JEI")) {
                if (maid.getOwner() instanceof ServerPlayer player)
                    Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                            new JEIRequestResultPacket(
                                    Component.translatable("gui.maid_storage_manager.jei_request.finish",
                                            maid.getDisplayName()
                                    )));

            } else if (tag.getString(RequestListItem.TAG_VIRTUAL_SOURCE).equals("DISPATCHED")) {
                dispatchedTaskDone(maid, reqList);
            } else if (tag.getString(RequestListItem.TAG_VIRTUAL_SOURCE).equals("DISPATCH_FIND")) {
                dispatchFindTaskDone(maid, reqList);
            } else if (tag.getString(RequestListItem.TAG_VIRTUAL_SOURCE).equals("COMMUNICATE")) {
                CommunicateRequest communicateRequest = CommunicateUtil.getCommunicateRequest(maid);
                if (communicateRequest != null && communicateRequest.getCurrentStep() instanceof RequestItemStep requestItemStep)
                    requestItemStep.onRequestDone(RequestListItem.isAllSuccess(reqList));
            }
            //虚拟的，不用额外处理
            //TODO 事件处理
        }
        //1.1 尝试扔给目标实体
        else if (tag.getInt(RequestListItem.TAG_REPEAT_INTERVAL) <= 0 && targetEntity != null) {
            Vec3 targetDir = MathUtil.getFromToWithFriction(maid, targetEntity.position());
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
        if (!Config.twoStepAiResponse) return;
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

            ItemStack itemstack = ItemStackUtil.parseStack(itemTag.getCompound(RequestListItem.TAG_ITEMS_ITEM));
            if (itemstack.isEmpty()) continue;

            int collected = itemTag.getInt(RequestListItem.TAG_ITEMS_COLLECTED);
            int requested = itemTag.getInt(RequestListItem.TAG_ITEMS_REQUESTED);


            if (itemTag.getBoolean(RequestListItem.TAG_ITEMS_DONE)) {
                sb.append("[Finished]");
            } else {
                sb.append("[Processing]");
            }

            sb.append(itemstack.getHoverName().getString());
            sb.append(Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(itemstack.getItem())));
            sb.append(" has collected ");
            sb.append(collected);
            sb.append(" and plans to get ");
            sb.append(requested == -1 ? "any amount" : requested);
            sb.append(".");
            sb.append("\n");
        }
        FunctionToolCall functionToolCall = new FunctionToolCall("stock", "{}");
        String id = UUID.randomUUID().toString();
        ToolCall toolCall = new ToolCall(id, functionToolCall);
        List<LLMMessage> llmMessages = aiChatManager.getSetting().map(s -> {
            String setting = s.getSetting(maid, AiUtils.transformLanguage(owner.getLanguage()));
            CappedQueue<LLMMessage> history = aiChatManager.getHistory();
            List<LLMMessage> chatList = Lists.newArrayList();
            chatList.add(LLMMessage.systemChat(maid, setting));
            // 倒序遍历，将历史对话加载进去
            history.getDeque().descendingIterator().forEachRemaining(chatList::add);
            return chatList;
        }).orElseGet(() -> {
            if (StringUtils.isNotBlank(aiChatManager.customSetting)) {
                String setting = PapiReplacer.replace(aiChatManager.customSetting, maid, AiUtils.transformLanguage(owner.getLanguage()));
                CappedQueue<LLMMessage> history = aiChatManager.getHistory();
                List<LLMMessage> chatList = Lists.newArrayList();
                chatList.add(LLMMessage.systemChat(maid, setting));
                // 倒序遍历，将历史对话加载进去
                history.getDeque().descendingIterator().forEachRemaining(chatList::add);
                return chatList;
            }
            return Lists.newArrayList();
        });
        llmMessages.add(LLMMessage.userChat(maid, "Please query and tell me the situation of last task."));
        llmMessages.add(LLMMessage.assistantChat(maid, "Query task progress.", List.of(toolCall)));
        llmMessages.add(LLMMessage.toolChat(maid, sb.toString(), id));
        LLMCallback callback = new LLMCallback(aiChatManager,
                "Please query and tell me the situation of last task.",
                0);
        LLMConfig config = LLMConfig.normalChat(aiChatManager.getLLMModel(), maid);
        client.chat(llmMessages, config, callback);
    }

    private static void dispatchedTaskDone(EntityMaid maid, ItemStack reqList) {
        CompoundTag data = RequestListItem.getVirtualData(reqList);
        if (data == null) return;
        UUID masterUUID = data.getUUID("master");
        int index = data.getInt("index");
        Entity targetEntity = ((ServerLevel) maid.level()).getEntity(masterUUID);
        if (!(targetEntity instanceof EntityMaid toMaid)) return;
        CraftMemory targetCraftingMemory = MemoryUtil.getCrafting(toMaid);
        if (!targetCraftingMemory.hasPlan()) return;
        CraftLayerChain targetPlan = targetCraftingMemory.plan();
        targetPlan.dispatchedDone(maid,
                toMaid,
                index,
                RequestListItem.isAllSuccess(reqList),
                reqList
        );
        targetPlan.showCraftingProgress(toMaid);
    }

    private static void dispatchFindTaskDone(EntityMaid maid, ItemStack reqList) {
        CompoundTag data = RequestListItem.getVirtualData(reqList);
        if (data == null) return;
        UUID masterUUID = data.getUUID("master");
        Entity targetEntity = ((ServerLevel) maid.level()).getEntity(masterUUID);
        ItemStack toItem = reqList.copy();
        RequestListItem.clearAllNonSuccess(toItem);

        CompoundTag tag = toItem.getOrCreateTag();
        //生成一个非虚拟请求列表
        tag.remove(RequestListItem.TAG_VIRTUAL);
        tag.remove(RequestListItem.TAG_VIRTUAL_SOURCE);
        toItem.setTag(tag);

        if (targetEntity instanceof EntityMaid toMaid) {
            ItemStack restItem = InvUtil.tryPlace(toMaid.getAvailableInv(true), toItem);
            if (restItem.isEmpty()) {
                MemoryUtil.getRequestProgress(toMaid).newWork(RequestListItem.getUUID(toItem));
                MemoryUtil.getRequestProgress(toMaid).setTryCrafting(true);
            }
            toItem = restItem;
        }
        if (!toItem.isEmpty()) {
            InvUtil.throwItem(maid, toItem);
        }
    }

    /**
     * 创建虚拟的请求列表（显示为女仆事务且玩家不可使用，结束后自动销毁）
     *
     * @param list           物品列表
     * @param target         目标
     * @param targetEntity   目标实体
     * @param virtual_source 来源。用于回调事件。还在设计中。
     * @return 虚拟物品
     */
    public static ItemStack makeVirtualItemStack(List<ItemStack> list, @Nullable Target target, @Nullable Entity targetEntity, String virtual_source) {
        return makeVirtualItemStack(list, target, targetEntity, virtual_source, ItemStackUtil.MATCH_TYPE.AUTO);
    }

    /**
     * 创建虚拟的请求列表（显示为女仆事务且玩家不可使用，结束后自动销毁）
     *
     * @param list           物品列表
     * @param target         目标
     * @param targetEntity   目标实体
     * @param virtual_source 来源。用于回调事件。还在设计中。
     * @return 虚拟物品
     */
    public static ItemStack makeVirtualItemStack(List<ItemStack> list, @Nullable Target target, @Nullable Entity targetEntity, String virtual_source, ItemStackUtil.MATCH_TYPE match) {
        ItemStack itemStack = ItemRegistry.REQUEST_LIST_ITEM.get().getDefaultInstance().copy();
        CompoundTag tag = itemStack.getOrCreateTag();
        tag.putBoolean(RequestListItem.TAG_VIRTUAL, true);
        tag.putString(RequestListItem.TAG_VIRTUAL_SOURCE, virtual_source);
        ListTag listTag = new ListTag();
        for (int i = 0; i < Math.max(list.size(), 10); i++) {
            ItemStack item = i < list.size() ? list.get(i) : ItemStack.EMPTY;
            CompoundTag tmp = new CompoundTag();
            tmp.putInt(RequestListItem.TAG_ITEMS_REQUESTED, item.getCount());
            tmp.put(RequestListItem.TAG_ITEMS_ITEM, ItemStackUtil.saveStack(item.copyWithCount(1)));
            listTag.add(tmp);
        }
        tag.put(RequestListItem.TAG_ITEMS, listTag);
        tag.putBoolean(RequestListItem.TAG_BLACKMODE, false);
        tag.putBoolean(RequestListItem.TAG_STOCK_MODE, false);
        tag.putBoolean(RequestListItem.TAG_IGNORE_TASK, false);
        tag.putInt(RequestListItem.TAG_MATCH, match.ordinal());
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

    public static ItemStack makeVirtualItemStack(ItemStack source, String virtual_source) {
        CompoundTag tag = source.getTag().copy();

        tag.putBoolean(RequestListItem.TAG_VIRTUAL, true);
        tag.putString(RequestListItem.TAG_VIRTUAL_SOURCE, virtual_source);

        ItemStack itemStack = ItemRegistry.REQUEST_LIST_ITEM.get().getDefaultInstance().copy();
        itemStack.setTag(tag);
        return itemStack;
    }

    /**
     * 设置当前请求列表的目标为已访问的
     *
     * @param level
     * @param maid
     * @param target
     */
    public static void markVisForCurrentRequestList(ServerLevel level, EntityMaid maid, AbstractTargetMemory target) {
        Target storageBlock = RequestListItem.getStorageBlock(maid.getMainHandItem());
        if (storageBlock != null) {
            target.addVisitedPos(storageBlock);
            StorageAccessUtil.checkNearByContainers(level, storageBlock.getPos(), pos -> {
                target.addVisitedPos(storageBlock.sameType(pos, null));
            });
        }
    }
}
