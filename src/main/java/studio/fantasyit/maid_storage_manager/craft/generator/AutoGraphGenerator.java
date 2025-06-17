package studio.fantasyit.maid_storage_manager.craft.generator;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.MaidPathFindingBFS;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import studio.fantasyit.maid_storage_manager.craft.CraftManager;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.generator.algo.GeneratorGraph;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;
import studio.fantasyit.maid_storage_manager.util.MoveUtil;

import java.util.List;

public class AutoGraphGenerator {
    private final EntityMaid maid;
    protected final GeneratorGraph graph;
    protected final List<IAutoCraftGuideGenerator> iAutoCraftGuideGenerators;
    protected final List<InventoryItem> inventory;
    protected final MaidPathFindingBFS pathfindingBFS;
    private final List<BlockPos> blockPosList;
    protected int index = 0;
    protected int craftGeneratorTypeIndex = 0;
    protected double currentMinDistance = Double.MAX_VALUE;
    protected BlockPos minPos;

    public AutoGraphGenerator(EntityMaid maid, List<ItemStack> itemList) {
        this.maid = maid;
        BlockPos center = maid.blockPosition();
        if (maid.hasRestriction())
            center = maid.getRestrictCenter();
        int distance = (int) Math.ceil(maid.hasRestriction() ? maid.getRestrictRadius() : 5);
        this.pathfindingBFS = new MaidPathFindingBFS(
                maid.getNavigation().getNodeEvaluator(),
                (ServerLevel) maid.level(),
                maid,
                distance
        );
        inventory = MemoryUtil.getViewedInventory(maid).flatten();
        graph = new GeneratorGraph(inventory.stream().map(i -> i.itemStack).toList(), maid.level().registryAccess(), itemList);
        iAutoCraftGuideGenerators = CraftManager.getInstance().getAutoCraftGuideGenerators();
        blockPosList = BlockPos
                .betweenClosedStream(new AABB(center).inflate(distance, 6, distance))
                .map(BlockPos::immutable)
                .filter(maid::isWithinRestriction)
                .toList();
    }

    public boolean processBlock() {
        int count = 0;
        if (craftGeneratorTypeIndex >= iAutoCraftGuideGenerators.size()) return true;
        IAutoCraftGuideGenerator generator = iAutoCraftGuideGenerators.get(craftGeneratorTypeIndex);
        while (index < blockPosList.size()) {
            BlockPos next = blockPosList.get(index++);
            if (generator.isBlockValid(maid.level(), next)) {
                if (MoveUtil.getAllAvailablePosForTarget((ServerLevel) maid.level(), maid, next, pathfindingBFS).isEmpty())
                    continue;
                double distance = maid.distanceToSqr(next.getCenter());
                if (distance < currentMinDistance) {
                    currentMinDistance = distance;
                    minPos = next;
                }
            }
            if (++count > 1000)
                return false;
        }
        if (minPos != null) {
            generator.generate(inventory, maid.level(), minPos, graph);
        }
        minPos = null;
        currentMinDistance = Double.MAX_VALUE;
        index = 0;
        craftGeneratorTypeIndex++;
        return craftGeneratorTypeIndex >= iAutoCraftGuideGenerators.size();
    }

    public List<CraftGuideData> getCraftGuideData() {
        return graph.craftGuides;
    }

    public int getDone() {
        if (isProcessingBlocks)
            return index;
        return graph.processedSteps;
    }

    public int getTotal() {
        if (isProcessingBlocks)
            return blockPosList.size();
        return graph.pushedSteps;
    }

    public float getProgress() {
        if (isProcessingBlocks)
            return (float) index / blockPosList.size();
        return (float) graph.processedSteps / graph.pushedSteps;
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
}
