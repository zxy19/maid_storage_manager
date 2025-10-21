package studio.fantasyit.maid_storage_manager.api.communicate;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.api.communicate.data.CommunicatePlan;
import studio.fantasyit.maid_storage_manager.api.communicate.data.CommunicateRequest;
import studio.fantasyit.maid_storage_manager.api.communicate.data.CommunicateWish;

import java.util.Set;

public interface ICommunicatable {
    Set<ResourceLocation> getAcceptedWishTypes();

    @Nullable CommunicatePlan acceptCommunicateWish(EntityMaid handler, CommunicateWish wish);

    boolean startCommunicate(CommunicateRequest plan);

    CommunicateRequest getCurrentCommunicateRequest(EntityMaid handler);
}
