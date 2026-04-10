package studio.fantasyit.maid_storage_manager.ai;

import com.github.tartaricacid.touhoulittlemaid.ai.manager.entity.LLMCallback;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.schema.parameter.*;
import com.github.tartaricacid.touhoulittlemaid.ai.service.llm.LLMClient;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.registries.ForgeRegistries;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.util.Conditions;
import studio.fantasyit.maid_storage_manager.util.InvUtil;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;
import studio.fantasyit.maid_storage_manager.util.RequestItemUtil;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class StorageFetchFunction extends AbstractTool<StorageFetchFunction.StorageFetchFunctionDataList> {
    private static final Map<UUID, CompletableFuture<String>> RUNNING_FETCH = new HashMap<>();

    public static void ends(EntityMaid maid, ItemStack reqList) {
        CompletableFuture<String> future = RUNNING_FETCH.get(maid.getUUID());
        if(future == null)
            return;
        JsonArray result = new JsonArray();
        CompoundTag tag = reqList.getOrCreateTag();
        ListTag list = tag.getList(RequestListItem.TAG_ITEMS, ListTag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag itemTag = list.getCompound(i);
            if (!itemTag.contains(RequestListItem.TAG_ITEMS_ITEM)) continue;
            JsonObject item = new JsonObject();
            ItemStack itemstack = ItemStackUtil.parseStack(itemTag.getCompound(RequestListItem.TAG_ITEMS_ITEM));
            if (itemstack.isEmpty()) continue;

            int collected = itemTag.getInt(RequestListItem.TAG_ITEMS_COLLECTED);

            item.addProperty("collected", collected);
            item.addProperty("id", Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(itemstack.getItem())).toString());
            result.add(item);
        }
        future.complete(new Gson().toJson(result));
    }

    public static void tick(MinecraftServer server) {
        for (Map.Entry<UUID, CompletableFuture<String>> entry : RUNNING_FETCH.entrySet()) {
            for(ServerLevel l:server.getAllLevels()){
                if(l.getEntity(entry.getKey()) instanceof EntityMaid maid){
                    if(!Conditions.takingRequestList(maid)){
                        entry.getValue().complete(AiUtils.commonFailJson("Request task was canceled unexpectedly"));
                    }
                }
            }
        }
        RUNNING_FETCH.entrySet().removeIf(entry -> entry.getValue().isDone());
    }

    @Override
    public String id() {
        return "storage_fetch";
    }

    @Override
    public String summary(EntityMaid maid) {
        return AiUtils.toolDenyTemplate("fetch_item_manual");
    }

    @Override
    public Parameter parameters(ObjectParameter root, EntityMaid maid) {
        ObjectParameter singleParam = ObjectParameter.create();
        singleParam.addProperties("itemId", StringParameter.create().setPattern("[a-zA-Z0-9_]+:[a-zA-Z0-9_]+"));
        singleParam.addProperties("count", NumberParameter.create());
        ArrayParameter arrayParameter = ArrayParameter.create().setItems(singleParam);
        root.addProperties("list", arrayParameter);
        return root;
    }
    @Override
    public Codec<StorageFetchFunctionDataList> codec() {
        return StorageFetchFunctionDataList.CODEC;
    }

    @Override
    public Component invocationSummaryComponent(StorageFetchFunctionDataList result) {

        if(result.list().isEmpty())
            return Component.empty();
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(result.list().get(0).itemId));
        if(item == null)
            return Component.empty();
        Component displayItemName = item.getDefaultInstance().getHoverName();
        if(result.list().size() > 1)
            displayItemName = Component.literal("").append(displayItemName).append(Component.translatable("chat_bubbles.maid_storage_manager.ai.storage_fetch_etc", result.list().size()));
        return Component.translatable("chat_bubbles.maid_storage_manager.ai.storage_fetch", displayItemName).withStyle(ChatFormatting.GRAY);
    }

    @Override
    public String call(StorageFetchFunctionDataList data, EntityMaid maid, LLMCallback callback) {
        return "";
    }

    @Override
    public CompletableFuture<LLMCallback> onCallAsync(String toolCallId, StorageFetchFunctionDataList data, LLMCallback callback, LLMClient client) {
        EntityMaid maid = callback.getMaid();
        List<StorageFetchFunctionData> storageFetchFunctionData = data.list;

        List<ItemStack> list = new ArrayList<>();
        for(StorageFetchFunctionData i: storageFetchFunctionData){
            ResourceLocation resourceLocation = new ResourceLocation(i.itemId);
            Item item = ForgeRegistries.ITEMS.getValue(resourceLocation);
            if(item == null)
                return CompletableFuture.completedFuture( callback.addToolResult(AiUtils.commonFailJson(i.itemId+" is not a valid item."), id()));
            ItemStack itemStack = item.getDefaultInstance();
            list.add(itemStack.copyWithCount(i.count));
        }
        if (list.isEmpty()) {
            return CompletableFuture.completedFuture(callback.addToolResult(AiUtils.commonFailJson("Item list is empty"), id()));
        } else {
            CombinedInvWrapper availableInv = maid.getAvailableInv(true);
            if (InvUtil.hasAnyFree(availableInv)) {
                InvUtil.tryPlace(availableInv, RequestItemUtil.makeVirtualItemStack(list, null, maid.getOwner(), "AI"));

                CompletableFuture<LLMCallback> future = new CompletableFuture<>();
                CompletableFuture<String> task = new CompletableFuture<>();
                RUNNING_FETCH.put(maid.getUUID(), task);
                task.thenAccept(result -> callback.runOnServerThread(() -> {
                    callback.addToolResult(result, toolCallId);
                    RUNNING_FETCH.remove(maid.getUUID());
                    future.complete(callback);
                }));
                return future;
            } else {
                return CompletableFuture.completedFuture(callback.addToolResult(AiUtils.commonFailJson("Backpack has no free space"), id()));
            }
        }
    }

    public record StorageFetchFunctionDataList(List<StorageFetchFunctionData> list) {
        public static Codec<StorageFetchFunctionDataList> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec
                                .list(StorageFetchFunctionData.CODEC)
                                .fieldOf("list")
                                .forGetter(StorageFetchFunctionDataList::list)
                ).apply(instance, StorageFetchFunctionDataList::new));
    }

    public record StorageFetchFunctionData(String itemId, int count) {
        public static Codec<StorageFetchFunctionData> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.STRING.fieldOf("itemId").forGetter(StorageFetchFunctionData::itemId),
                        Codec.INT.fieldOf("count").forGetter(StorageFetchFunctionData::count)
                ).apply(instance, StorageFetchFunctionData::new));
    }
}
