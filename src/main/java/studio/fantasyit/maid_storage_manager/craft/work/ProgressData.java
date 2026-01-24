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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
    public enum Status {
        NORMAL,
        WAITING,
        FAILED
    }

    public record ProgressMeta(UUID uuid, ProgressPad.Viewing viewing, ProgressPad.Style style,
                               ProgressPad.Merge merge) {
        public static ProgressMeta fromNetwork(FriendlyByteBuf buf) {
            return new ProgressMeta(buf.readUUID(), ProgressPad.Viewing.valueOf(buf.readUtf()),
                    ProgressPad.Style.valueOf(buf.readUtf()), ProgressPad.Merge.valueOf(buf.readUtf()));
        }

        public static @Nullable ProgressMeta fromItemStack(ItemStack itemStack) {
            UUID bindingUUID = ProgressPad.getBindingUUID(itemStack);
            if (bindingUUID == null)
                return null;
            return new ProgressMeta(
                    bindingUUID,
                    ProgressPad.getViewing(itemStack),
                    ProgressPad.getStyle(itemStack),
                    ProgressPad.getMerge(itemStack)
            );
        }

        public void toNetwork(FriendlyByteBuf buf) {
            buf.writeUUID(this.uuid);
            buf.writeUtf(this.viewing.name());
            buf.writeUtf(this.style.name());
            buf.writeUtf(this.merge.name());
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ProgressMeta progressMeta) {
                return progressMeta.uuid.equals(this.uuid) && progressMeta.viewing == this.viewing && progressMeta.style == this.style && progressMeta.merge == this.merge;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return uuid.hashCode() * 31 + merge.hashCode() * 17 + viewing.hashCode() * 13 + style.hashCode();
        }
    }

    public record TaskProgress(List<ItemStack> outputs, int total, int progress, Status status, List<Component> taker) {
        public static TaskProgress fromNetwork(FriendlyByteBuf friendlyByteBuf) {
            return new TaskProgress(
                    friendlyByteBuf.readCollection(ArrayList::new, (t) -> {
                        if (t instanceof RegistryFriendlyByteBuf t1)
                            return ItemStackUtil.parseStack(t1.registryAccess(), t1.readNbt());
                        return ItemStack.EMPTY;
                    }),
                    friendlyByteBuf.readInt(),
                    friendlyByteBuf.readInt(),
                    friendlyByteBuf.readEnum(Status.class),
                    friendlyByteBuf.readCollection(ArrayList::new, (t) -> t.readJsonWithCodec(ComponentSerialization.CODEC))
            );
        }

        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.writeCollection(outputs, (t, c) -> {
                if (t instanceof RegistryFriendlyByteBuf t1)
                    t1.writeNbt(ItemStackUtil.saveStack(t1.registryAccess(), c));
            });
            friendlyByteBuf.writeInt(total);
            friendlyByteBuf.writeInt(progress);
            friendlyByteBuf.writeEnum(status);
            friendlyByteBuf.writeCollection(taker, (t, c) -> t.writeJsonWithCodec(ComponentSerialization.CODEC, c));
        }
    }

    public final List<TaskProgress> working;
    public final Component maidName;
    public final List<Component> workGroups;
    public final List<ItemStack> items;
    public final int total;
    public final int progress;
    public final Status status;
    public final int tickCount;
    public final int maxSz;

    public ProgressData(List<TaskProgress> working, Component maidName, List<Component> workGroups, List<ItemStack> items, int total, int progress, int tickCount, int maxSz, Status status) {
        this.working = working;
        this.maidName = maidName;
        this.workGroups = workGroups;
        this.items = items;
        this.total = total;
        this.progress = progress;
        this.tickCount = tickCount;
        this.maxSz = maxSz;
        this.status = status;
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
        buf.writeEnum(status);
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
                buf.readInt(),
                buf.readEnum(Status.class)
        );
    }

    public static ProgressData fromPlan(EntityMaid maid, ServerLevel level, CraftLayerChain plan, ProgressPad.Viewing viewing, ProgressPad.Merge merge, int maxSz) {
        List<TaskProgress> toList = new ArrayList<>();
        List<ItemStack> targetItem = List.of();
        boolean peekItem = false;
        for (int i = 0; i < plan.getLayerCount(); i++) {
            SolvedCraftLayer node = plan.getNode(i);
            CraftLayer layer = plan.getLayer(i);
            if (node.group() != plan.getCurrentGroup()) continue;
            if (peekItem && layer.getCraftData().isEmpty()) {
                targetItem = layer.getItems();
            }
            peekItem = true;
            if (!switch (node.progress().getValue()) {
                case IDLE, WAITING, STANDBY -> viewing == ProgressPad.Viewing.WAITING;
                case PREFETCH, DISPATCHED, WORKING, GATHERING -> viewing == ProgressPad.Viewing.WORKING;
                case FINISHED -> viewing == ProgressPad.Viewing.DONE;
                case FAILED -> viewing != ProgressPad.Viewing.WORKING;
            }) continue;
            Status status = switch (node.progress().getValue()) {
                case IDLE, WAITING, STANDBY -> Status.WAITING;
                case FAILED -> Status.FAILED;
                default -> Status.NORMAL;
            };
            int totalSteps = layer.getCount();
            int processedSteps = layer.getDoneCount();
            if (node.progress().getValue() == SolvedCraftLayer.Progress.WORKING || node.progress().getValue() == SolvedCraftLayer.Progress.GATHERING)
                processedSteps += 1;
            List<Component> taker = List.of();
            if (node.progress().getValue() == SolvedCraftLayer.Progress.DISPATCHED) {
                UUID takerUUID = plan.getLayerTaker(node.index());
                if (level.getEntity(takerUUID) instanceof EntityMaid takerMaid) {
                    taker = List.of(takerMaid.getDisplayName());

                    if (MemoryUtil.getCrafting(takerMaid).hasPlan()) {
                        CraftLayerChain dispatchedPlan = MemoryUtil.getCrafting(takerMaid).plan();
                        if (dispatchedPlan.hasCurrent()) {
                            CraftLayer dispatchedLayer = dispatchedPlan.getCurrentLayer();
                            totalSteps = dispatchedLayer.getCount();
                            processedSteps = dispatchedLayer.getDoneCount();
                            if (plan.getIsStoppingAdding()) {
                                status = Status.FAILED;
                            }
                        }
                    }
                    if (MemoryUtil.getRequestProgress(takerMaid).isReturning()) {
                        totalSteps = processedSteps = 0;
                    }
                }
            }


            toList.add(new TaskProgress(
                    layer.getCraftData().map(CraftGuideData::getOutput).map(
                            t -> t.stream().map(ii -> ii.copyWithCount(ii.getCount() * layer.getCount())).toList()
                    ).orElse(List.of()),
                    totalSteps,
                    processedSteps,
                    status,
                    taker
            ));
        }

        toList = mergeAndSlice(merge, maxSz, toList);

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
                maxSz,
                plan.getIsStoppingAdding() ? Status.FAILED : Status.NORMAL
        );
    }

    public static ProgressData fromMaidNoPlan(EntityMaid maid, int maxSz) {
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
                maxSz,
                Status.WAITING
        );
    }

    public static ProgressData fromRequest(EntityMaid maid, ServerLevel level, ItemStack requestList, ProgressPad.Viewing viewing, ProgressPad.Merge merge, int maxSz) {
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
                    return new TaskProgress(List.of(item), cnt, collected, Status.NORMAL, List.of());
                }).filter(Objects::nonNull)
                .toList();
        progresses = mergeAndSlice(merge, maxSz, progresses);
        return new ProgressData(
                progresses,
                maid.getName(),
                WorkCardItem.getAllWorkCards(maid),
                List.of(ItemRegistry.REQUEST_LIST_ITEM.get().getDefaultInstance()),
                total.getValue(),
                done.getValue(),
                maid.tickCount,
                maxSz,
                Status.NORMAL);
    }

    @NotNull
    private static List<TaskProgress> mergeAndSlice(ProgressPad.Merge merge, int maxSz, List<TaskProgress> progresses) {
        if (merge == ProgressPad.Merge.ALWAYS)
            progresses = ProgressData.mergeSame(progresses);
        if (progresses.size() > maxSz) {
            if (merge == ProgressPad.Merge.OVERFLOW_ONLY)
                progresses = ProgressData.mergeSame(progresses.subList(0, maxSz));
            if (progresses.size() > maxSz)
                progresses = progresses.subList(0, maxSz);
        }
        return progresses;
    }

    private static ProgressData fromPlacing(EntityMaid maid, ServerLevel level, PlacingInventoryMemory placingInv, ProgressPad.Viewing viewing, ProgressPad.Merge merge, int maxSz) {
        List<TaskProgress> list = placingInv.arrangeItems.stream()
                .map(item -> new TaskProgress(List.of(item), 1, 0, Status.NORMAL, List.of()))
                .toList();
        list = mergeAndSlice(merge, maxSz, (List<TaskProgress>) list);
        return new ProgressData(
                list,
                maid.getName(),
                WorkCardItem.getAllWorkCards(maid),
                List.of(Items.CHEST.getDefaultInstance()),
                0,
                0,
                maid.tickCount,
                maxSz,
                Status.NORMAL);
    }

    public static ProgressData fromMaidAuto(EntityMaid maid, ServerLevel level, ProgressPad.Viewing viewing, ProgressPad.Merge merge, int maxSz) {
        if (Conditions.takingRequestList(maid)) {
            if (MemoryUtil.getCrafting(maid).hasPlan() && !MemoryUtil.getRequestProgress(maid).isReturning()) {
                if (MemoryUtil.getCrafting(maid).isGoPlacingBeforeCraft()) {
                    return ProgressData.fromPlacing(maid, level, MemoryUtil.getPlacingInv(maid), viewing, merge, maxSz);
                }
                return ProgressData.fromPlan(maid, level, MemoryUtil.getCrafting(maid).plan(), viewing, merge, maxSz);
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
                            maxSz,
                            Status.NORMAL);
                return ProgressData.fromRequest(maid, level, maid.getMainHandItem(), viewing, merge, maxSz);
            }
        } else if (MemoryUtil.getCurrentlyWorking(maid) == ScheduleBehavior.Schedule.PLACE) {
            return ProgressData.fromPlacing(maid, level, MemoryUtil.getPlacingInv(maid), viewing, merge, maxSz);
        }
        return ProgressData.fromMaidNoPlan(maid, maxSz);
    }

    public static List<TaskProgress> mergeSame(List<TaskProgress> tasks) {
        List<TaskProgress> res = new ArrayList<>();
        for (TaskProgress task : tasks) {
            boolean found = false;
            for (int i = res.size() - 1; i >= 0; i--) {
                if (task.outputs.size() != res.get(i).outputs.size()) continue;
                boolean eq = true;
                for (int j = 0; eq && j < task.outputs.size(); j++) {
                    eq = ItemStackUtil.isSameInCrafting(task.outputs.get(j), res.get(i).outputs.get(j));
                }
                if (eq) {
                    List<ItemStack> newOutputs = new ArrayList<>();
                    List<Component> allTakers = new ArrayList<>();
                    for (int j = 0; j < task.outputs.size(); j++) {
                        newOutputs.add(res.get(i).outputs.get(j).copyWithCount(task.outputs.get(j).getCount() + res.get(i).outputs.get(j).getCount()));
                    }
                    allTakers.addAll(res.get(i).taker());
                    allTakers.addAll(task.taker());
                    res.set(i, new TaskProgress(
                            newOutputs,
                            task.total() + res.get(i).total(),
                            task.progress() + res.get(i).progress(),
                            task.status(),
                            allTakers
                    ));
                    found = true;
                }
            }
            if (!found) {
                res.add(task);
            }
        }
        return res;
    }
}
