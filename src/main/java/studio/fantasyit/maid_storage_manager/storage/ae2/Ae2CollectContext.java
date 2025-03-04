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
import studio.fantasyit.maid_storage_manager.storage.base.IStorageExtractableContext;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class Ae2CollectContext implements IStorageExtractableContext {
    private MEStorage inv;
    private int current = 0;
    private boolean done = false;

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
            }
        }
    }
    @Override
    public void extract(List<ItemStack> itemList, boolean matchNbt, Function<ItemStack, ItemStack> process) {
        if (inv == null) return;
        for (; current < itemList.size(); current++) {
            ItemStack item = itemList.get(current);
            if (item.isEmpty()) continue;
            AEItemKey key = AEItemKey.of(item);
            if (!matchNbt) {
                key = AEItemKey.of(item.getItem());
            }
            if (key == null) continue;

            long extract = inv.extract(key, item.getCount(), Actionable.SIMULATE, IActionSource.empty());
            if (extract == 0) continue;
            while (extract > 0) {
                ItemStack tmp = key
                        .getReadOnlyStack()
                        .copyWithCount((int) Math.min(extract, item.getMaxStackSize()));
                ItemStack apply = process.apply(tmp);
                if (!apply.isEmpty()) {
                    inv.extract(key, apply.getCount(), Actionable.MODULATE, IActionSource.empty());
                    extract -= apply.getCount();
                } else break;
            }
            break;
        }
        if (current >= itemList.size()) {
            done = true;
        }
    }

    @Override
    public boolean isDone() {
        return done;
    }
}
