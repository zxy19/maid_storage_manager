package studio.fantasyit.maid_storage_manager.api.communicate.context;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;

public interface IDelayCompleteContext extends ICommunicateContext {
    void complete(EntityMaid wisher, EntityMaid handler);
}
