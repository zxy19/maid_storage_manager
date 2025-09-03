package studio.fantasyit.maid_storage_manager.craft.algo.graph;

import net.minecraft.world.item.ItemStack;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.Logger;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.util.ThreadingUtil;

import java.util.List;
import java.util.concurrent.Future;

public class ThreadedSearchGraph extends SimpleSearchGraph {
    public Future<?> running = null;

    public ThreadedSearchGraph(List<Pair<ItemStack, Integer>> items, List<CraftGuideData> craftGuides) {
        super(items, craftGuides);
    }

    @Override
    public boolean buildGraph() {
        return true;
    }

    @Override
    public void buildGraphInstantly() {
        while (!super.buildGraph()) ;
    }

    @Override
    public boolean processQueues() {
        if (running != null)
            return running.isDone();
        running = ThreadingUtil.run(this::_process);
        return false;
    }

    public void _process() {
        try {
            while (!super.buildGraph()) ;
            while (!super.processQueues()) ;
        } catch (Exception e) {
            Logger.logger.error("In search recipe", e);
        }
    }

    @Override
    public void restoreCurrent() {
        super.restoreCurrent();
        if (running != null && running.isDone()) {
            running = null;
        }
    }

    @Override
    public void startContext(ItemStack item, int count) {
        super.startContext(item, count);
        if (running != null && running.isDone()) {
            running = null;
        }
    }
}
