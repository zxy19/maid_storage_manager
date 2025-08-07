package studio.fantasyit.maid_storage_manager.craft.algo.utils;

import studio.fantasyit.maid_storage_manager.craft.work.CraftLayer;

import java.util.ArrayList;
import java.util.List;

public class ResultListUtils {
    public static void unsetPlaceBefore(List<CraftLayer> layers) {
        for (CraftLayer layer : layers) {
            layer.clearPlaceBefore();
        }
    }

    public static List<CraftLayer> splitIntoSingleStep(List<CraftLayer> layers) {
        List<CraftLayer> result = new ArrayList<>();

        for (CraftLayer layer : layers) {
            if (layer.getCraftData().isEmpty())
                result.add(layer);
            else {
                int oCount = layer.getCount();
                boolean placeBefore = layer.shouldPlaceBefore();
                for (int i = 0; i < oCount; i++) {
                    CraftLayer newLayer = layer.copyWithNoState();
                    newLayer.setCount(1);
                    newLayer.setItems(
                            newLayer.getItems()
                                    .stream()
                                    .map(itemStack -> itemStack.copyWithCount(itemStack.getCount() / oCount))
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
