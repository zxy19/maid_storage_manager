package studio.fantasyit.maid_storage_manager.api.communicate.context;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;

public interface ICommunicateContext {
    void start(EntityMaid maid, EntityMaid toCommunicate);
    boolean isFinished(EntityMaid maid, EntityMaid toCommunicate);
}
