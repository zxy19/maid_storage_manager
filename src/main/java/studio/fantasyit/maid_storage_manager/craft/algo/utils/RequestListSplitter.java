package studio.fantasyit.maid_storage_manager.craft.algo.utils;

import studio.fantasyit.maid_storage_manager.craft.work.CraftLayer;

import java.util.ArrayList;
import java.util.List;

public class RequestListSplitter {

    public static List<CraftLayer> splitLayerMax(List<CraftLayer> layers, int maxSize) {
        List<CraftLayer> result = new ArrayList<>();

        for (CraftLayer layer : layers) {
            if (layer.getCraftData().isEmpty())
                result.add(layer);
            else {
                int oCount = layer.getCount();
                boolean placeBefore = layer.shouldPlaceBefore();
                if (layer.getItems().stream().anyMatch(t -> t.getCount() < layer.getCount())) {
                    //不可分割的层（涉及循环物品）
                    result.add(layer);
                    continue;
                }
                for (int rest = oCount; rest > 0; rest -= maxSize) {
                    int currentSize = Math.min(rest, maxSize);
                    CraftLayer newLayer = layer.copyWithNoState();
                    newLayer.setCount(currentSize);
                    newLayer.setItems(
                            newLayer.getItems()
                                    .stream()
                                    .map(itemStack -> itemStack.copyWithCount(itemStack.getCount() * currentSize / oCount))
                                    .toList()
                    );
                    if (placeBefore) {
                        newLayer.setPlaceBefore();
                        placeBefore = false;
                    }
                    result.add(newLayer);
                }
            }
        }
        return result;
    }
}
