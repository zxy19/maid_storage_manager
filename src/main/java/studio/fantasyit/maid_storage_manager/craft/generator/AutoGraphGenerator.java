package studio.fantasyit.maid_storage_manager.craft.generator;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.MaidPathFindingBFS;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.phys.AABB;
import org.apache.commons.lang3.mutable.MutableInt;
import studio.fantasyit.maid_storage_manager.craft.CraftManager;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.generator.algo.GeneratorGraph;
import studio.fantasyit.maid_storage_manager.craft.generator.type.base.IAutoCraftGuideGenerator;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

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

    public AutoGraphGenerator(EntityMaid maid, List<ItemStack> itemList, List<CraftGuideData> hasExisted) {
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
        MutableInt count = new MutableInt();
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
        int count = 0;
        if (craftGeneratorTypeIndex >= iAutoCraftGuideGenerators.size()) return true;
        IAutoCraftGuideGenerator generator = iAutoCraftGuideGenerators.get(craftGeneratorTypeIndex);
        while (index < blockPosList.size()) {
            BlockPos next = blockPosList.get(index++);
            if (generator.isBlockValid(maid.level(), next)) {
                if (!generator.positionalAvailable((ServerLevel) maid.level(), maid, next, pathfindingBFS))
                    continue;
                if (generator.allowMultiPosition()) {
                    generator.generate(inventory, maid.level(), next, graph);
                } else {
                    //如果只允许一个位置，那么统计最近位置
                    double distance = maid.distanceToSqr(next.getCenter());
                    if (distance < currentMinDistance) {
                        currentMinDistance = distance;
                        minPos = next;
                    }
                }
            }
            if (++count > 2000)
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
            return index + craftGeneratorTypeIndex * blockPosList.size();
        return graph.processedSteps;
    }

    public int getTotal() {
        if (isProcessingBlocks)
            return blockPosList.size() * iAutoCraftGuideGenerators.size();
        return graph.pushedSteps;
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
}
