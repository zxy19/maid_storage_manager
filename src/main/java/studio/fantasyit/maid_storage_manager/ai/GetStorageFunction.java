package studio.fantasyit.maid_storage_manager.ai;

import com.github.tartaricacid.touhoulittlemaid.ai.service.function.IFunctionCall;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.response.ToolResponse;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.schema.parameter.ObjectParameter;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.schema.parameter.Parameter;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.schema.parameter.StringParameter;
import com.github.tartaricacid.touhoulittlemaid.ai.service.llm.openai.request.ChatCompletion;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;
import studio.fantasyit.maid_storage_manager.items.CraftGuide;
import studio.fantasyit.maid_storage_manager.maid.task.StorageManageTask;
import studio.fantasyit.maid_storage_manager.registry.DataComponentRegistry;
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

    protected Set<ItemInfo> getItemKeys(List<InventoryItem> list, RegistryAccess registryAccess) {
        Set<ItemInfo> keys = new HashSet<>();
        Registry<Item> reg = registryAccess.registry(Registries.ITEM).get();
        ObjIntConsumer<ItemStack> add = (ItemStack item, int count) -> {
            @Nullable ResourceLocation key = reg.getKey(item.getItem());
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
                CraftGuideData cgd = itemStack.getOrDefault(DataComponentRegistry.CRAFT_GUIDE_DATA, CraftGuide.empty());
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
        return "If you need to retrieve some item IDs stored in the warehouse, you can call this tool. The tool requires a filter parameter - you can input an item ID, a keyword from the item's name, or leave it blank to get all items. The results will be returned in the format: [Item ID]: Item Name * Stock Quantity. If there is a \"(Craftable)\" suffix after the quantity (e.g., [ItemID]:ItemName * 5(Craftable)), it indicates the item can be synthesized upon request, allowing you to request quantities exceeding current stock. When communicating with players, always use item names rather than item IDs to reference items.";
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
        Set<ItemInfo> itemKeys = getItemKeys(MemoryUtil.getViewedInventory(entityMaid).flatten(), entityMaid.registryAccess());
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
                    message.append("(Craftable)");
                message.append("\n");
                anyMatch.setTrue();
            }
        });
        if (!anyMatch.getValue()) {
            message.append("No result that matches the filter.");
        } else {
            message.append("Please always refer to items by their name and not their ID.");
        }
        return new ToolResponse(message.toString());
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
