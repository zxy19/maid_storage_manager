package studio.fantasyit.maid_storage_manager.menu.container;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.DataSlot;
import org.apache.commons.lang3.mutable.MutableInt;

public class CountSlot extends DataSlot {

    private final MutableInt count;
    private final Container container;

    public CountSlot(MutableInt count, Container container) {
        super();
        this.count = count;
        this.container = container;
    }

    @Override
    public int get() {
        return count.getValue();
    }

    @Override
    public void set(int p_39402_) {
        count.setValue(p_39402_);
        container.setChanged();
    }
}
