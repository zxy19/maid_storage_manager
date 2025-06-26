package studio.fantasyit.maid_storage_manager.storage.base;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import studio.fantasyit.maid_storage_manager.storage.Target;

/**
 * 存储上下文
 */
public interface IStorageContext {
    /**
     * 开始
     *
     * @param maid   女仆
     * @param level  世界
     * @param target 目标
     */
    void start(EntityMaid maid, ServerLevel level, Target target);

    /**
     * 结束
     */
    default void finish() {
    }

    /**
     * 是否完成
     *
     * @return 是否完成
     */
    default boolean isDone() {
        return false;
    }

    /**
     * 重置
     */
    default void reset() {
    }
}
