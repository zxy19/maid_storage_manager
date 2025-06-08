package studio.fantasyit.maid_storage_manager.craft.algo.utils;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.MaidPathFindingBFS;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import studio.fantasyit.maid_storage_manager.craft.CraftManager;
import studio.fantasyit.maid_storage_manager.craft.autogen.IAutoCraftGuideGenerator;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

import java.util.ArrayList;
import java.util.List;

public class AutoGraphGenerator {
    private final EntityMaid maid;
    protected final List<IAutoCraftGuideGenerator> iAutoCraftGuideGenerators;
    protected final List<CraftGuideData> craftGuideData;
    protected final List<InventoryItem> inventory;
    protected final MaidPathFindingBFS pathfindingBFS;
    private final List<BlockPos> blockPosList;
    protected int index = 0;
    protected int craftGeneratorTypeIndex = 0;
    protected double currentMinDistance = Double.MAX_VALUE;
    protected BlockPos minPos;

    public AutoGraphGenerator(EntityMaid maid) {
        this.maid = maid;
        this.craftGuideData = new ArrayList<>();
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
        iAutoCraftGuideGenerators = CraftManager.getInstance().getAutoCraftGuideGenerators();
        blockPosList = BlockPos
                .betweenClosedStream(new AABB(center).inflate(distance, 6, distance))
                .filter(p -> pathfindingBFS.canPathReach(p) && maid.isWithinRestriction(p))
                .toList();
    }

    public boolean process() {
        int count = 0;
        if (craftGeneratorTypeIndex >= iAutoCraftGuideGenerators.size()) return true;
        IAutoCraftGuideGenerator generator = iAutoCraftGuideGenerators.get(craftGeneratorTypeIndex);
        while (index < blockPosList.size()) {
            BlockPos next = blockPosList.get(index++);
            if (generator.isBlockValid(maid.level(), next)) {
                double distance = maid.distanceToSqr(next.getCenter());
                if (distance < currentMinDistance) {
                    currentMinDistance = distance;
                    minPos = next;
                }
            }
            if (++count > 50)
                return false;
        }
        if (minPos != null) {
            craftGuideData.addAll(generator.generate(inventory, maid.level(), minPos));
        }
        minPos = null;
        currentMinDistance = Double.MAX_VALUE;
        index = 0;
        craftGeneratorTypeIndex++;
        return craftGeneratorTypeIndex >= iAutoCraftGuideGenerators.size();
    }

    public List<CraftGuideData> getCraftGuideData() {
        return craftGuideData;
    }

    public int getDone() {
        return index;
    }

    public int getTotal() {
        return blockPosList.size();
    }

    public float getProgress() {
        return (float) index / blockPosList.size();
    }
}
