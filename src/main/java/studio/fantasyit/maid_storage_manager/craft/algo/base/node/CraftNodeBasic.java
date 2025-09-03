package studio.fantasyit.maid_storage_manager.craft.algo.base.node;


public class CraftNodeBasic extends Node {
    public int scheduled;
    public boolean hasLoopIngredient;

    public CraftNodeBasic(int id, boolean related) {
        super(id, related);
        this.scheduled = 0;
        this.hasLoopIngredient = false;
    }

    public void addScheduled(int count) {
        this.scheduled += count;
    }

}
