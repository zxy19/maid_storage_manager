package studio.fantasyit.maid_storage_manager.maid.behavior.request.craft;

import com.github.tartaricacid.touhoulittlemaid.entity.chatbubble.ChatBubbleManger;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.craft.AvailableCraftGraph;
import studio.fantasyit.maid_storage_manager.craft.CraftLayer;
import studio.fantasyit.maid_storage_manager.debug.DebugData;
import studio.fantasyit.maid_storage_manager.items.PortableCraftCalculatorBauble;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.maid.ChatTexts;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.maid.memory.ViewedInventoryMemory;
import studio.fantasyit.maid_storage_manager.storage.Storage;
import studio.fantasyit.maid_storage_manager.util.Conditions;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CraftInitBehavior extends Behavior<EntityMaid> {
    public CraftInitBehavior() {
        super(Map.of(), 10000);
    }


    private AvailableCraftGraph availableCraftGraph;
    List<Pair<ItemStack, Integer>> notDone;
    int count = 0;
    int success = 0;
    private boolean done;

    @Override
    protected boolean checkExtraStartConditions(ServerLevel p_22538_, EntityMaid p_22539_) {
        if (MemoryUtil.getCurrentlyWorking(p_22539_) != ScheduleBehavior.Schedule.REQUEST) return false;
        if (!Conditions.takingRequestList(p_22539_)) return false;
        if (!MemoryUtil.getRequestProgress(p_22539_).isTryCrafting()) return false;
        //女仆当前没有生成合成任务，应该立刻计算所有合成
        return !MemoryUtil.getCrafting(p_22539_).hasTasks();
    }

    @Override
    protected boolean canStillUse(@NotNull ServerLevel level, @NotNull EntityMaid maid, long p_22547_) {
        if (MemoryUtil.getCurrentlyWorking(maid) != ScheduleBehavior.Schedule.REQUEST) return false;
        if (!Conditions.takingRequestList(maid)) return false;
        return !done;
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long p_22542_) {
        if (PortableCraftCalculatorBauble.getCalculator(maid).isEmpty()) {
            DebugData.getInstance().sendMessage("[REQUEST_CRAFT]No Calculator found");
            done = true;
            success = 0;
            return;
        }
        ChatTexts.send(maid, ChatTexts.CHAT_CRAFT_CALCULATE);
        DebugData.getInstance().sendMessage("[REQUEST_CRAFT]Start. Calculate tree");
        notDone = RequestListItem.getItemStacksNotDone(maid.getMainHandItem());
        Storage storage = RequestListItem.getStorageBlock(maid.getMainHandItem());
        List<com.mojang.datafixers.util.Pair<ItemStack, Integer>> items;
        if (storage != null) {
            items = new ArrayList<>();
            MemoryUtil.getViewedInventory(maid).positionFlatten()
                    .forEach((pos, itemStacks) -> {
                        if (pos.equals(storage)) return;
                        for (ViewedInventoryMemory.ItemCount itemStack : itemStacks) {
                            boolean flag = false;
                            for (int i = 0; i < items.size(); i++) {
                                if (ItemStack.isSameItemSameTags(itemStack.getFirst(), items.get(i).getFirst())) {
                                    items.set(i, com.mojang.datafixers.util.Pair.of(
                                            items.get(i).getFirst(),
                                            items.get(i).getSecond() + itemStack.getSecond()
                                    ));
                                    flag = true;
                                    break;
                                }
                            }
                            if (flag) continue;
                            items.add(com.mojang.datafixers.util.Pair.of(
                                    itemStack.getFirst(),
                                    itemStack.getSecond()
                            ));
                        }
                    });

        } else
            items = MemoryUtil.getViewedInventory(maid).flatten();

        availableCraftGraph = new AvailableCraftGraph(
                items,
                MemoryUtil.getCrafting(maid).getCraftGuides()
        );

        DebugData.getInstance().sendMessage(String.format("[REQUEST_CRAFT] %d items in memory",
                MemoryUtil.getViewedInventory(maid).flatten().size()));
        DebugData.getInstance().sendMessage(String.format("[REQUEST_CRAFT] %d recipes prefetched",
                MemoryUtil.getCrafting(maid).getCraftGuides().size()));
        count = 0;
        success = 0;
        done = false;
        RequestListItem.getItemStacksNotDone(maid.getMainHandItem(), false)
                .forEach(itemStack -> {
                    availableCraftGraph.setCount(itemStack.getA(), 0);
                });

        MemoryUtil.getCrafting(maid).clearLayers();
        MemoryUtil.getCrafting(maid).resetVisitedPos();
        MemoryUtil.getCrafting(maid).startWorking(false);
    }

    @Override
    protected void tick(ServerLevel p_22551_, EntityMaid maid, long p_22553_) {
        if (!availableCraftGraph.buildGraph()) return;
        if (!availableCraftGraph.processQueues()) return;
        if (count != 0) {
            List<CraftLayer> results = availableCraftGraph.getResults();
            if (results == null) {
                DebugData.getInstance().sendMessage(
                        "[REQUEST_CRAFT] Failed to find recipe for %s",
                        notDone.get(count - 1).getA().getHoverName().getString()
                );
                List<Pair<ItemStack, Integer>> fails = availableCraftGraph.getFails();
                RequestListItem.setMissingItem(
                        maid.getMainHandItem(),
                        notDone.get(count - 1).getA(),
                        fails.stream().map(e -> e.getA().copyWithCount(e.getB())).toList()
                );
            } else {
                results.forEach(craftLayer -> {
                    MemoryUtil.getCrafting(maid).addLayer(craftLayer);
                });

                DebugData.getInstance().sendMessage(
                        "[REQUEST_CRAFT] %s tree with %d layers",
                        notDone.get(count - 1).getA().getHoverName().getString(),
                        results.size()
                );
                success += 1;
            }
        }
        if (count >= notDone.size()) {
            done = true;
            return;
        }
        availableCraftGraph.startContext(notDone.get(count).getA(), notDone.get(count).getB());
        count++;
    }

    @Override
    protected void stop(ServerLevel p_22548_, EntityMaid maid, long p_22550_) {
        if (success == 0) {
            // 没有成功合成，就直接返回
            RequestListItem.markAllDone(maid.getMainHandItem());
            MemoryUtil.getRequestProgress(maid).setTryCrafting(false);
            MemoryUtil.getRequestProgress(maid).setReturn(true);
            DebugData.getInstance().sendMessage("[REQUEST_CRAFT] Failed to find recipe for any items");
        }
        MemoryUtil.clearTarget(maid);
    }
}
