package studio.fantasyit.maid_storage_manager.storage.base;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import studio.fantasyit.maid_storage_manager.storage.Storage;

public interface IStorageContext {
    void start(EntityMaid maid, ServerLevel level, Storage target);

    default void finish() {
    }

    default boolean isDone() {
        return false;
    }
}
