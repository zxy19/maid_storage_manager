package studio.fantasyit.maid_storage_manager.storage.base;

import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;

import java.util.List;

/**
 * 合成指南提供者
 */
public interface IStorageCraftDataProvider {
    /**
     * 获取合成指南对象
     * @return 合成指南对象列表
     */
    List<CraftGuideData> getCraftGuideData();
}
