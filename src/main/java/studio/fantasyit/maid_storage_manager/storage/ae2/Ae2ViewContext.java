package studio.fantasyit.maid_storage_manager.storage.ae2;

import appeng.api.config.Actionable;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.parts.IPart;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.MEStorage;
import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.parts.reporting.AbstractTerminalPart;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.storage.Storage;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageInteractContext;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class Ae2ViewContext implements IStorageInteractContext {
    private MEStorage inv;
    int current = 0;
    private List<AEItemKey> keys;

    @Override
    public void start(EntityMaid maid, ServerLevel level, Storage target) {
        if (level.getBlockEntity(target.pos) instanceof CableBusBlockEntity cbbe) {
            Optional<Direction> first = Arrays.stream(Direction
                            .orderedByNearest(maid))
                    .filter(direction -> {
                        IPart part = cbbe.getCableBus().getPart(direction);
                        return part instanceof AbstractTerminalPart atp;
                    })
                    .findFirst();

            if (first.isEmpty()) return;

            IGridNode terminal = cbbe.getGridNode(first.get());
            if (terminal != null && terminal.getGrid() != null) {
                this.inv = terminal.getGrid().getStorageService().getInventory();
                this.keys = inv
                        .getAvailableStacks()
                        .keySet()
                        .stream()
                        .filter(key -> key instanceof AEItemKey)
                        .map(key -> (AEItemKey) key)
                        .toList();
            }
        }
    }

    @Override
    public boolean isDone() {
        return current >= keys.size();
    }

    @Override
    public void reset() {
        current = 0;
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