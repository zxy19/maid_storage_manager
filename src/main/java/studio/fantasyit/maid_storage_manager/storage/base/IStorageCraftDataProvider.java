package studio.fantasyit.maid_storage_manager.storage.base;

import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;

import java.util.List;

public interface IStorageCraftDataProvider {
    List<CraftGuideData> getCraftGuideData();
}
