package studio.fantasyit.maid_storage_manager.craft.work;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.apache.commons.lang3.mutable.MutableInt;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.items.ProgressPad;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.items.WorkCardItem;
import studio.fantasyit.maid_storage_manager.items.data.RequestItemStackList;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.maid.memory.PlacingInventoryMemory;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.util.Conditions;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ProgressData {
    public record TaskProgress(List<ItemStack> outputs, int total, int progress, Component taker) {
        public static TaskProgress fromNetwork(FriendlyByteBuf friendlyByteBuf) {
            return new TaskProgress(
                    friendlyByteBuf.readCollection(ArrayList::new, (t) -> {
                        if (t instanceof RegistryFriendlyByteBuf t1)
                            return ItemStackUtil.parseStack(t1.registryAccess(), t1.readNbt());
                        return ItemStack.EMPTY;
                    }),
                    friendlyByteBuf.readInt(),
                    friendlyByteBuf.readInt(),
                    friendlyByteBuf.readJsonWithCodec(ComponentSerialization.CODEC)
            );
        }

        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.writeCollection(outputs, (t, c) -> {
                if (t instanceof RegistryFriendlyByteBuf t1)
                    t1.writeNbt(ItemStackUtil.saveStack(t1.registryAccess(), c));
            });
            friendlyByteBuf.writeInt(total);
            friendlyByteBuf.writeInt(progress);
            friendlyByteBuf.writeJsonWithCodec(ComponentSerialization.CODEC, taker);
        }
    }

    public final List<TaskProgress> working;
    public final Component maidName;
    public final List<Component> workGroups;
    public final List<ItemStack> items;
    public final int total;
    public final int progress;
    public final int tickCount;
    public final int maxSz;

    public ProgressData(List<TaskProgress> working, Component maidName, List<Component> workGroups, List<ItemStack> items, int total, int progress, int tickCount, int maxSz) {
        this.working = working;
        this.maidName = maidName;
        this.workGroups = workGroups;
        this.items = items;
        this.total = total;
        this.progress = progress;
        this.tickCount = tickCount;
        this.maxSz = maxSz;
    }

    public void toNetwork(RegistryFriendlyByteBuf buf) {
        buf.writeCollection(working, (t, d) -> d.toNetwork((RegistryFriendlyByteBuf) t));
        buf.writeJsonWithCodec(ComponentSerialization.CODEC, maidName);
        buf.writeCollection(workGroups, (t, c) -> t.writeJsonWithCodec(ComponentSerialization.CODEC, c));
        buf.writeCollection(items, (t, c) -> {
            if (t instanceof RegistryFriendlyByteBuf t1)
                t1.writeNbt(ItemStackUtil.saveStack(t1.registryAccess(), c));
        });
        buf.writeInt(total);
        buf.writeInt(progress);
        buf.writeInt(tickCount);
        buf.writeInt(maxSz);
    }

    public static ProgressData fromNetwork(RegistryFriendlyByteBuf buf) {
        return new ProgressData(
                buf.readCollection(ArrayList::new, TaskProgress::fromNetwork),
                buf.readJsonWithCodec(ComponentSerialization.CODEC),
                buf.readCollection(ArrayList::new, t -> t.readJsonWithCodec(ComponentSerialization.CODEC)),
                buf.readCollection(ArrayList::new, t -> {
                    if (t instanceof RegistryFriendlyByteBuf t1)
                        return ItemStackUtil.parseStack(t1.registryAccess(), t1.readNbt());
                    return ItemStack.EMPTY;
                }),
                buf.readInt(),
                buf.readInt(),
                buf.readInt(),
                buf.readInt()
        );
    }

    public static ProgressData fromPlan(EntityMaid maid, ServerLevel level, CraftLayerChain plan, ProgressPad.Viewing viewing, int maxSz) {
        List<TaskProgress> toList = new ArrayList<>();
        List<ItemStack> targetItem = List.of();
        boolean peekItem = false;
        for (int i = 0; i < plan.getLayerCount(); i++) {
            SolvedCraftLayer node = plan.getNode(i);
            CraftLayer layer = plan.getLayer(i);
            if (peekItem && layer.getCraftData().isEmpty()) {
                targetItem = layer.getItems();
            }
            if (node.group() != plan.getCurrentGroup()) continue;
            peekItem = true;
            if (!switch (node.progress().getValue()) {
                case IDLE, WAITING -> viewing == ProgressPad.Viewing.WAITING;
                case DISPATCHED, WORKING, GATHERING -> viewing == ProgressPad.Viewing.WORKING;
                case FINISHED -> viewing == ProgressPad.Viewing.DONE;
                default -> false;
            }) continue;
            int totalSteps = layer.getTotalStep() * layer.getCount() + 1;
            int processedSteps = layer.getDoneCount() * layer.getTotalStep() + layer.getStep();
            if (node.progress().getValue() == SolvedCraftLayer.Progress.WORKING || node.progress().getValue() == SolvedCraftLayer.Progress.GATHERING)
                processedSteps += 1;
            Component taker = Component.empty();
            if (node.progress().getValue() == SolvedCraftLayer.Progress.DISPATCHED) {
                UUID takerUUID = plan.getLayerTaker(node.index());
                if (level.getEntity(takerUUID) instanceof EntityMaid takerMaid) {
                    taker = takerMaid.getDisplayName();

                    if (MemoryUtil.getCrafting(takerMaid).hasPlan()) {
                        CraftLayerChain dispatchedPlan = MemoryUtil.getCrafting(takerMaid).plan();
                        if (dispatchedPlan.hasCurrent()) {
                            CraftLayer dispatchedLayer = dispatchedPlan.getCurrentLayer();
                            totalSteps = dispatchedLayer.getTotalStep() * dispatchedLayer.getCount() + 1;
                            processedSteps = dispatchedLayer.getDoneCount() * dispatchedLayer.getTotalStep() + dispatchedLayer.getStep() + 1;
                        }
                    }
                }
            }


            toList.add(new TaskProgress(
                    layer.getCraftData().map(CraftGuideData::getOutput).orElse(List.of()),
                    totalSteps,
                    processedSteps,
                    taker
            ));
        }

        if (toList.size() > maxSz) {
            if (viewing != ProgressPad.Viewing.DONE)
                toList = toList.subList(0, maxSz);
            else
                toList = toList.subList(toList.size() - maxSz, toList.size());
        }

        int done = 0;
        int total = 0;
        for (int i = 0; i < plan.getLayerCount(); i++) {
            CraftLayer layer = plan.getLayer(i);
            SolvedCraftLayer node = plan.getNode(i);

            if (node.progress().getValue() == SolvedCraftLayer.Progress.FINISHED) {
                done += layer.getCount() * layer.getTotalStep() + 1;
            } else if (node.progress().getValue() == SolvedCraftLayer.Progress.WORKING || node.progress().getValue() == SolvedCraftLayer.Progress.DISPATCHED) {
                done += layer.getDoneCount() * layer.getTotalStep() + layer.getStep() + 1;
            }
            total += layer.getCount() * layer.getTotalStep() + 1;
        }

        return new ProgressData(
                toList,
                maid.getName(),
                WorkCardItem.getAllWorkCards(maid),
                targetItem,
                total,
                done,
                maid.tickCount,
                maxSz);
    }

    public static ProgressData fromMaidNoPlan(EntityMaid maid,int maxSz) {
        Item icon = (switch (MemoryUtil.getCurrentlyWorking(maid)) {
            case CO_WORK -> Items.PLAYER_HEAD;
            case MEAL -> Items.COOKED_BEEF;
            case LOGISTICS -> ItemRegistry.LOGISTICS_GUIDE.get();
            case RESORT -> Items.CHEST;
            default -> null;
        });
        return new ProgressData(
                List.of(),
                maid.getName(),
                WorkCardItem.getAllWorkCards(maid),
                icon != null ? List.of(icon.getDefaultInstance()) : List.of(),
                0,
                0,
                maid.tickCount,
                maxSz
        );
    }

    public static ProgressData fromRequest(EntityMaid maid, ServerLevel level, ItemStack requestList, ProgressPad.Viewing viewing, int maxSz) {
        RequestItemStackList.Immutable requestData = RequestListItem.getImmutableRequestData(requestList);
        List<RequestItemStackList.ImmutableItem> list = requestData.list();
        MutableInt total = new MutableInt(0);
        MutableInt done = new MutableInt(0);
        List<TaskProgress> progresses = list.stream()
                .filter(t -> !t.done())
                .map(t -> {
                    ItemStack item = t.item();
                    if (item.isEmpty()) return null;
                    int cnt = t.requested();
                    int collected = t.collected();
                    total.increment();
                    if (cnt != -1 && collected >= cnt)
                        done.increment();
                    return new TaskProgress(List.of(item), collected, cnt, Component.empty());
                }).filter(Objects::nonNull)
                .toList();
        if (progresses.size() > maxSz)
            progresses = progresses.subList(0, maxSz);
        return new ProgressData(
                progresses,
                maid.getName(),
                WorkCardItem.getAllWorkCards(maid),
                List.of(ItemRegistry.REQUEST_LIST_ITEM.get().getDefaultInstance()),
                total.getValue(),
                done.getValue(),
                maid.tickCount,
                maxSz);
    }

    private static ProgressData fromPlacing(EntityMaid maid, ServerLevel level, PlacingInventoryMemory placingInv, ProgressPad.Viewing viewing, int maxSz) {
        List<TaskProgress> list = placingInv.arrangeItems.stream()
                .map(item -> new TaskProgress(List.of(item), 1, 0, Component.empty()))
                .toList();
        if (list.size() > maxSz)
            list = list.subList(0, maxSz);
        return new ProgressData(
                list,
                maid.getName(),
                WorkCardItem.getAllWorkCards(maid),
                List.of(Items.CHEST.getDefaultInstance()),
                0,
                0,
                maid.tickCount,
                maxSz);
    }

    public static ProgressData fromMaidAuto(EntityMaid maid, ServerLevel level, ProgressPad.Viewing viewing, int maxSz) {
        if (Conditions.takingRequestList(maid)) {
            if (MemoryUtil.getCrafting(maid).hasPlan()) {
                return ProgressData.fromPlan(maid, level, MemoryUtil.getCrafting(maid).plan(), viewing, maxSz);
            } else if (maid.getMainHandItem().is(ItemRegistry.REQUEST_LIST_ITEM.get())) {
                if (MemoryUtil.getCrafting(maid).calculatingTotal != -1)
                    return new ProgressData(
                            List.of(),
                            maid.getDisplayName(),
                            WorkCardItem.getAllWorkCards(maid),
                            List.of(ItemRegistry.PORTABLE_CRAFT_CALCULATOR_BAUBLE.get().getDefaultInstance()),
                            MemoryUtil.getCrafting(maid).calculatingTotal,
                            MemoryUtil.getCrafting(maid).calculatingProgress,
                            maid.tickCount,
                            maxSz);
                return ProgressData.fromRequest(maid, level, maid.getMainHandItem(), viewing, maxSz);
            }
        } else if (MemoryUtil.getCurrentlyWorking(maid) == ScheduleBehavior.Schedule.PLACE) {
            return ProgressData.fromPlacing(maid, level, MemoryUtil.getPlacingInv(maid), viewing, maxSz);
        }
        return ProgressData.fromMaidNoPlan(maid, maxSz);
    }

}
