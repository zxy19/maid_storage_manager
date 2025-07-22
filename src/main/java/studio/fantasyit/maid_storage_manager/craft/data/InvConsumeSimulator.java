package studio.fantasyit.maid_storage_manager.craft.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.craft.work.CraftLayer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class InvConsumeSimulator {
    public static Codec<InvConsumeSimulator> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.unboundedMap(ItemStack.CODEC, Codec.INT).fieldOf("itemConsumeCount").forGetter(t -> t.itemConsumeCount)
            ).apply(instance, InvConsumeSimulator::new)
    );

    Map<ItemStack, Integer> itemConsumeCount;

    public InvConsumeSimulator(Map<ItemStack, Integer> itemConsumeCount) {
        this.itemConsumeCount = new HashMap<>(itemConsumeCount);
    }

    public InvConsumeSimulator() {
        this.itemConsumeCount = new HashMap<>();
    }

    public void addConsumeCount(ItemStack itemStack, int count) {
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

    public void removeConsumeCount(ItemStack itemStack, int count) {
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

    public void removeLayerAfterFinish(CraftLayer layer) {
        layer.getCraftData()
                .ifPresent(craftData -> {
                            craftData
                                    .getAllOutputItems()
                                    .forEach(
                                            itemStack ->
                                                    removeConsumeCount(itemStack, itemStack.getCount() * layer.getDoneCount())
                                    );
                            craftData
                                    .getAllInputItems()
                                    .forEach(
                                            itemStack ->
                                                    addConsumeCount(itemStack, itemStack.getCount() * layer.getDoneCount())
                                    );
                        }
                );
    }

    public void removeLayerInputAll(CraftLayer layer) {
        layer.getCraftData()
                .ifPresent(craftData ->
                        craftData
                                .getAllOutputItemsWithOptional()
                                .forEach(
                                        itemStack ->
                                                removeConsumeCount(itemStack, itemStack.getCount() * layer.getDoneCount())
                                )
                );
    }
}