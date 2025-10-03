package studio.fantasyit.maid_storage_manager.craft.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.Logger;
import studio.fantasyit.maid_storage_manager.craft.work.CraftLayer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class InvConsumeSimulator {
    public static Codec<InvConsumeSimulator> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                            RecordCodecBuilder.create((RecordCodecBuilder.Instance<Pair<ItemStack, Integer>> ii) ->
                                    ii.group(
                                            ItemStack.CODEC.fieldOf("itemStack").forGetter(Pair::getA),
                                            Codec.INT.fieldOf("count").forGetter(Pair::getB)
                                    ).apply(ii, Pair::new)
                            ).listOf().fieldOf("data").forGetter(InvConsumeSimulator::getToSave)
                    ).
                    apply(instance, InvConsumeSimulator::new)
    );

    private List<Pair<ItemStack, Integer>> getToSave() {
        return itemConsumeCount.entrySet().stream().map(e -> new Pair<>(e.getKey(), e.getValue())).toList();
    }

    public boolean enableLog = false;
    Map<ItemStack, Integer> itemConsumeCount;
    Map<ItemStack, Integer> snapshot;

    public InvConsumeSimulator(List<Pair<ItemStack, Integer>> mapBuilder) {
        this.itemConsumeCount = new HashMap<>();
        mapBuilder.forEach(pair -> itemConsumeCount.put(pair.getA(), pair.getB()));
        snapshot = null;
    }

    public InvConsumeSimulator(Map<ItemStack, Integer> itemConsumeCount) {
        this.itemConsumeCount = new HashMap<>(itemConsumeCount);
        snapshot = null;
    }

    public InvConsumeSimulator() {
        this.itemConsumeCount = new HashMap<>();
    }

    public void addConsumeCount(ItemStack itemStack, int count) {
        if (enableLog && snapshot == null) {
            Logger.debug("[II]addConsumeCount %s %d", itemStack.getItem(), count);
        }
        HashSet<ItemStack> ks = new HashSet<>(itemConsumeCount.keySet());
        for (ItemStack itemStack1 : ks) {
            if (ItemStack.isSameItemSameComponents(itemStack1, itemStack)) {
                int currentCount = Math.min(itemStack1.getMaxStackSize() - itemConsumeCount.get(itemStack1), count);
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

    public void removeConsumeCount(ItemStack itemStack, int count) {
        if (enableLog && snapshot == null) {
            Logger.debug("[II]removeConsumeCount %s %d", itemStack.getItem(), count);
        }
        HashSet<ItemStack> ks = new HashSet<>(itemConsumeCount.keySet());
        for (ItemStack itemStack1 : ks) {
            if (ItemStack.isSameItemSameComponents(itemStack1, itemStack)) {
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

    public int getCurrentSlotConsume() {
        return itemConsumeCount.size();
    }

    public void clear() {
        itemConsumeCount.clear();
    }

    public void addLayer(CraftLayer layer) {
        layer.getItems().forEach(itemStack -> addConsumeCount(itemStack, itemStack.getCount()));
        layer.getCraftData()
                .ifPresent(craftData ->
                        craftData
                                .getAllOutputItemsWithOptional()
                                .forEach(
                                        itemStack ->
                                                addConsumeCount(itemStack, itemStack.getCount() * layer.getCount())
                                )
                );
    }

    public void addLayerOutput(CraftLayer layer) {
        layer.getCraftData()
                .ifPresent(craftData ->
                        craftData
                                .getOutput()
                                .forEach(
                                        itemStack ->
                                                addConsumeCount(itemStack, itemStack.getCount() * layer.getCount())
                                )
                );
    }

    public void removeLayerOutput(CraftLayer layer) {
        layer.getCraftData()
                .ifPresent(craftData ->
                        craftData
                                .getAllOutputItemsWithOptional()
                                .forEach(
                                        itemStack ->
                                                removeConsumeCount(itemStack, itemStack.getCount() * layer.getCount())
                                )
                );
    }

    public void removeLayer(CraftLayer layer) {
        layer.getItems().forEach(itemStack -> removeConsumeCount(itemStack, itemStack.getCount()));
        layer.getCraftData()
                .ifPresent(craftData ->
                        craftData
                                .getAllOutputItemsWithOptional()
                                .forEach(
                                        itemStack ->
                                                removeConsumeCount(itemStack, itemStack.getCount() * layer.getCount())
                                )
                );
    }

    public void removeLayerInput(CraftLayer layer) {
        layer.getItems().forEach(itemStack -> removeConsumeCount(itemStack, itemStack.getCount()));
    }

    public List<Pair<ItemStack, Integer>> getRemain() {
        return itemConsumeCount
                .entrySet()
                .stream()
                .map(e -> new Pair<>(e.getKey(), e.getValue()))
                .toList();
    }

    public void snapshot() {
        snapshot = new HashMap<>(itemConsumeCount);
    }

    public void restoreSnapshot() {
        if (snapshot == null)
            throw new IllegalStateException("snapshot is null");
        itemConsumeCount = snapshot;
        snapshot = null;
    }
}