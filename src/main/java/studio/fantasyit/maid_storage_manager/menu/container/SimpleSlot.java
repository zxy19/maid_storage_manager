package studio.fantasyit.maid_storage_manager.menu.container;


import net.minecraft.world.inventory.DataSlot;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class SimpleSlot extends DataSlot {
    Consumer<Integer> set;
    Supplier<Integer> get;

    public SimpleSlot(Consumer<Integer> set, Supplier<Integer> get) {
        this.set = set;
        this.get = get;
    }

    @Override
    public int get() {
        return get.get();
    }

    @Override
    public void set(int p_39402_) {
        set.accept(p_39402_);
    }
}