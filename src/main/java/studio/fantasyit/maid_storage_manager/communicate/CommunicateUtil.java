package studio.fantasyit.maid_storage_manager.communicate;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.entity.EntityTypeTest;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.api.communicate.ICommunicatable;
import studio.fantasyit.maid_storage_manager.api.communicate.data.CommunicateHolder;
import studio.fantasyit.maid_storage_manager.api.communicate.data.CommunicatePlan;
import studio.fantasyit.maid_storage_manager.api.communicate.data.CommunicateRequest;
import studio.fantasyit.maid_storage_manager.api.communicate.data.CommunicateWish;
import studio.fantasyit.maid_storage_manager.api.communicate.wish.IActionWish;
import studio.fantasyit.maid_storage_manager.registry.MemoryModuleRegistry;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public class CommunicateUtil {
    public static Optional<CommunicatePlan> sendCommunicateWishAndGetPlan(EntityMaid wisher, CommunicateWish wish, Predicate<CommunicatePlan> planPredicate) {
        List<ResourceLocation> requestTypes = wish.wishes().stream().map(IActionWish::getType).toList();
        List<EntityMaid> maids = wisher.level().getEntities(
                EntityTypeTest.forClass(EntityMaid.class),
                wisher.getBoundingBox().inflate(32.0D, 32.0D, 32.0D),
                entity ->
                        entity != wisher &&
                                entity.getTask() instanceof ICommunicatable communicatable &&
                                communicatable.getAcceptedWishTypes().containsAll(requestTypes)
        );
        for (EntityMaid maid : maids) {
            ICommunicatable communicateHandler = (ICommunicatable) maid.getTask();
            CommunicatePlan actionPlan = communicateHandler.acceptCommunicateWish(maid, wish);
            if (actionPlan != null) {
                if (planPredicate.test(actionPlan)) {
                    return Optional.of(actionPlan);
                }
            }
        }
        return Optional.empty();
    }

    public static boolean hasCommunicateHolder(EntityMaid maid) {
        return maid.getBrain().hasMemoryValue(MemoryModuleRegistry.COMMUNICATE_HOLDER.get());
    }

    public static boolean hasCommunicateRequest(EntityMaid maid) {
        return maid.getBrain().hasMemoryValue(MemoryModuleRegistry.COMMUNICATE_REQUEST.get());
    }

    public static CommunicateHolder getCommunicateHolder(EntityMaid maid) {
        return maid.getBrain().getMemory(MemoryModuleRegistry.COMMUNICATE_HOLDER.get()).orElse(null);
    }

    public static CommunicateRequest getCommunicateRequest(EntityMaid maid) {
        return maid.getBrain().getMemory(MemoryModuleRegistry.COMMUNICATE_REQUEST.get()).orElse(null);
    }

    public static void clearHolder(EntityMaid maid) {
        maid.getBrain().eraseMemory(MemoryModuleRegistry.COMMUNICATE_HOLDER.get());
    }

    public static boolean hasLastResult(EntityMaid maid) {
        return maid.getBrain().hasMemoryValue(MemoryModuleRegistry.COMMUNICATE_LAST_RESULT.get());
    }

    public static Pair<UUID, Boolean> getLastResult(EntityMaid maid) {
        return maid.getBrain().getMemory(MemoryModuleRegistry.COMMUNICATE_LAST_RESULT.get()).orElse(null);
    }

    public static void setLastResult(EntityMaid maid, UUID uuid, boolean result) {
        maid.getBrain().setMemory(MemoryModuleRegistry.COMMUNICATE_LAST_RESULT.get(), new Pair<>(uuid, result));
    }

    public static void clearLastResult(EntityMaid maid) {
        maid.getBrain().eraseMemory(MemoryModuleRegistry.COMMUNICATE_LAST_RESULT.get());
    }
}
