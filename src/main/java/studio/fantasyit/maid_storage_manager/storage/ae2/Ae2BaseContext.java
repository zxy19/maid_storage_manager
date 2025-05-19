package studio.fantasyit.maid_storage_manager.storage.ae2;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.parts.IPart;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.parts.reporting.AbstractTerminalPart;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import studio.fantasyit.maid_storage_manager.craft.context.special.AeCraftingAction;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.type.AE2Type;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageContext;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageCraftDataProvider;

import java.util.*;

public abstract class Ae2BaseContext implements IStorageCraftDataProvider, IStorageContext {
    protected MEStorage inv;
    protected IGrid grid;
    protected Target target;
    protected IPart part;
    boolean isDone = false;

    public boolean init(EntityMaid maid, ServerLevel level, Target target) {
        if (level.getBlockEntity(target.pos) instanceof CableBusBlockEntity cbbe) {
            Optional<Direction> first = Arrays.stream(Direction
                            .orderedByNearest(maid))
                    .filter(direction -> {
                        IPart part = cbbe.getCableBus().getPart(direction);
                        return part instanceof AbstractTerminalPart atp;
                    })
                    .findFirst();

            if (first.isEmpty()) return false;

            part = cbbe.getCableBus().getPart(first.get());
            IGridNode terminal = cbbe.getGridNode(first.get());
            if (terminal != null) {
                grid = terminal.getGrid();
                if (grid != null) {
                    this.inv = grid.getStorageService().getInventory();
                    this.target = target;
                    return inv != null;
                }
            }
        }
        return false;
    }

    public ICraftingService getCraftingService() {
        return grid.getCraftingService();
    }

    public IGrid getGrid() {
        return grid;
    }

    public IPart getPart() {
        return part;
    }


    @Override
    public List<CraftGuideData> getCraftGuideData() {
        ArrayList<CraftGuideData> list = new ArrayList<>();
        inv
                .getAvailableStacks()
                .keySet()
                .stream()
                .filter(key -> key instanceof AEItemKey aek && aek.getReadOnlyStack().is(ItemRegistry.CRAFT_GUIDE.get()))
                .map(key -> CraftGuideData.fromItemStack(((AEItemKey) key).getReadOnlyStack()))
                .filter(CraftGuideData::available)
                .forEach(list::add);

        ICraftingService crafting = this.getCraftingService();
        Set<AEKey> craftables = crafting.getCraftables(AEItemKey.filter());
        craftables.stream()
                .filter(aeKey -> aeKey instanceof AEItemKey)
                .map(key -> {
                    List<CraftGuideStepData> steps = List.of(new CraftGuideStepData(
                            target,
                            List.of(),
                            List.of(((AEItemKey) key).getReadOnlyStack().copyWithCount(1)),
                            AeCraftingAction.TYPE,
                            false,
                            false,
                            new CompoundTag()
                    ));
                    return new CraftGuideData(
                            steps,
                            AE2Type.TYPE
                    );
                })
                .forEach(list::add);
        setDone(true);
        return list;
    }
    @Override
    public boolean isDone() {
        return isDone;
    }
    public void setDone(boolean done){
        isDone = done;
    }
}
