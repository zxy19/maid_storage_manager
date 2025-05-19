package studio.fantasyit.maid_storage_manager.storage.ae2;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageInteractContext;

import java.util.List;
import java.util.function.Function;

public class Ae2ViewContext extends Ae2BaseContext implements IStorageInteractContext {
    int current = 0;
    private List<AEItemKey> keys;

    @Override
    public void start(EntityMaid maid, ServerLevel level, Target target) {
        if (this.init(maid, level, target))
            this.keys = inv
                    .getAvailableStacks()
                    .keySet()
                    .stream()
                    .filter(key -> key instanceof AEItemKey)
                    .map(key -> (AEItemKey) key)
                    .toList();

    }

    @Override
    public boolean isDone() {
        return keys == null || current >= keys.size() || super.isDone();
    }

    @Override
    public void reset() {
        current = 0;
        setDone(false);
    }

    @Override
    public void tick(Function<ItemStack, ItemStack> process) {
        int count = 0;
        for (; current < keys.size(); current++) {
            if (count++ > 32) return;
            AEItemKey key = keys.get(current);
            long extract = inv.extract(key, Long.MAX_VALUE, Actionable.SIMULATE, IActionSource.empty());
            if (extract > Integer.MAX_VALUE)
                extract = Integer.MAX_VALUE;
            process.apply(key.toStack((int) extract));
        }
    }
}