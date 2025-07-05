package studio.fantasyit.maid_storage_manager.craft.data;

import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.craft.work.CraftLayer;

import java.util.*;
import java.util.function.BiConsumer;

public class CraftResultContext {
    List<CraftLayer> layers;
    Map<ItemStack, Integer> itemConsumeCount;
    int slotConsume;

    public CraftResultContext(List<CraftLayer> layers) {
        this.layers = layers;
        this.itemConsumeCount = new HashMap<>();
        this.slotConsume = 0;

        for (CraftLayer layer : layers) {
            //女仆优先使用合成树上找到的剩余物品
            layer.getItems().forEach(i -> removeConsumeCount(i, i.getCount()));
            //如果是最后一层，那么移除终产物即可
            Optional<CraftGuideData> craftData = layer.getCraftData();
            if (craftData.isEmpty()) break;
            //否则，加入该层物品需要占用的背包空间
            layer.getItems().forEach(i -> addConsumeCount(i, i.getCount()));
            craftData.get().getAllOutputItemsWithOptional().forEach(i -> addConsumeCount(i, i.getCount() * layer.getCount()));
            //记录最大背包占用
            this.slotConsume = Math.max(this.slotConsume, itemConsumeCount.keySet().size());
            //移除配方开销(必选的输入）
            craftData.get().getAllInputItems().forEach(i -> removeConsumeCount(i, i.getCount() * layer.getCount()));
            //二输入是可选的，所以存在二输入没有成功的可能性，故不需要从中排除
        }
        //所有可选的物品都应该呗当作不存在的。不应当参与计算
        for (CraftLayer layer : layers) {
            Optional<CraftGuideData> craftData = layer.getCraftData();
            if (craftData.isEmpty()) break;
            //添加一次必选物品，然后移除必选和可选物品，即相当于多移除一次可选物品，这样在计算剩余物品时，就会忽略可选物品
            craftData.get().getAllOutputItems().forEach(i -> addConsumeCount(i, i.getCount() * layer.getCount()));
            craftData.get().getAllInputItems().forEach(i -> addConsumeCount(i, i.getCount() * layer.getCount()));
            craftData.get().getAllInputItemsWithOptional().forEach(i -> removeConsumeCount(i, i.getCount() * layer.getCount()));
            craftData.get().getAllOutputItemsWithOptional().forEach(i -> removeConsumeCount(i, i.getCount() * layer.getCount()));
        }
    }

    private void addConsumeCount(ItemStack itemStack, int count) {
        HashSet<ItemStack> ks = new HashSet<>(itemConsumeCount.keySet());
        for (ItemStack itemStack1 : ks) {
            if (ItemStack.isSameItemSameTags(itemStack1, itemStack)) {
                int currentCount = Math.min(itemStack1.getMaxStackSize(), count);
                count -= currentCount;
                itemConsumeCount.put(itemStack1, itemConsumeCount.get(itemStack1) + currentCount);
                if (count <= 0)
                    return;
            }
        }
        while (count > 0) {
            int currentCount = Math.min(itemStack.getMaxStackSize(), count);
            itemConsumeCount.put(itemStack.copy(), currentCount);
            count -= currentCount;
        }
    }

    private void removeConsumeCount(ItemStack itemStack, int count) {
        HashSet<ItemStack> ks = new HashSet<>(itemConsumeCount.keySet());
        for (ItemStack itemStack1 : ks) {
            if (ItemStack.isSameItemSameTags(itemStack1, itemStack)) {
                int currentCount = Math.min(itemConsumeCount.get(itemStack1), count);
                count -= currentCount;
                itemConsumeCount.put(itemStack1, itemConsumeCount.get(itemStack1) - currentCount);
                if (itemConsumeCount.get(itemStack1) == 0)
                    itemConsumeCount.remove(itemStack1);
                else
                    return;
            }
        }
    }

    public int getSlotConsume() {
        return slotConsume;
    }

    public void forEachRemaining(BiConsumer<ItemStack, Integer> consumer) {
        for (ItemStack itemStack : itemConsumeCount.keySet()) {
            consumer.accept(itemStack, itemConsumeCount.get(itemStack));
        }
    }
}
