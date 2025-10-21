package studio.fantasyit.maid_storage_manager.api.communicate.data;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;

import java.util.UUID;

public record CommunicateHolder(
        UUID requestId,
        EntityMaid handler
) {

}
