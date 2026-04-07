package studio.fantasyit.maid_storage_manager.ai;

import com.github.tartaricacid.touhoulittlemaid.ai.manager.entity.LLMCallback;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.schema.parameter.BoolParameter;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.schema.parameter.ObjectParameter;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.schema.parameter.Parameter;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.schema.parameter.StringParameter;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.capability.InventoryListDataProvider;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;
import studio.fantasyit.maid_storage_manager.network.AIMatchLocalizedItemS2CPacket;
import studio.fantasyit.maid_storage_manager.network.Network;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.ObjIntConsumer;

public class GetStorageFunction extends AbstractTool<GetStorageFunction.FilterData> {
    private static final Map<Integer,CompletableFuture<List<ItemStackI18N>>> RPC_CALLS = new HashMap<>();
    private static int rpcId = 0;
    public static final String ID = "get_storage";
    public static CompletableFuture<LLMCallback> sendQueryItemPatternRPC(EntityMaid maid, FilterData data, CompletableFuture<List<ItemStackI18N>> c, LLMCallback  callback,String toolCallId){
        LivingEntity owner = maid.getOwner();
        int id = ++rpcId;
        String pattern = data.filter;
        RPC_CALLS.put(id, c);
        @NotNull LazyOptional<InventoryListDataProvider.InventoryListData> capp = maid.getServer().overworld().getCapability(InventoryListDataProvider.INVENTORY_LIST_DATA_CAPABILITY);

        CompletableFuture<LLMCallback> cb = new CompletableFuture<>();
        c.completeOnTimeout(List.of(),3, TimeUnit.SECONDS).thenAccept(l->{
            RPC_CALLS.remove(id);
            callback.runOnServerThread(()->{
                JsonArray results = new JsonArray();
                Set<ItemInfo> itemKeys = getItemKeys(MemoryUtil.getViewedInventory(maid).flatten());
                JsonArray result = new JsonArray();
                MutableBoolean anyMatch = new MutableBoolean(false);
                Map<String,ItemStackI18N> itemMap = new HashMap<>();
                for(ItemStackI18N itemStackI18N : l)
                    itemMap.put(itemStackI18N.id(),itemStackI18N);
                itemKeys.forEach(ik -> {
                    ItemStackI18N itemStackI18N = itemMap.getOrDefault(ik.id(),ItemStackI18N.INVALID);
                    boolean valid =StringUtil.isNullOrEmpty(data.filter);
                    valid |= itemStackI18N.name.contains(pattern) || ik.id().contains(pattern);
                    if(data.queryTooltip)
                        valid |= itemStackI18N.tooltip.contains( pattern);
                    if (valid) {
                        JsonObject item = new JsonObject();
                        item.addProperty("id", ik.id());
                        item.addProperty("name", itemStackI18N.name());
                        item.addProperty("count", ik.count().getValue());
                        item.addProperty("craftable", ik.craftable().getValue());
                        if(data.queryTooltip)
                            item.addProperty("tooltip", itemStackI18N.tooltip());
                        result.add(item);
                        anyMatch.setTrue();
                    }
                });
                String rString;
                if (!anyMatch.getValue()) {
                    rString = AiUtils.commonFailJson("No result that matches the filter.");
                }else {
                    rString = new Gson().toJson(result);
                }
                cb.complete(callback.addToolResult(rString,toolCallId));
            });
        });
        if(owner instanceof ServerPlayer player && capp.isPresent()) {
            UUID uuid = maid.getUUID();
            InventoryListDataProvider.InventoryListData inventoryListData = capp.orElse(null);
            List<InventoryItem> flatten = MemoryUtil.getViewedInventory(maid).flatten();
            inventoryListData.addWithCraftable(uuid, flatten);
            inventoryListData.sendTo(uuid, player);
            Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new AIMatchLocalizedItemS2CPacket(id,uuid, pattern));
            inventoryListData.remove(uuid);
        }else{
            c.complete(List.of());
        }
        return cb;
    }
    public static void handleRPC(int id,List<ItemStackI18N> l){
        if(RPC_CALLS.containsKey(id)) {
            RPC_CALLS.get(id).complete(l);
        }
    }
    @Override
    public String id() {
        return ID;
    }

    protected static Set<ItemInfo> getItemKeys(List<InventoryItem> list) {
        Set<ItemInfo> keys = new HashSet<>();
        ObjIntConsumer<ItemStack> add = (ItemStack item, int count) -> {
            @Nullable ResourceLocation key = ForgeRegistries.ITEMS.getKey(item.getItem());
            if (key != null) {
                keys.stream().filter(k -> k.id().equals(key.toString())).findFirst()
                        .ifPresentOrElse(
                                ii -> {
                                    ii.count.add(count);
                                    if (count == 0)
                                        ii.craftable.setTrue();
                                },
                                () -> keys.add(new ItemInfo(key.toString(),
                                        new MutableInt(count),
                                        new MutableBoolean(count == 0)))
                        );
            }
        };
        list.forEach(item -> {
            ItemStack itemStack = item.itemStack;
            if (itemStack.is(ItemRegistry.CRAFT_GUIDE.get())) {
                CraftGuideData cgd = CraftGuideData.fromItemStack(itemStack);
                cgd.getOutput().forEach(t -> add.accept(t, 0));
            } else {
                if (item.totalCount != 0)
                    add.accept(itemStack, item.totalCount);
            }
        });
        return keys;
    }

    @Override
    public String summary(EntityMaid maid) {
        return "When need to get item in storage or need to search for names, use this tool. Read skill `get_storage` before using this tool.";
    }

    @Override
    public Parameter parameters(ObjectParameter root, EntityMaid maid) {
        root.addProperties("filter", StringParameter.create());
        root.addProperties("queryTooltip", BoolParameter.create().setDefaultValue("false"));
        root.addProperties("showTooltip", BoolParameter.create().setDefaultValue("false"));
        return root;
    }

    @Override
    public Codec<FilterData> codec() {
        return FilterData.CODEC;
    }

    @Override
    public String invocationSummary(FilterData result) {
        return "Finding %s...".formatted(result.filter);
    }

    @Override
    public String call(FilterData data, EntityMaid maid, LLMCallback callback) {
        return "";
    }

    @Override
    public CompletableFuture<LLMCallback> onCallAsync(String toolCallId, FilterData result, LLMCallback callback) {
        CompletableFuture<List<ItemStackI18N>> res = new CompletableFuture<>();
        return sendQueryItemPatternRPC(callback.getMaid(), result, res, callback,toolCallId);
    }

    protected record ItemInfo(String id, MutableInt count, MutableBoolean craftable) {
    }

    public record FilterData(String filter,Boolean queryTooltip,Boolean showTooltip) {
        public static Codec<FilterData> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.STRING.fieldOf("filter").forGetter(FilterData::filter),
                        Codec.BOOL.fieldOf("queryTooltip").forGetter(FilterData::queryTooltip),
                        Codec.BOOL.fieldOf("showTooltip").forGetter(FilterData::showTooltip)
                ).apply(instance, FilterData::new));
    }
    public record ItemStackI18N(String id, String name,String tooltip){
        public static final ItemStackI18N INVALID = new ItemStackI18N("","","");
    }
}
