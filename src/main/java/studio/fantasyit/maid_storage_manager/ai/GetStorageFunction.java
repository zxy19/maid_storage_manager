package studio.fantasyit.maid_storage_manager.ai;

import com.github.tartaricacid.touhoulittlemaid.ai.service.function.IFunctionCall;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.response.KeepToolResponse;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.response.ToolResponse;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.schema.parameter.ObjectParameter;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.schema.parameter.Parameter;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.schema.parameter.StringParameter;
import com.github.tartaricacid.touhoulittlemaid.ai.service.llm.openai.request.ChatCompletion;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;
import studio.fantasyit.maid_storage_manager.maid.task.StorageManageTask;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.ObjIntConsumer;

public class GetStorageFunction implements IFunctionCall<GetStorageFunction.FilterData> {

    @Override
    public String getId() {
        return "get_storage";
    }

    protected Set<ItemInfo> getItemKeys(List<InventoryItem> list) {
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
                                        item.getHoverName().getString(),
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
    public String getDescription(EntityMaid entityMaid) {
        return "如果你需要获取仓库里存储的所有的物品ID，可以调用这个工具。这个工具需要传入一个过滤器参数，你可以传入想要查找的物品的ID或者名字的关键字，或者将其留空来获取所有物品。工具将会把物品ID按照`[物品ID]:物品名字 * 库存数量`的格式发送给你。如果数量后面有(合成)的字样，如`[物品ID]:物品名字 * 库存数量(合成)`，说明这个物品在请求的时候可以被合成，可以请求超过库存数量的物品。请注意，和玩家对话时应该始终使用物品名而不是物品ID来称呼物品。";
    }

    @Override
    public boolean addToChatCompletion(EntityMaid maid, ChatCompletion chatCompletion) {
        return maid.getTask().getUid().equals(StorageManageTask.TASK_ID);
    }

    @Override
    public Parameter addParameters(ObjectParameter objectParameter, EntityMaid entityMaid) {
        objectParameter.addProperties("filter", StringParameter.create());
        return objectParameter;
    }

    @Override
    public Codec<FilterData> codec() {
        return FilterData.CODEC;
    }

    @Override
    public ToolResponse onToolCall(FilterData filter, EntityMaid entityMaid) {
        Set<ItemInfo> itemKeys = getItemKeys(MemoryUtil.getViewedInventory(entityMaid).flatten());
        StringBuilder message = new StringBuilder();
        MutableBoolean anyMatch = new MutableBoolean(false);
        itemKeys.forEach(ik -> {
            if (StringUtil.isNullOrEmpty(filter.filter) || ik.name.contains(filter.filter) || ik.id().contains(filter.filter)) {
                message.append("[")
                        .append(ik.id())
                        .append("]:")
                        .append(ik.name())
                        .append(" * ")
                        .append(ik.count);
                if (ik.craftable.getValue())
                    message.append("(合成)");
                message.append("\n");
                anyMatch.setTrue();
            }
        });
        if (!anyMatch.getValue()) {
            message.append("没有找到任何符合条件的物品");
        } else {
            message.append("请始终用物品名称而不是物品ID来称呼物品。");
        }
        return new KeepToolResponse(message.toString());
    }

    protected record ItemInfo(String id, String name, MutableInt count, MutableBoolean craftable) {
    }

    public record FilterData(String filter) {
        public static Codec<FilterData> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.STRING.fieldOf("filter").forGetter(FilterData::filter)
                ).apply(instance, FilterData::new));
    }
}
