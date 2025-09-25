package studio.fantasyit.maid_storage_manager.api.interact;

public interface IItemStorer {
    boolean hasItemToStore();
    boolean tickStoreItem();
}
