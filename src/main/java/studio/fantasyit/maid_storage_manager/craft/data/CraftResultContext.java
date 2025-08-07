package studio.fantasyit.maid_storage_manager.craft.data;

import net.minecraft.world.item.ItemStack;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.craft.work.CraftLayer;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public class CraftResultContext {
    List<CraftLayer> layers;
    InvConsumeSimulator consumer;
    List<Pair<ItemStack, Integer>> itemRemain;
    int slotConsume;

    public CraftResultContext(List<CraftLayer> layers) {
        consumer = new InvConsumeSimulator();
        this.layers = layers;
        calculate(Integer.MAX_VALUE);
        this.itemRemain = consumer.getRemain();
    }

    protected void calculate(int maxSlotConsume) {
        consumer.clear();
        this.slotConsume = 0;

        boolean isFirst = true;
        for (int _i = 0; _i < layers.size(); _i++) {
            CraftLayer layer = layers.get(_i);
            //女仆优先使用合成树上找到的剩余物品
            layer.getItems().forEach(i -> consumer.removeConsumeCount(i, i.getCount()));
            //如果是最后一层，那么移除终产物即可
            Optional<CraftGuideData> craftData = layer.getCraftData();
            if (craftData.isEmpty()) break;
            //否则，加入该层物品需要占用的背包空间
            layer.getItems().forEach(i -> consumer.addConsumeCount(i, i.getCount()));
            craftData.get().getAllOutputItemsWithOptional().forEach(i -> consumer.addConsumeCount(i, i.getCount() * layer.getCount()));
            //记录最大背包占用
            int currentMaxConsume = Math.max(this.slotConsume, consumer.getCurrentSlotConsume());
            //移除配方开销(必选的输入）
            craftData.get().getAllInputItems().forEach(i -> consumer.removeConsumeCount(i, i.getCount() * layer.getCount()));
            //二输入是可选的，所以存在二输入没有成功的可能性，故不需要从中排除

            //如果发现目前已经用超背包了，而且不是放置后的第一层，那么可以尝试放一次东西再合成。
            if (maxSlotConsume < currentMaxConsume && !isFirst) {
                //TODO:验证可行性
//                layer.setPlaceBefore();
                consumer.clear();
                --_i;
                isFirst = true;
            } else {
                //否则，记录最大的，然后继续执行
                isFirst = false;
                this.slotConsume = currentMaxConsume;
            }
        }
        //所有可选的物品都应该呗当作不存在的。不应当参与计算
        for (CraftLayer layer : layers) {
            Optional<CraftGuideData> craftData = layer.getCraftData();
            if (craftData.isEmpty()) break;
            //添加一次必选物品，然后移除必选和可选物品，即相当于多移除一次可选物品，这样在计算剩余物品时，就会忽略可选物品
            craftData.get().getAllOutputItems().forEach(i -> consumer.addConsumeCount(i, i.getCount() * layer.getCount()));
            craftData.get().getAllInputItems().forEach(i -> consumer.addConsumeCount(i, i.getCount() * layer.getCount()));
            craftData.get().getAllInputItemsWithOptional().forEach(i -> consumer.removeConsumeCount(i, i.getCount() * layer.getCount()));
            craftData.get().getAllOutputItemsWithOptional().forEach(i -> consumer.removeConsumeCount(i, i.getCount() * layer.getCount()));
        }
    }

    public int getSlotConsume() {
        return slotConsume;
    }

    public void forEachRemaining(BiConsumer<ItemStack, Integer> consumer) {
        itemRemain.forEach(pair -> consumer.accept(pair.getA(), pair.getB()));
    }

    public void splitTaskWith(int maxSlotConsume) {
        calculate(maxSlotConsume);
    }
}
