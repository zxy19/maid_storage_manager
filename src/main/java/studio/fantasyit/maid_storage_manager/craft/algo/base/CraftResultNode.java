package studio.fantasyit.maid_storage_manager.craft.algo.base;

public class CraftResultNode {
    public int index;
    public int count;
    public boolean related;

    public CraftResultNode(int index, int count, boolean related) {
        this.index = index;
        this.count = count;
        this.related = related;
    }
}