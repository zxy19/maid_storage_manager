package studio.fantasyit.maid_storage_manager.maid.behavior.request.terminal;

import appeng.api.config.Actionable;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.parts.IPart;
import appeng.api.stacks.*;
import appeng.api.storage.MEStorage;
import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.parts.reporting.AbstractTerminalPart;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.items.wrapper.RangedWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.registry.MemoryModuleRegistry;
import studio.fantasyit.maid_storage_manager.util.Conditions;
import studio.fantasyit.maid_storage_manager.util.InvUtil;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

import java.util.*;

public class PickItemFromMeBehavior extends Behavior<EntityMaid> {
    @Nullable
    private MEStorage inv;
    private KeyCounter keys;
    private int duration;

    public PickItemFromMeBehavior() {
        super(Map.of(), 100000000);
    }

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel level, @NotNull EntityMaid maid) {
        if (!ModList.get().isLoaded("ae2")) return false;
        if (!Config.enableAe2Sup) return false;
        if (!MemoryUtil.isWorkingRequest(maid)) return false;

        if (!Conditions.hasCurrentTerminalPos(maid)) return false;
        if (!Conditions.takingRequestList(maid)) return false;
        if (!Conditions.inventoryNotFull(maid)) return false;
        if (!Conditions.listNotDone(maid)) return false;
        if (Conditions.alreadyArriveTarget(maid)) return true;
        return Conditions.hasReachedValidTargetOrReset(maid);
    }

    @Override
    protected boolean canStillUse(ServerLevel p_22545_, EntityMaid p_22546_, long p_22547_) {
        return duration < 20;
    }

    @Override
    protected void start(@NotNull ServerLevel level, @NotNull EntityMaid maid, long gameTimeIn) {
        BlockPos target = MemoryUtil.getCurrentTerminalPos(maid);
        if (target == null)
            return;
        MemoryUtil.arriveTarget(maid);
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
        this.tryTake(maid);
        this.duration = 0;
    }

    @Override
    protected void stop(ServerLevel level, EntityMaid maid, long p_22550_) {
        super.stop(level, maid, p_22550_);
        BlockPos target = MemoryUtil.getCurrentTerminalPos(maid);
        if (target != null) {
            Set<BlockPos> visitedPos = MemoryUtil.getVisitedPos(maid.getBrain());
            visitedPos.add(target);
            maid.getBrain().setMemory(MemoryModuleRegistry.MAID_VISITED_POS.get(), visitedPos);
        }
        MemoryUtil.clearArriveTarget(maid);
        MemoryUtil.clearPosition(maid);
        MemoryUtil.clearCurrentTerminalPos(maid);
    }

    @Override
    protected void tick(ServerLevel p_22551_, EntityMaid p_22552_, long p_22553_) {
        super.tick(p_22551_, p_22552_, p_22553_);
        this.duration++;
    }

    @SuppressWarnings("UnusedAssignment")
    protected void tryTake(EntityMaid maid) {
        RangedWrapper availableBackpackInv = maid.getAvailableBackpackInv();
        ItemStack listItem = maid.getMainHandItem();
        List<Pair<ItemStack, Integer>> items = RequestListItem.getItemStacksNotDone(listItem, true);
        for (Pair<ItemStack, Integer> item : items) {
            int count = 0;
            int restNeed = item.getB();
            if (restNeed == -1) restNeed = Integer.MAX_VALUE;
            int maxStore = InvUtil.maxCanPlace(availableBackpackInv, item.getA());
            List<AEItemKey> keys = List.of(Objects.requireNonNull(AEItemKey.of(item.getA())));
            if (!listItem.getOrCreateTag().getBoolean(RequestListItem.TAG_MATCH_TAG) && inv != null) {
                keys = new ArrayList<>();
                KeyCounter availableStacks = inv.getAvailableStacks();
                for (AEKey availableStack : availableStacks.keySet()) {
                    if (availableStack instanceof AEItemKey aeItemKey) {
                        if (aeItemKey.getItem() == item.getA().getItem()) {
                            keys.add(aeItemKey);
                        }
                    }
                }
            }

            if (maxStore > 0) {
                for (AEItemKey key : keys) {
                    long extract = inv.extract(key, restNeed, Actionable.SIMULATE, IActionSource.empty());
                    while (extract > 0) {
                        ItemStack tmp = key
                                .getReadOnlyStack()
                                .copyWithCount((int) Math.min(extract, item.getA().getMaxStackSize()));
                        long extract1 = inv
                                .extract(key, tmp.getCount(), Actionable.MODULATE, IActionSource.empty());
                        if (extract1 != tmp.getCount()) {
                            if (extract1 == 0) break;
                            tmp.setCount((int) extract1);
                        }
                        InvUtil.tryPlace(availableBackpackInv, tmp);
                        extract -= extract1;
                        restNeed -= extract1;
                        count += extract1;
                    }
                }
            }
            if (count > 0) {
                RequestListItem.addItemStackCollected(maid.getMainHandItem(), item.getA(), count);
            }
        }
    }
}