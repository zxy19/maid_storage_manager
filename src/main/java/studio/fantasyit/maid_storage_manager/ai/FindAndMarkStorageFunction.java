package studio.fantasyit.maid_storage_manager.ai;

import com.github.tartaricacid.touhoulittlemaid.ai.service.function.IFunctionCall;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.response.ToolResponse;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.schema.parameter.ArrayParameter;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.schema.parameter.ObjectParameter;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.schema.parameter.Parameter;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.schema.parameter.StringParameter;
import com.github.tartaricacid.touhoulittlemaid.ai.service.llm.openai.request.ChatCompletion;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;
import studio.fantasyit.maid_storage_manager.maid.memory.ViewedInventoryMemory;
import studio.fantasyit.maid_storage_manager.maid.task.StorageManageTask;
import studio.fantasyit.maid_storage_manager.network.Network;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

import java.util.List;
import java.util.Map;

public class FindAndMarkStorageFunction implements IFunctionCall<FindAndMarkStorageFunction.ItemIdData> {

    @Override
    public String getId() {
        return "find_mark_storage";
    }

    @Override
    public String getDescription(EntityMaid entityMaid) {
        //如果你想要查找某个物品存储的位置，你可以使用这个工具。工具要求你传入参数`item`，参数为物品ID的列表。物品ID必须符合 命名空间:路径 的格式，。当工具使用后，你将得到一个列表，表示被找到的位置。这些位置会在主人的屏幕上被高亮框出来。
        return "If you want to find the storage location of an item, you can use this tool. The tool requires you to pass in the parameter `item`, which is a list of item ID. The item ID must match the format of namespace:path, e.g. `{\"item\":[\"minecraft:egg\",\"minecraft:stone\"]}`. When the tool is used, you will get a list of locations that were found. If the tool was successfully called, the locations will be highlighted on the owner's screen. You must get ItemId from `get_storage` tool.";
    }

    @Override
    public Parameter addParameters(ObjectParameter objectParameter, EntityMaid entityMaid) {
        objectParameter.addProperties("item", ArrayParameter.create().setItems(StringParameter.create().setPattern("[a-zA-Z0-9_]+:[a-zA-Z0-9_]+")));
        return objectParameter;
    }

    @Override
    public Codec<ItemIdData> codec() {
        return ItemIdData.CODEC;
    }

    @Override
    public boolean addToChatCompletion(EntityMaid maid, ChatCompletion chatCompletion) {
        return maid.getTask().getUid().equals(StorageManageTask.TASK_ID);
    }

    @Override
    public ToolResponse onToolCall(ItemIdData itemIdData, EntityMaid entityMaid) {
        Map<Target, List<ViewedInventoryMemory.ItemCount>> itemKeys = MemoryUtil.getViewedInventory(entityMaid).positionFlatten();
        StringBuilder message = new StringBuilder();
        for (String itemId : itemIdData.itemId()) {
            ResourceLocation resourceLocation = new ResourceLocation(itemId);
            Item item = ForgeRegistries.ITEMS.getValue(resourceLocation);
            if (item == null) {
                message.append("Fail. Item ID: ").append(itemId).append(" is not a valid item.\n");
            }
            ItemStack itemStack = item.getDefaultInstance();
            InventoryItem toShowItem = new InventoryItem(itemStack, 0);
            MutableBoolean found = new MutableBoolean(false);

            itemKeys.forEach((pos, list) -> {
                MutableInt count = new MutableInt(0);
                list.stream()
                        .filter(ic -> ItemStackUtil.isSame(ic.item(), itemStack, false))
                        .forEach(ic -> count.add(ic.getSecond()));
                if (count.intValue() > 0) {
                    toShowItem.addCount(pos, count.intValue());
                    found.setTrue();
                    message.append("Found ").append(count.intValue()).append(" ").append(" at ").append(pos).append("\n");
                }
            });
            if (!found.getValue()) {
                message.append("Fail. Item ID: ").append(itemId).append(" is not found.\n");
            }
            message.append("Success! Positions for ").append(itemId).append(" are been highlighted.\n");
            LivingEntity owner = entityMaid.getOwner();
            if (owner instanceof ServerPlayer player)
                Network.sendShowInvPacket(player, toShowItem, 600);
        }
        message.append("Please always refer to items by their name and not their ID.");
        return new ToolResponse(message.toString());
    }

    public record ItemIdData(List<String> itemId) {
        public static Codec<ItemIdData> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.STRING.listOf().fieldOf("item").forGetter(ItemIdData::itemId)
                ).apply(instance, ItemIdData::new));
    }
}
