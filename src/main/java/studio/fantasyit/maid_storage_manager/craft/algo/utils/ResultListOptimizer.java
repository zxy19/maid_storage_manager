package studio.fantasyit.maid_storage_manager.craft.algo.utils;

import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.work.CraftLayer;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;

public class ResultListOptimizer {
    public static List<CraftLayer> optimize(List<CraftLayer> layers) {
        layers = mergeSame(layers);
        return layers;
    }

    public static List<CraftLayer> mergeSame(List<CraftLayer> layers) {
        if (layers.isEmpty()) return layers;
        List<CraftLayer> result = new ArrayList<>();
        List<ItemStack> tItemStack;
        CraftLayer tmp = layers.get(0);
        tItemStack = new ArrayList<>(tmp.getItems().stream().map(ItemStack::copy).toList());
        for (int i = 1; i < layers.size(); i++) {
            CraftLayer layer = layers.get(i);
            if (layer.getCraftData().isEmpty()) {
                result.add(tmp);
                result.add(layer);
                i++;
                if (i < layers.size()) {
                    tmp = layers.get(i);
                    tItemStack = new ArrayList<>(tmp.getItems().stream().map(ItemStack::copy).toList());
                } else tmp = null;
                continue;
            }

            CraftGuideData cgd = layer.getCraftData().get();
            CraftGuideData lastCgd = tmp.getCraftData().get();

            boolean equal = true;
            if (cgd.getType().equals(lastCgd.getType()) && cgd.steps.size() == lastCgd.steps.size()) {
                for (int j = 0; j < cgd.steps.size(); j++) {
                    CraftGuideStepData step1 = cgd.steps.get(j);
                    CraftGuideStepData step2 = lastCgd.steps.get(j);
                    if (!step1.equals(step2)) {
                        equal = false;
                        break;
                    }
                }
            } else equal = false;

            if (layer.shouldPlaceBefore())
                equal = false;

            if (equal) {
                List<ItemStack> nextInputs = new ArrayList<>(layer.getItems());
                List<ItemStack> nextOutputs = new ArrayList<>(cgd
                        .getOutput()
                        .stream()
                        .map(ii -> ii.copyWithCount(ii.getCount() * layer.getCount()))
                        .toList());
                CraftLayer finalTmp = tmp;
                List<ItemStack> oldOutputs = new ArrayList<>(lastCgd
                        .getOutput()
                        .stream()
                        .map(ii -> ii.copyWithCount(ii.getCount() * finalTmp.getCount()))
                        .toList());
                List<ItemStack> oldOutputsToDetect = new ArrayList<>(lastCgd
                        .getOutput()
                        .stream()
                        .map(ii -> ii.copyWithCount(ii.getCount() * finalTmp.getCount()))
                        .toList());
                //判断每个组是否有溢出
                boolean exceed = false;
                //对于下一个层的输入，如果满足上一层的输出，那么将其从输入中移除；否则，加入新的输入列表
                for (ItemStack itemStack : nextInputs) {
                    ItemStack itemStack1 = ItemStackUtil.removeIsMatchInList(oldOutputsToDetect, itemStack.copy(), ItemStackUtil::isSameInCrafting);
                    if (itemStack1.isEmpty()) continue;
                    if (itemStack1.getMaxStackSize() == 1) {
                        exceed = true;
                    } else {
                        ItemStackUtil.addToList(tItemStack, itemStack1, ItemStackUtil::isSameInCrafting);
                    }
                }
                //对于下一层的输出，判断其和上一层合并是否会导致溢出
                for (ItemStack itemStack1 : nextOutputs) {
                    if (itemStack1.getMaxStackSize() == 1) {
                        exceed = true;
                    }
                    ItemStackUtil.addToList(oldOutputsToDetect, itemStack1, ItemStackUtil::isSameInCrafting);
                }
                if (tItemStack.stream().anyMatch(itemStack1 -> itemStack1.getCount() > itemStack1.getMaxStackSize()))
                    exceed = true;
                if (oldOutputsToDetect.stream().anyMatch(itemStack1 -> itemStack1.getCount() > itemStack1.getMaxStackSize()))
                    exceed = true;

                if (!exceed) {
                    //下一步的输入如果是上一步的输出，将其删去（内部循环物品无需处理）否则加入
                    for (ItemStack itemStack : nextInputs) {
                        ItemStack itemStack1 = ItemStackUtil.removeIsMatchInList(oldOutputs, itemStack, ItemStackUtil::isSameInCrafting);
                        if (!itemStack1.isEmpty())
                            ItemStackUtil.addToList(tmp.getItems(), itemStack1, ItemStackUtil::isSameInCrafting);
                    }
                    tmp.setCount(tmp.getCount() + layer.getCount());
                    continue;
                }
            }

            result.add(tmp);
            tmp = layer;
            tItemStack = new ArrayList<>(tmp.getItems().stream().map(ItemStack::copy).toList());
        }
        if (tmp != null)
            result.add(tmp);
        return result;
    }
}
