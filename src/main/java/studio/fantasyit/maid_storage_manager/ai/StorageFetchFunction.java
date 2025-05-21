package studio.fantasyit.maid_storage_manager.ai;

import com.github.tartaricacid.touhoulittlemaid.ai.service.function.IFunctionCall;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.response.KeepToolResponse;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.response.ToolResponse;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.schema.parameter.*;
import com.github.tartaricacid.touhoulittlemaid.ai.service.llm.openai.request.ChatCompletion;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;
import studio.fantasyit.maid_storage_manager.maid.task.StorageManageTask;
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
            if (key != null) {
                if (keys.stream().noneMatch(k -> k.getA().equals(key.toString())))
                    keys.add(new Pair<>(key.toString(), item.getHoverName().getString()));
            }
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
        return "如果你需要获取某个物品给主人，可以使用这个工具。这个工具要求你传入list作为参数，list的值可以是物品ID中多个组成的JSON数组，返回格式：`{\"list\":[{\"itemId\":\"<物品ID>\",\"count\":<数量>}...]}`，物品ID的格式是`命名空间:id`，传入的数量不得超过十个。物品ID可以通过查询仓库库存工具取得。该工具被使用后，将会生成一个任务清单，物品被加入了任务清单不代表物品已经找到了，只是代表工具会稍后去查找物品，只有到工具任务清单中的任务全部完成后，主人才会拿到物品。你将得到已经加入任务的物品和未加入任务的物品ID和物品名。请始终用物品名称而不是物品ID来称呼物品。";
    }

    @Override
    public Parameter addParameters(ObjectParameter objectParameter, EntityMaid entityMaid) {
        ObjectParameter singleParam = ObjectParameter.create();
        singleParam.addProperties("itemId", StringParameter.create().setPattern("[a-zA-Z0-9_]+:[a-zA-Z0-9_]+"));
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
    public boolean addToChatCompletion(EntityMaid maid, ChatCompletion chatCompletion) {
        return maid.getTask().getUid().equals(StorageManageTask.TASK_ID);
    }

    @Override
    public ToolResponse onToolCall(StorageFetchFunctionDataList storageFetchFunctionDataList, EntityMaid entityMaid) {
        Set<Pair<String, String>> itemKeys = getItemKeys(MemoryUtil.getViewedInventory(entityMaid).flatten());
        StringBuilder message = new StringBuilder();
        MutableBoolean partial = new MutableBoolean(false);
        List<StorageFetchFunctionData> storageFetchFunctionData = storageFetchFunctionDataList.list;
        List<ItemStack> list = storageFetchFunctionData.stream()
                .map(i -> {
                    ResourceLocation resourceLocation = new ResourceLocation(i.itemId);
                    Item item = ForgeRegistries.ITEMS.getValue(resourceLocation);
                    if (item == null) {
                        partial.setTrue();
                        message.append("未加入任务: [").append(i.itemId).append("]:").append("未知 没有对应的物品\n");
                        return null;
                    } else if (itemKeys.stream().noneMatch(ik -> ik.getA().equals(i.itemId))) {
                        partial.setTrue();
                        message.append("未加入任务: [").append(i.itemId).append("]").append(item.getDefaultInstance().getHoverName()).append(" 在仓库中没有\n");
                        return null;
                    } else {
                        message.append("成功加入任务: [").append(i.itemId).append("]").append(item.getDefaultInstance().getHoverName()).append("\n");
                    }
                    return item.getDefaultInstance().copyWithCount(i.count);
                })
                .filter(Objects::nonNull)
                .toList();
        if (list.isEmpty()) {
            message.append("失败！没有任何可以被查找的物品");
        } else {
            CombinedInvWrapper availableInv = entityMaid.getAvailableInv(true);
            if (InvUtil.hasAnyFree(availableInv)) {
                InvUtil.tryPlace(availableInv, RequestItemUtil.makeVirtualItemStack(list, null, entityMaid.getOwner(),"AI"));
                if (partial.getValue())
                    message.append("部分物品已经被添加到查找任务。");
                else
                    message.append("所有任务都已经被添加到查找任务。");
                message.append("请始终用物品名称而不是物品ID来称呼物品。工具会稍后去查找物品，只有到工具任务清单中的任务全部完成后，主人才会拿到物品。");
            } else {
                return new KeepToolResponse("失败！背包目前没有剩余空间。");
            }
        }
        return new KeepToolResponse(message.toString());
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
