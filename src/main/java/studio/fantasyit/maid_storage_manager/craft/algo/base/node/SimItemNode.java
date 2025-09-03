package studio.fantasyit.maid_storage_manager.craft.algo.base.node;

public class SimItemNode extends ItemNodeBasic {
    public String data;

    public SimItemNode(int id, boolean related, String data) {
        super(id, related);
        this.data = data;
    }

    @Override
    public String toString() {
        return "SimItemNode#" + id + "[" + data + "]";
    }
}
