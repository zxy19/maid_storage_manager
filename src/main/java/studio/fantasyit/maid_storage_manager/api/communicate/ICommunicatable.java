package studio.fantasyit.maid_storage_manager.api.communicate;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.api.communicate.context.ICommunicateContext;

public interface ICommunicatable {
    boolean willingCommunicate(EntityMaid maid);

    @Nullable ICommunicateContext startCommunicate(EntityMaid maid);
}
