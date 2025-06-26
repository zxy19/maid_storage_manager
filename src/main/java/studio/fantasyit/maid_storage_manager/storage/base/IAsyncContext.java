package studio.fantasyit.maid_storage_manager.storage.base;

/**
 * 异步存储上下文
 * @param <T>
 */
public interface IAsyncContext<T> extends IStorageContext {
    /**
     * 当前是否有任务
     * @return 是否有任务
     */
    boolean hasTask();

    /**
     * 清空任务
     */
    void clearTask();

    /**
     * 任务tick
     * @param callback 找到物品回调
     */
    void tick(T callback);
}
