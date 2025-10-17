package studio.fantasyit.maid_storage_manager.api.communicate.context;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;

public interface IMultiTickContext extends ICommunicateContext {
    boolean tick(EntityMaid wisher, EntityMaid handler);
    void stop(EntityMaid wisher, EntityMaid handler);
}
