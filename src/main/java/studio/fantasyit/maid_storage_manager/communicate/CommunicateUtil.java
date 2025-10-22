package studio.fantasyit.maid_storage_manager.communicate;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.entity.EntityTypeTest;
import studio.fantasyit.maid_storage_manager.api.communicate.ICommunicatable;
import studio.fantasyit.maid_storage_manager.api.communicate.data.CommunicatePlan;
import studio.fantasyit.maid_storage_manager.api.communicate.data.CommunicateWish;
import studio.fantasyit.maid_storage_manager.api.communicate.wish.IActionWish;

import java.util.List;
import java.util.Optional;
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
}
