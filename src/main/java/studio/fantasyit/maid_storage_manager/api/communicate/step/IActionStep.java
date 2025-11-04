package studio.fantasyit.maid_storage_manager.api.communicate.step;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public interface IActionStep {
    /**
     * 用于方案检查，在方案被用作请求之前检查
     * @param checks 已经进行的检查结果，可以用于避免重复运算
     * @param wisher 请求者
     * @param handler 被请求者
     * @return 是否可用
     */
    default boolean isAvailable(Map<ResourceLocation, Boolean> checks, EntityMaid wisher, EntityMaid handler) {
        return true;
    }

    /**
     * 是否应该进行准备。一般的，如果之前没有准备过，那么这时候应该进行一次准备。请注意，该函数会被用作PrepareBehavior的启动条件，请注意耗时
     * @param wisher 请求者
     * @param handler 被请求者
     * @param prepared 是否已经准备过了
     * @return 是否应该进行准备
     */
    default boolean shouldRunPrepare(EntityMaid wisher, EntityMaid handler, boolean prepared) {
        return !prepared;
    }
    /**
     * 准备
     * @param wisher 请求者
     * @param handler 被请求者
     * @return 是否成功
     */
    default boolean prepare(EntityMaid wisher, EntityMaid handler) {
        return true;
    }

    /**
     * 准备是否完成，如果准备完成，则进行work流程。请注意，该函数会被用作WorkBehavior的结束条件，请注意耗时
     * @param wisher 请求者
     * @param handler 被请求者
     * @return 是否完成
     */
    default boolean isPrepareDone(EntityMaid wisher, EntityMaid handler) {
        return true;
    }

    /**
     * work开始。如果返回不是CONTINUE则不会进行tick。
     * @param wisher 请求者
     * @param handler 被请求者
     * @return 结果
     */
    ActionResult start(EntityMaid wisher, EntityMaid handler);

    /**
     * work进行中
     * @param wisher 请求者
     * @param handler 被请求者
     * @return 结果
     */
    default ActionResult tick(EntityMaid wisher, EntityMaid handler) {
        return ActionResult.FAIL;
    }

    /**
     * work结束
     * @param wisher 请求者
     * @param handler 被请求者
     */
    default void stop(EntityMaid wisher, EntityMaid handler) {
    }
}
