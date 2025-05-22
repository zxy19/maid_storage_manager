package studio.fantasyit.maid_storage_manager.storage.base;

public interface IAsyncContext<T> extends IStorageContext {
    boolean hasTask();
    void clearTask();
    void tick(T callback);
}
