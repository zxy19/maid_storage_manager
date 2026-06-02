package studio.fantasyit.maid_storage_manager.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.transfer.CombinedResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.items.data.RequestItemStackList;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.List;
import java.util.UUID;

public interface IRequestTaskHandler {

    ItemCapability<IRequestTaskHandler, @Nullable Void> CAPABILITY =
            ItemCapability.createVoid(
                    Identifier.fromNamespaceAndPath(MaidStorageManager.MODID, "request_task_handler"),
                    IRequestTaskHandler.class
            );

    static @Nullable IRequestTaskHandler of(ItemStack stack) {
        return stack.getCapability(CAPABILITY);
    }

    static boolean is(ItemStack stack) {
        return of(stack) != null;
    }

    // --- 基础数据访问 ---
    @NotNull RequestItemStackList.Immutable getImmutableRequestData(ItemStack stack);

    @NotNull RequestItemStackList getMutableRequestData(ItemStack stack);

    // --- 匹配模式 ---
    ItemStackUtil.MATCH_TYPE getMatchType(ItemStack stack);

    ItemStackUtil.MATCH_TYPE getMatchType(ItemStack stack, boolean crafting);

    // --- 忽略/CD/重复 ---
    boolean isIgnored(ItemStack stack);

    void setIgnore(ItemStack stack);

    int getRepeatInterval(ItemStack stack);

    int getRepeatCd(ItemStack stack);

    boolean isCoolingDown(ItemStack stack);

    void tickCoolingDown(ItemStack stack);

    // --- 进度操作 ---
    void addItemStackCollected(ItemStack stack, ItemStack item, int count);

    void clearItemProcess(ItemStack stack);

    void clearAllNonSuccess(ItemStack stack);

    void markDone(ItemStack stack, ItemStack target);

    void markAllDone(ItemStack stack);

    ItemStack updateCollectedItem(ItemStack stack, ItemStack collected, int maxCollect, boolean isInCrafting);

    int updateStored(ItemStack stack, ItemStack toStore, boolean simulate, boolean isInCrafting);

    void updateCollectedNotStored(ItemStack stack, CombinedResourceHandler<ItemResource> tmpStorage);

    void setFailAddition(ItemStack stack, ItemStack item, String failAddition);

    // --- 合成相关 ---
    void setMissingItem(ItemStack stack, ItemStack item, List<ItemStack> missing);

    // --- 状态查询 ---
    boolean isAllSuccess(ItemStack stack);

    boolean isAllStored(ItemStack stack);

    List<Pair<ItemStack, Integer>> getItemStacksNotDone(ItemStack stack);

    List<Pair<ItemStack, Integer>> getItemStacksNotDone(ItemStack stack, boolean includingNoRequest);

    // --- 存储目标 ---
    @Nullable UUID getStorageEntity(ItemStack stack);

    @Nullable Target getStorageBlock(ItemStack stack);

    boolean hasAnyStorage(ItemStack stack);

    // --- 工作UUID ---
    @NotNull UUID getWorkUUID(ItemStack stack);

    // --- 存量模式 ---
    boolean isStockMode(ItemStack stack);

    boolean hasCheckedStock(ItemStack stack);

    void setHasCheckedStock(ItemStack stack, boolean has);

    // --- 虚拟请求 ---
    boolean isVirtual(ItemStack stack);

    // --- 黑名单模式 ---
    boolean isBlackMode(ItemStack stack);

    boolean isBlackModeDone(ItemStack stack);

    // --- 虚拟数据 ---
    void setVirtualData(ItemStack stack, CompoundTag data);

    CompoundTag getVirtualData(ItemStack stack);
}
