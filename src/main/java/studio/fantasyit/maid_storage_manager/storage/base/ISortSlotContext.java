package studio.fantasyit.maid_storage_manager.storage.base;

public interface ISortSlotContext extends IStorageContext {
    void startSorting();

    void tickSorting();

    boolean isDoneSorting();
}
