package studio.fantasyit.maid_storage_manager.craft.algo;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.craft.algo.base.AbstractBiCraftGraph;
import studio.fantasyit.maid_storage_manager.craft.algo.base.BiCraftCountCalculator;
import studio.fantasyit.maid_storage_manager.craft.algo.base.ICraftGraphLike;
import studio.fantasyit.maid_storage_manager.craft.algo.graph.FlattenSearchGraph;
import studio.fantasyit.maid_storage_manager.craft.algo.graph.SimpleSearchGraph;
import studio.fantasyit.maid_storage_manager.craft.algo.graph.ThreadedSearchGraph;
import studio.fantasyit.maid_storage_manager.craft.algo.graph.TopologyCraftGraph;
import studio.fantasyit.maid_storage_manager.craft.algo.misc.ItemListStepSum;
import studio.fantasyit.maid_storage_manager.craft.algo.utils.RequestListSplitter;
import studio.fantasyit.maid_storage_manager.craft.algo.utils.ResultListOptimizer;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.debug.CraftingDebugContext;
import studio.fantasyit.maid_storage_manager.craft.debug.IDebugContextSetter;
import studio.fantasyit.maid_storage_manager.craft.generator.AutoGraphGenerator;
import studio.fantasyit.maid_storage_manager.craft.work.CraftLayer;
import studio.fantasyit.maid_storage_manager.craft.work.CraftLayerChain;
import studio.fantasyit.maid_storage_manager.items.PortableCraftCalculatorBauble;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.maid.ChatTexts;
import studio.fantasyit.maid_storage_manager.maid.data.StorageManagerConfigData;
import studio.fantasyit.maid_storage_manager.maid.memory.CraftMemory;
import studio.fantasyit.maid_storage_manager.maid.memory.ViewedInventoryMemory;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.InvUtil;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;
import studio.fantasyit.maid_storage_manager.util.StorageAccessUtil;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class MaidCraftPlanner implements IDebugContextSetter {
    private final CraftMemory craftingMemory;
    ICraftGraphLike currentAvailableGraph;

    boolean done = false;
    int success = 0;
    int count = 0;
    ServerLevel level;
    EntityMaid maid;
    List<Pair<ItemStack, Integer>> notDone;
    Queue<Pair<Queue<Pair<ItemStack, Integer>>, ICraftGraphLike.CraftAlgorithmInit<?>>> craftJobs = new LinkedList<>();
    Queue<Pair<ItemStack, Integer>> tmpNextJob = new LinkedList<>();
    ItemListStepSum futureSteps;
    AutoGraphGenerator autoGraphGenerator;
    private List<CraftGuideData> craftGuides;
    private CraftingDebugContext debugContext = CraftingDebugContext.Dummy.INSTANCE;

    CraftLayerChain plan;

    public MaidCraftPlanner(ServerLevel level, EntityMaid maid) {
        this.craftingMemory = MemoryUtil.getCrafting(maid);
        this.maid = maid;
        this.level = level;
        this.count = 0;
        if (!precheck()) {
            done = true;
            return;
        }
        for (ICraftGraphLike.CraftAlgorithmInit<?> craftAlgorithmInit : initGraphList()) {
            craftJobs.add(new Pair<>(
                    new LinkedList<>(),
                    craftAlgorithmInit
            ));
        }
        craftGuides = new ArrayList<>(MemoryUtil.getCrafting(maid).getCraftGuides());
        notDone = RequestListItem.getItemStacksNotDone(maid.getMainHandItem());
        if (Config.craftingGenerateCraftGuide) {
            autoGraphGenerator = new AutoGraphGenerator(
                    maid,
                    notDone.stream().map(itemStack -> itemStack.getA()).toList(),
                    craftGuides
            );
            autoGraphGenerator.setDebugContext(debugContext);
        } else {
            initItems();
        }
        plan = new CraftLayerChain(maid);
    }

    protected boolean precheck() {
        if (PortableCraftCalculatorBauble.getCalculator(maid).isEmpty()) {
            if (!Config.craftingNoCalculator) {
                debugContext.logNoLevel(CraftingDebugContext.TYPE.PLANNER, "No Calculator found");
                return false;
            }
        }
        if (RequestListItem.isBlackMode(maid.getMainHandItem())) {
            debugContext.logNoLevel(CraftingDebugContext.TYPE.PLANNER, "Black list, no crafting");
            return false;
        }
        return true;
    }

    protected List<ICraftGraphLike.CraftAlgorithmInit<?>> initGraphList() {
        return Config.craftingSolver.stream()
                .map(craftSolver -> (switch (craftSolver) {
                    case TOPOLOGY -> (ICraftGraphLike.CraftAlgorithmInit<?>) TopologyCraftGraph::new;
                    case DFS -> (ICraftGraphLike.CraftAlgorithmInit<?>) SimpleSearchGraph::new;
                    case DFS_QUEUED -> (ICraftGraphLike.CraftAlgorithmInit<?>) FlattenSearchGraph::new;
                    case DFS_THREADED -> (ICraftGraphLike.CraftAlgorithmInit<?>) ThreadedSearchGraph::new;
                }))
                .toList();
    }

    protected boolean initItems() {
        debugContext.logNoLevel(CraftingDebugContext.TYPE.PLANNER, "[REQUEST_CRAFT]Start. Calculate tree");
        if (notDone.isEmpty()) {
            return false;
        }
        Target storage = RequestListItem.getStorageBlock(maid.getMainHandItem());
        List<Pair<ItemStack, Integer>> items = new ArrayList<>();
        MemoryUtil.getViewedInventory(maid).positionFlatten()
                .forEach((pos, itemStacks) -> {
                    if (pos.equals(storage)) return;
                    if (StorageAccessUtil.findTargetRewrite(level, maid, pos, false).isEmpty()) return;
                    for (ViewedInventoryMemory.ItemCount itemStack : itemStacks) {
                        boolean flag = false;
                        for (int i = 0; i < items.size(); i++) {
                            if (ItemStackUtil.isSameInCrafting(itemStack.getFirst(), items.get(i).getA())) {
                                items.set(i, new Pair<>(
                                        items.get(i).getA(),
                                        items.get(i).getB() + itemStack.getSecond()
                                ));
                                flag = true;
                                break;
                            }
                        }
                        if (flag) continue;
                        items.add(new Pair<>(
                                itemStack.getFirst(),
                                itemStack.getSecond()
                        ));
                        debugContext.logNoLevel(CraftingDebugContext.TYPE.PLANNER, "Adding Stock ItemStack %s * %s;", itemStack.getFirst(), itemStack.getSecond());
                    }
                });

        futureSteps = new ItemListStepSum(notDone);
        craftJobs.peek().getA().addAll(notDone);
        currentAvailableGraph = craftJobs
                .peek()
                .getB().init(items, craftGuides);
        debugContext.convey(currentAvailableGraph);
        notDone.forEach(itemStack -> currentAvailableGraph.setItemCount(itemStack.getA(), 0));
        notDone.forEach(itemStack -> {
            debugContext.logNoLevel(CraftingDebugContext.TYPE.PLANNER, "Adding Craft ItemStack %s * %s;", itemStack.getA(), itemStack.getB());
        });
        return true;
    }


    /// /任务步骤
    BiCraftCountCalculator biCalc = null;
    Pair<ItemStack, Integer> currentWork = null;

    public boolean done() {
        return done;
    }

    public void tick(long tick) {
        sendMaidProgressBubble();

        if (autoGraphGenerator != null) {
            if (autoGraphGenerator.process()) {
                List<CraftGuideData> craftGuideData = autoGraphGenerator.getCraftGuideData();
                //去除和用户添加的有重叠的部分（第一输出相同）
                craftGuideData = craftGuideData
                        .stream().
                        filter(c -> {
                                    if (!c.getOutput()
                                            .stream()
                                            .findFirst()
                                            .map(i1 ->
                                                    craftGuides
                                                            .stream()
                                                            .anyMatch(c2 -> c2
                                                                    .getOutput()
                                                                    .stream()
                                                                    .findFirst()
                                                                    .map(i -> ItemStackUtil.isSameInCrafting(i, i1))
                                                                    .orElse(false)
                                                            )
                                            )
                                            .orElse(false)) {
                                        return true;
                                    }
                                    debugContext.logNoLevel(CraftingDebugContext.TYPE.PLANNER, "Craft Guide %s is overlapped with user added craft guide", c);
                                    return false;
                                }
                        )
                        .toList();
                craftGuides.addAll(craftGuideData);
                autoGraphGenerator.cache();
                autoGraphGenerator = null;
                initItems();
            }
            return;
        }

        //如果所有任务层完成，标记done
        if (craftJobs.isEmpty()) {
            done = true;
            return;
        }
        //如果当前任务层完成，则移除
        if (biCalc == null && craftJobs.peek().getA().isEmpty()) {
            craftJobs.poll();
            if (!craftJobs.isEmpty()) {
                while (!tmpNextJob.isEmpty()) craftJobs.peek().getA().add(tmpNextJob.poll());
                currentAvailableGraph = currentAvailableGraph.createGraphWithItem(craftJobs.peek().getB());
                debugContext.convey(currentAvailableGraph);
            }
            count = 0;
            return;
        }
        //上一任务已经完成，获取下一任务
        if (currentWork == null) {
            currentWork = craftJobs.peek().getA().poll();
            if (currentWork != null && currentWork.getB() == 0)
                currentWork = null;
            if (currentWork == null)
                return;
        }
        //当前任务初始化
        if (biCalc == null) {
            if (currentAvailableGraph instanceof AbstractBiCraftGraph abstractGraph)
                debugContext.saveGraph(abstractGraph, currentWork.getA(), currentWork.getB());
            biCalc = new BiCraftCountCalculator(
                    currentAvailableGraph,
                    currentWork.getA(),
                    currentWork.getB(),
                    InvUtil.freeSlots(maid.getAvailableInv(true))
            );
            debugContext.convey(biCalc);
        }
        //进行一个计算的尝试
        boolean finish = false;
        for (int i = 0; i < 5; i++) {
            if (biCalc.tick()) continue;
            finish = true;
            break;
        }
        if (finish) {
            handleResult();
            biCalc = null;
        }
    }

    protected void handleResult() {
        List<CraftLayer> results = biCalc.getResults();

        if (results.isEmpty()) {
            debugContext.logNoLevel(CraftingDebugContext.TYPE.PLANNER,
                    "[REQUEST_CRAFT] Failed to find recipe for %s",
                    currentWork.getA().getHoverName().getString()
            );
            if (biCalc.hasAnySuccessCraftingCalc()) {
                RequestListItem.setFailAddition(maid.getMainHandItem(),
                        currentWork.getA(),
                        "tooltip.maid_storage_manager.request_list.fail_backpack_full");
            }
            RequestListItem.markDone(maid.getMainHandItem(), currentWork.getA());
        } else {
            List<CraftLayer> optimize = ResultListOptimizer.optimize(results);
            optimize = RequestListSplitter.splitLayerMax(optimize, StorageManagerConfigData.get(maid).maxCraftingLayerRepeatCount());
            optimize.forEach(craftLayer -> plan.addLayer(craftLayer));

            debugContext.logEntryNewLevel(CraftingDebugContext.TYPE.PLANNER,
                    "[REQUEST_CRAFT] %s tree with %d layers",
                    currentWork.getA().getHoverName().getString(),
                    results.size()
            );
            optimize.forEach(t -> debugContext.log(CraftingDebugContext.TYPE.PLANNER, "Layer g:%s * %s", t.getCraftData(), t.getCount()));
            debugContext.exitLogLevel(CraftingDebugContext.TYPE.PLANNER, "[REQUEST_CRAFT] layers");
            success += 1;
        }
        List<Pair<ItemStack, Integer>> fails = biCalc.getFails();
        if (!fails.isEmpty())
            RequestListItem.setMissingItem(
                    maid.getMainHandItem(),
                    currentWork.getA(),
                    fails.stream().map(e -> e.getA().copyWithCount(e.getB())).toList()
            );
        int restCount = biCalc.getNotCraftedCount();
        tmpNextJob.add(new Pair<>(currentWork.getA(), restCount));
        biCalc = null;
        currentWork = null;
        count++;
    }

    private void sendMaidProgressBubble() {
        if (autoGraphGenerator != null) {
            ChatTexts.progress(maid, Component.translatable(
                    ChatTexts.CHAT_CRAFT_GENERATING,
                    autoGraphGenerator.getDone(),
                    autoGraphGenerator.getTotal()
            ), autoGraphGenerator.getProgress());
            craftingMemory.calculatingProgress = autoGraphGenerator.getDone();
            craftingMemory.calculatingTotal = autoGraphGenerator.getTotal();
            return;
        }
        if (biCalc == null) return;
        int restSteps = futureSteps.getStep(count) + biCalc.getWorstRestSteps();
        ChatTexts.progress(maid, Component.translatable(
                ChatTexts.CHAT_CRAFT_CALCULATE,
                String.valueOf(futureSteps.getTotalStep() - restSteps),
                String.valueOf(futureSteps.getTotalStep())
        ), (double) futureSteps.getTotalStep() / (futureSteps.getTotalStep() - restSteps));
        craftingMemory.calculatingProgress = futureSteps.getTotalStep() - restSteps;
        craftingMemory.calculatingTotal = futureSteps.getTotalStep();
    }

    public boolean anySuccess() {
        return success > 0;
    }

    public CraftLayerChain getPlan() {
        return plan;
    }

    @Override
    public void setDebugContext(CraftingDebugContext context) {
        this.debugContext = context;
        context.convey(currentAvailableGraph);
        context.convey(autoGraphGenerator);
    }
}