package studio.fantasyit.maid_storage_manager.craft.algo.base.node;

import oshi.util.tuples.Pair;

import java.util.List;

public class SimCraftNode extends CraftNodeBasic {
    public String data;
    public List<Pair<Integer, Integer>> toId;
    public List<Pair<Integer, Integer>> fromId;

    public SimCraftNode(int id, boolean related, String data, List<Pair<Integer, Integer>> toId, List<Pair<Integer, Integer>> fromId) {
        super(id, related);
        this.data = data;
        this.toId = toId;
        this.fromId = fromId;
    }

    @Override
    public String toString() {
        return String.format("SimCraftNode#%d[%s]", id, data);
    }
}
