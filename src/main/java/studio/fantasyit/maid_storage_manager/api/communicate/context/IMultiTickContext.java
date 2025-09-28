package studio.fantasyit.maid_storage_manager.api.communicate.context;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;

public interface IMultiTickContext extends ICommunicateContext {
    boolean tick(EntityMaid maid, EntityMaid toCommunicate);
    void stop(EntityMaid maid, EntityMaid toCommunicate);
}
