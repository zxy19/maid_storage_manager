package studio.fantasyit.maid_storage_manager.api.communicate.data;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import studio.fantasyit.maid_storage_manager.communicate.CommunicateUtil;

import java.util.UUID;

public record CommunicateHolder(
        UUID requestId,
        EntityMaid handler
) {
    public boolean isValid() {
        if (!handler.isAlive())
            return false;
        if (!CommunicateUtil.hasCommunicateRequest(handler))
            return false;
        return CommunicateUtil.getCommunicateRequest(handler).requestId().equals(requestId);
    }
}
