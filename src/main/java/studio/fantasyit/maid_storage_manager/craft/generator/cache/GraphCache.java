package studio.fantasyit.maid_storage_manager.craft.generator.cache;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.craft.generator.algo.GeneratorGraph;
import studio.fantasyit.maid_storage_manager.craft.generator.type.base.IAutoCraftGuideGenerator;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GraphCache {
    public record CacheRecord(UUID maid, BlockPos restrictCenter, Map<ResourceLocation, List<BlockPos>> targets,
                              GeneratorGraph graph) {
    }

    public static final Map<UUID, CacheRecord> CACHE = new java.util.concurrent.ConcurrentHashMap<>();

    public static @Nullable CacheRecord get(UUID maid) {
        return CACHE.get(maid);
    }

    public static @Nullable CacheRecord getAndValidate(Level level, EntityMaid maid, List<IAutoCraftGuideGenerator> iAutoCraftGuideGenerators) {
        CacheRecord cacheRecord = get(maid.getUUID());
        if (cacheRecord == null) return null;
        if (cacheRecord.restrictCenter == null) {
            if (maid.getRestrictCenter() != null)
                return null;
        } else if (!cacheRecord.restrictCenter.equals(maid.getRestrictCenter())) {
            return null;
        }
        for (IAutoCraftGuideGenerator iAutoCraftGuideGenerator : iAutoCraftGuideGenerators) {
            if (cacheRecord.targets.containsKey(iAutoCraftGuideGenerator.getType()) && iAutoCraftGuideGenerator.canCacheGraph()) {
                if (!cacheRecord.targets.get(iAutoCraftGuideGenerator.getType()).stream().allMatch(blockPos -> iAutoCraftGuideGenerator.isBlockValid(level, blockPos))) {
                    return null;
                }
            }
        }
        cacheRecord.graph.clearStates();
        for (IAutoCraftGuideGenerator iAutoCraftGuideGenerator : iAutoCraftGuideGenerators) {
            if (!iAutoCraftGuideGenerator.canCacheGraph()) {
                cacheRecord.graph.invalidAllCraftWithType(iAutoCraftGuideGenerator.getType());
                cacheRecord.targets.remove(iAutoCraftGuideGenerator.getType());
            }
        }
        return cacheRecord;
    }

    public static void putCache(EntityMaid maid, Map<ResourceLocation, List<BlockPos>> targets, GeneratorGraph generatorGraph) {
        CacheRecord cacheRecord = new CacheRecord(maid.getUUID(), maid.getRestrictCenter(), targets, generatorGraph);
        CACHE.put(maid.getUUID(), cacheRecord);
    }

    public static void put(UUID maid, CacheRecord record) {
        CACHE.put(maid, record);
    }
    public static void invalidateAll(){
        CACHE.clear();
    }
}
