package studio.fantasyit.maid_storage_manager.craft.generator;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.MaidPathFindingBFS;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.phys.AABB;
import org.apache.commons.lang3.mutable.MutableInt;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.craft.CraftManager;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.debug.CraftingDebugContext;
import studio.fantasyit.maid_storage_manager.craft.debug.IDebugContextSetter;
import studio.fantasyit.maid_storage_manager.craft.generator.algo.GeneratorGraph;
import studio.fantasyit.maid_storage_manager.craft.generator.algo.ICachableGeneratorGraph;
import studio.fantasyit.maid_storage_manager.craft.generator.algo.ThreadedGeneratorGraph;
import studio.fantasyit.maid_storage_manager.craft.generator.cache.GraphCache;
import studio.fantasyit.maid_storage_manager.craft.generator.config.GeneratingConfig;
import studio.fantasyit.maid_storage_manager.craft.generator.type.base.IAutoCraftGuideGenerator;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

import java.util.*;

public class AutoGraphGenerator implements IDebugContextSetter {
    private static final ResourceLocation INTERNAL_TYPE = new ResourceLocation(MaidStorageManager.MODID, "_maid_storage_internal_existed");
    private final EntityMaid maid;
    protected final ICachableGeneratorGraph graph;
    protected final List<IAutoCraftGuideGenerator> iAutoCraftGuideGenerators;
    protected final List<InventoryItem> inventory;
    protected MaidPathFindingBFS pathfindingBFS;
    Map<ResourceLocation, List<BlockPos>> recognizedTypePositions;
    Set<ResourceLocation> hasDoneTypes;
    private final List<BlockPos> blockPosList;
    protected int index = 0;
    protected int craftGeneratorTypeIndex = 0;
    protected double currentMinDistance = Double.MAX_VALUE;
    protected BlockPos minPos;
    private CraftingDebugContext debugContext = CraftingDebugContext.Dummy.INSTANCE;

    protected ICachableGeneratorGraph getGraph(RegistryAccess registryAccess) {
        return switch (Config.craftingGenerator) {
            case RELEVANCE -> new GeneratorGraph(registryAccess);
            case RELEVANCE_THREADED -> new ThreadedGeneratorGraph(registryAccess);
        };
    }

    protected void updatePathfinding() {
        int distance = (int) Math.ceil(maid.hasRestriction() ? maid.getRestrictRadius() : 5);
        this.pathfindingBFS = new MaidPathFindingBFS(
                maid.getNavigation().getNodeEvaluator(),
                (ServerLevel) maid.level(),
                maid,
                distance,
                7
        );
    }

    public AutoGraphGenerator(EntityMaid maid, List<ItemStack> itemList, List<CraftGuideData> hasExisted) {
        this.maid = maid;
        BlockPos center = maid.blockPosition();
        if (maid.hasRestriction())
            center = maid.getRestrictCenter();
        int distance = (int) Math.ceil(maid.hasRestriction() ? maid.getRestrictRadius() : 5);
        updatePathfinding();
        inventory = MemoryUtil.getViewedInventory(maid).flatten();
        iAutoCraftGuideGenerators = CraftManager.getInstance().getAutoCraftGuideGenerators();
        blockPosList = BlockPos
                .betweenClosedStream(new AABB(center).inflate(distance, 6, distance))
                .map(BlockPos::immutable)
                .filter(maid::isWithinRestriction)
                .toList();
        MutableInt count = new MutableInt();
        GraphCache.CacheRecord cache = GraphCache.getAndValidate(maid.level(), maid, iAutoCraftGuideGenerators);
        hasDoneTypes = new HashSet<>();
        if (cache != null) {
            graph = cache.graph();
            recognizedTypePositions = cache.targets();
            hasDoneTypes.addAll(recognizedTypePositions.keySet());
            graph.invalidAllCraftWithType(INTERNAL_TYPE);
        } else {
            graph = getGraph(maid.level().registryAccess());
            recognizedTypePositions = new HashMap<>();
        }
        graph.setItems(inventory.stream().map(i -> i.itemStack).toList(), itemList);
        graph.setCurrentGeneratorType(INTERNAL_TYPE, true);
        if (graph instanceof IDebugContextSetter i) i.setDebugContext(debugContext);
        hasExisted.forEach(craftGuideData -> {
            graph.addRecipe(
                    new ResourceLocation("_maid_storage_internal_existed", String.valueOf(count.incrementAndGet())),
                    craftGuideData.getAllInputItemsWithOptional().stream().map(Ingredient::of).toList(),
                    craftGuideData.getAllInputItemsWithOptional().stream().map(ItemStack::getCount).toList(),
                    craftGuideData.getAllOutputItems(),
                    t -> null
            );
        });
    }

    public boolean processBlock() {
        updatePathfinding();
        int count = 0;
        if (craftGeneratorTypeIndex >= iAutoCraftGuideGenerators.size()) return true;
        IAutoCraftGuideGenerator generator = iAutoCraftGuideGenerators.get(craftGeneratorTypeIndex);
        if (!GeneratingConfig.isEnabled(generator.getType()) || hasDoneTypes.contains(generator.getType())) {
            craftGeneratorTypeIndex++;
            return false;
        }
        if (!recognizedTypePositions.containsKey(generator.getType()))
            recognizedTypePositions.put(generator.getType(), new ArrayList<>());
        while (index < blockPosList.size()) {
            BlockPos next = blockPosList.get(index++);
            if (generator.isBlockValid(maid.level(), next)) {
                if (!generator.positionalAvailable((ServerLevel) maid.level(), maid, next, pathfindingBFS))
                    continue;
                if (generator.allowMultiPosition() || !Config.generateNearestOnly) {
                    count++;
                    graph.setCurrentGeneratorType(generator);
                    debugContext.logNoLevel(CraftingDebugContext.TYPE.GENERATOR, "%s type generating at %s", generator, next);
                    generator.generate(inventory, maid.level(), next, graph, recognizedTypePositions);
                    recognizedTypePositions.get(generator.getType()).add(next);
                } else {
                    //如果只允许一个位置，那么统计最近位置
                    double distance = maid.distanceToSqr(next.getCenter());
                    if (distance < currentMinDistance) {
                        currentMinDistance = distance;
                        minPos = next;
                    }
                }
            }
            if (count > 10)
                return false;
        }
        if (minPos != null) {
            graph.setCurrentGeneratorType(generator);
            debugContext.logNoLevel(CraftingDebugContext.TYPE.GENERATOR, "%s type generating at %s", generator, minPos);
            generator.generate(inventory, maid.level(), minPos, graph, recognizedTypePositions);
            recognizedTypePositions.get(generator.getType()).add(minPos);
        }
        minPos = null;
        currentMinDistance = Double.MAX_VALUE;
        index = 0;
        hasDoneTypes.add(generator.getType());
        craftGeneratorTypeIndex++;
        return craftGeneratorTypeIndex >= iAutoCraftGuideGenerators.size();
    }

    public List<CraftGuideData> getCraftGuideData() {
        return graph.getCraftGuides();
    }

    public int getDone() {
        if (isProcessingBlocks)
            return index + craftGeneratorTypeIndex * blockPosList.size();
        return graph.getProcessedSteps();
    }

    public int getTotal() {
        if (isProcessingBlocks)
            return blockPosList.size() * iAutoCraftGuideGenerators.size();
        return graph.getPushedSteps();
    }

    public float getProgress() {
        return (float) getDone() / getTotal();
    }


    boolean isProcessingBlocks = true;

    /**
     * @return 返回处理是否完成
     */
    public boolean process() {
        if (isProcessingBlocks) {
            if (processBlock())
                isProcessingBlocks = false;
            return false;
        } else {
            return graph.process();
        }
    }

    public void cache() {
        GraphCache.putCache(maid, recognizedTypePositions, graph);
    }

    @Override
    public void setDebugContext(CraftingDebugContext context) {
        this.debugContext = context;
        context.convey(graph);
    }
}
