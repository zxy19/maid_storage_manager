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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageInsertableContext;

import java.util.Arrays;
import java.util.Optional;

public class Ae2PlacingContext implements IStorageInsertableContext {
    private MEStorage inv;

    @Override
    public void start(EntityMaid maid, ServerLevel level, BlockPos target) {
        if (level.getBlockEntity(target) instanceof CableBusBlockEntity cbbe) {
            Optional<Direction> first = Arrays.stream(Direction
                            .orderedByNearest(maid))
                    .filter(direction -> {
                        IPart part = cbbe.getCableBus().getPart(direction);
                        if (part instanceof AbstractTerminalPart atp) {
                            return true;
                        }
                        return false;
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
    public ItemStack insert(ItemStack item) {
        if (inv == null) return item;
        AEItemKey key = AEItemKey.of(item);
        if (key == null) return item;
        long insert = this.inv.insert(key, item.getCount(), Actionable.MODULATE, IActionSource.empty());
        ItemStack result = item.copy();
        result.shrink((int) insert);
        return result;
    }
}
