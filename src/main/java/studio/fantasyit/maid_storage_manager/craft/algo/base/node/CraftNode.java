package studio.fantasyit.maid_storage_manager.craft.algo.base.node;

import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;

import java.util.ArrayList;
import java.util.List;

public class CraftNode extends CraftNodeBasic {
    public final CraftGuideData craftGuideData;
    public final List<CraftGuideData> sameData;

    public CraftNode(int id, boolean related, CraftGuideData craftGuideData) {
        super(id, related);
        this.craftGuideData = craftGuideData;
        this.sameData = new ArrayList<>(List.of(craftGuideData));
    }

    public void addSame(CraftGuideData data) {
        this.sameData.add(data);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CraftNode#").append(id).append("[");
        List<ItemStack> input = craftGuideData.getInput();
        for (int i = 0; i < input.size(); i++) {
            sb.append(input.get(i).getItem());
            if (i >= 2) {
                sb.append(",...");
                break;
            } else {
                sb.append(", ");
            }
        }
        sb.append(" -> ");
        List<ItemStack> output = craftGuideData.getOutput();
        for (int i = 0; i < output.size(); i++) {
            sb.append(output.get(i).getItem());
            if (i >= 2) {
                sb.append(",...");
                break;
            } else {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
