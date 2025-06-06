package studio.fantasyit.maid_storage_manager.storage.base;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import studio.fantasyit.maid_storage_manager.storage.Target;

public interface IStorageContext {
    void start(EntityMaid maid, ServerLevel level, Target target);

    default void finish() {
    }

    default boolean isDone() {
        return false;
    }

    default void reset() {
    }
}
