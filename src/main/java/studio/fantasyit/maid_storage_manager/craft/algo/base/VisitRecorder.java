package studio.fantasyit.maid_storage_manager.craft.algo.base;

/**
 * 记录每个节点在当前子段的上次访问次数，用于阻断正权环
 */
public class VisitRecorder {
    public int[] record;

    public VisitRecorder(int size) {
        record = new int[size];
        for (int i = 0; i < size; i++) {
            record[i] = Integer.MAX_VALUE;
        }
    }


    public void minStepRequire(AbstractBiCraftGraph.Node index, int value) {
        minStepRequire(index.id, value);
    }

    public int minStepRequire(AbstractBiCraftGraph.Node index) {
        return minStepRequire(index.id);
    }

    public void minStepRequire(int index, int value) {
        record[index] = value;
    }

    public int minStepRequire(int index) {
        return record[index];
    }
}
