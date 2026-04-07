package studio.fantasyit.maid_storage_manager.ai;

import com.github.tartaricacid.touhoulittlemaid.ai.manager.entity.LLMCallback;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.schema.parameter.ArrayParameter;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;
import studio.fantasyit.maid_storage_manager.maid.memory.ViewedInventoryMemory;
import studio.fantasyit.maid_storage_manager.network.Network;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

import java.util.List;
import java.util.Map;

public class FindAndMarkStorageFunction extends AbstractTool<FindAndMarkStorageFunction.ItemIdData> {
    @Override
    public String id() {
        return "find_mark_storage";
    }

    @Override
    public String summary(EntityMaid maid) {
        return "Use this tool if you want to find the storage location of an item. You MUST read skill `find_storage` before using this tool.";
    }

    @Override
    public Parameter parameters(ObjectParameter root, EntityMaid maid) {
        root.addProperties("item", ArrayParameter.create().setItems(StringParameter.create().setPattern("[a-zA-Z0-9_]+:[a-zA-Z0-9_]+")));
        return root;
    }

    @Override
    public Codec<ItemIdData> codec() {
        return ItemIdData.CODEC;
    }

    @Override
    public String call(ItemIdData itemIdData, EntityMaid maid, LLMCallback callback) {

        Map<Target, List<ViewedInventoryMemory.ItemCount>> itemKeys = MemoryUtil.getViewedInventory(callback.getMaid()).positionFlatten();
        JsonArray result = new JsonArray();
        for (String itemId : itemIdData.itemId()) {
            ResourceLocation resourceLocation = new ResourceLocation(itemId);
            Item item = ForgeRegistries.ITEMS.getValue(resourceLocation);
            if (item == null) {
                return AiUtils.commonFailJson(itemId+" is not a valid item.");
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
                    JsonObject obj = new JsonObject();
                    obj.addProperty("id", resourceLocation.toString());
                    obj.addProperty("count",count.intValue());
                    obj.add("position",AiUtils.posToArray(pos.pos));
                    result.add(obj);
                }
            });
            LivingEntity owner = callback.getMaid().getOwner();
            if (owner instanceof ServerPlayer player)
                Network.sendShowInvPacket(player, toShowItem, 600);
        }

        return new Gson().toJson(result);
    }

    @Override
    public String invocationSummary(ItemIdData result) {
        return "Queried %d items".formatted(result.itemId.size());
    }

    public record ItemIdData(List<String> itemId) {
        public static Codec<ItemIdData> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.STRING.listOf().fieldOf("item").forGetter(ItemIdData::itemId)
                ).apply(instance, ItemIdData::new));
    }
}
