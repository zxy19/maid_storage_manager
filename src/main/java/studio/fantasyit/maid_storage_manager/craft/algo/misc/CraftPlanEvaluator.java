package studio.fantasyit.maid_storage_manager.craft.algo.misc;


import studio.fantasyit.maid_storage_manager.craft.algo.base.AbstractBiCraftGraph;
import studio.fantasyit.maid_storage_manager.craft.algo.base.CraftResultNode;
import studio.fantasyit.maid_storage_manager.craft.algo.base.node.CraftNodeBasic;
import studio.fantasyit.maid_storage_manager.craft.algo.base.node.ItemNodeBasic;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public enum CraftPlanEvaluator {
    NONE,
    STEPS {
        @Override
        public int getScore(Map<Integer, Integer> changeMap, List<CraftResultNode> results, AbstractBiCraftGraph graph) {
            return results.size();
        }
    },
    CRAFT_GUIDES {
        @Override
        public int getScore(Map<Integer, Integer> changeMap, List<CraftResultNode> results, AbstractBiCraftGraph graph) {
            return results
                    .stream()
                    .map(t -> graph.getNode(t.index))
                    .filter(t -> t instanceof CraftNodeBasic)
                    .map(t -> t.id)
                    .collect(Collectors.toSet())
                    .size();
        }
    },
    ITEMS {
        @Override
        public int getScore(Map<Integer, Integer> changeMap, List<CraftResultNode> results, AbstractBiCraftGraph graph) {
            return results
                    .stream()
                    .map(t -> graph.getNode(t.index))
                    .filter(t -> t instanceof ItemNodeBasic)
                    .map(t -> t.id)
                    .collect(Collectors.toSet())
                    .size();
        }
    },
    ITEMS_COSTED {
        @Override
        public int getScore(Map<Integer, Integer> changeMap, List<CraftResultNode> results, AbstractBiCraftGraph graph) {
            return results
                    .stream()
                    .map(t -> graph.getNode(t.index))
                    .filter(t -> t instanceof ItemNodeBasic)
                    .map(t -> t.id)
                    .filter(t -> changeMap.get(t) < 0)
                    .collect(Collectors.toSet())
                    .size();
        }
    };

    public int getScore(Map<Integer, Integer> changeMap, List<CraftResultNode> results, AbstractBiCraftGraph graph) {
        return 0;
    }
}
