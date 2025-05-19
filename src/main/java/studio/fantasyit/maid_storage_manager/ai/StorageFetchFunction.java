package studio.fantasyit.maid_storage_manager.ai;

import com.github.tartaricacid.touhoulittlemaid.ai.manager.response.ResponseChat;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.IFunctionCall;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.schema.parameter.*;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.util.InvUtil;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;
import studio.fantasyit.maid_storage_manager.util.RequestItemUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public class StorageFetchFunction implements IFunctionCall<StorageFetchFunction.StorageFetchFunctionDataList> {

    @Override
    public String getId() {
        return "storage_fetch";
    }

    protected Set<Pair<String, String>> getItemKeys(List<InventoryItem> list) {
        Set<Pair<String, String>> keys = new HashSet<>();
        Consumer<ItemStack> add = (ItemStack item) -> {
            @Nullable ResourceLocation key = ForgeRegistries.ITEMS.getKey(item.getItem());
            if (key != null) keys.add(new Pair<>(key.toString(), item.getHoverName().getString()));
        };
        list.forEach(item -> {
            ItemStack itemStack = item.itemStack;
            if (itemStack.is(ItemRegistry.CRAFT_GUIDE.get())) {
                CraftGuideData cgd = CraftGuideData.fromItemStack(itemStack);
                cgd.getOutput().forEach(add);
            } else {
                add.accept(itemStack);
            }
        });
        return keys;
    }

    @Override
    public String getDescription(EntityMaid entityMaid) {
        StringBuilder paramDescription = new StringBuilder("如果你需要获取某个物品给主人，可以使用这个工具。这个工具要求你传入list作为参数，list的值可以是下面的物品ID中多个组成的JSON数组，返回格式：`{\"list\":[{\"itemId\":\"<物品ID>\",\"count\":<数量>}...]}`，物品ID的格式是`命名空间:id`。接下来是物品ID和名字的对应关系，每一行分别是[物品id]：物品名字。");
        getItemKeys(MemoryUtil.getViewedInventory(entityMaid).flatten()).forEach(itemName -> paramDescription
                .append("[")
                .append(itemName.getA())
                .append("]：")
                .append(itemName.getB()).append("\n"));

        return paramDescription.toString();
    }

    @Override
    public Parameter addParameters(ObjectParameter objectParameter, EntityMaid entityMaid) {
        StringParameter paramItemId = StringParameter.create();
        getItemKeys(MemoryUtil.getViewedInventory(entityMaid).flatten())
                .stream()
                .map(Pair::getB)
                .forEach(paramItemId::addEnumValues);
        ObjectParameter singleParam = ObjectParameter.create();
        singleParam.addProperties("itemId", paramItemId);
        singleParam.addProperties("count", NumberParameter.create());
        ArrayParameter arrayParameter = ArrayParameter.create().setItems(singleParam);
        objectParameter.addProperties("list", arrayParameter);
        return objectParameter;
    }

    @Override
    public Codec<StorageFetchFunctionDataList> codec() {
        return StorageFetchFunctionDataList.CODEC;
    }

    @Override
    public ResponseChat onToolCall(StorageFetchFunctionDataList storageFetchFunctionDataList, EntityMaid entityMaid) {
        List<StorageFetchFunctionData> storageFetchFunctionData = storageFetchFunctionDataList.list;
        List<ItemStack> list = storageFetchFunctionData.stream().map(i -> {
                    ResourceLocation resourceLocation = new ResourceLocation(i.itemId);
                    Item item = ForgeRegistries.ITEMS.getValue(resourceLocation);
                    return item == null ? null : item.getDefaultInstance().copyWithCount(i.count);
                })
                .filter(Objects::nonNull)
                .toList();
        String message;
        if (list.isEmpty()) {
            message = "没有找到对应的物品";
        } else {
            CombinedInvWrapper availableInv = entityMaid.getAvailableInv(true);
            if (InvUtil.hasAnyFree(availableInv)) {
                InvUtil.tryPlace(availableInv, RequestItemUtil.makeVirtualItemStack(list, null, entityMaid.getOwner()));
                message = "已经成功生成物品拿去任务";
            } else {
                message = "背包目前没有剩余空间";
            }
        }
//        LivingEntity owner = entityMaid.getOwner();
//        if (owner instanceof ServerPlayer sp) {
//            entityMaid.getAiChatManager().addAssistantHistory("我的上一个物品拿取的结果是：" + message + "。工具调用完成。");
//            entityMaid.getAiChatManager().chat("请根据工具调用的结果重新回答，如告诉用户任务已经完成或其他。", sp.getLanguage());
//        }
        return new ResponseChat(message, "");
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
