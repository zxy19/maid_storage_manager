package studio.fantasyit.maid_storage_manager.craft.algo.misc;

import net.minecraft.world.item.ItemStack;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.util.MathUtil;

import java.util.ArrayList;
import java.util.List;

public class ItemListStepSum {
    public List<Integer> futureSteps;
    int totalSteps;

    public ItemListStepSum(List<Pair<ItemStack, Integer>> items) {
        futureSteps = new ArrayList<>();
        int tmpStep = 0;
        for (int i = 0; i < items.size(); i++) {
            futureSteps.add(0);
        }
        for (int i = items.size() - 1; i >= 0; i--) {
            futureSteps.set(i, tmpStep);
            tmpStep += MathUtil.biMaxStepCalc(items.get(i).getB());
        }
        totalSteps = tmpStep;
    }

    public int getStep(int index) {
        if (index >= futureSteps.size()) return futureSteps.get(futureSteps.size() - 1);
        if (index < 0) return futureSteps.get(0);
        return futureSteps.get(index);
    }

    public int getTotalStep() {
        return totalSteps;
    }
}
