package studio.fantasyit.maid_storage_manager.storage.ItemHandler;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import studio.fantasyit.maid_storage_manager.items.FilterListItem;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.storage.Storage;
import studio.fantasyit.maid_storage_manager.storage.base.IFilterable;
import studio.fantasyit.maid_storage_manager.util.InvUtil;

import java.util.ArrayList;
import java.util.List;

public class FilterableItemHandler implements IFilterable {
    protected Storage storage;

    public FilterableItemHandler(Storage storage) {
        this.storage = storage;
    }

    List<Pair<ItemStack, Boolean>> filtered;
    boolean isBlackMode;
    boolean requestOnly;

    public void init(ServerLevel level, Storage target) {
        List<BlockPos> samePos = new ArrayList<>(List.of(target.pos));
        InvUtil.checkNearByContainers(level, target.pos, samePos::add);
        AABB aabb = AABB.ofSize(target.pos.getCenter(), 5, 5, 5);
        List<ItemFrame> frames = level.getEntities(
                EntityTypeTest.forClass(ItemFrame.class),
                aabb,
                itemFrame -> {
                    if (storage.side != null && storage.side != itemFrame.getDirection()) return false;
                    BlockPos relative = itemFrame.blockPosition().relative(itemFrame.getDirection(), -1);
                    return samePos.stream().anyMatch(t -> t.equals(relative));
                }
        );
        if (frames.isEmpty()) {
            filtered = new ArrayList<>();
            isBlackMode = true;
        } else {
            this.requestOnly = frames
                    .stream()
                    .map(ItemFrame::getItem)
                    .anyMatch(t -> t.is(ItemRegistry.NO_ACCESS.get()));
            List<CompoundTag> items = frames
                    .stream()
                    .map(ItemFrame::getItem)
                    .filter(t -> t.is(ItemRegistry.FILTER_LIST.get()))
                    .map(ItemStack::getOrCreateTag).toList();
            isBlackMode = items.stream().allMatch(t -> t.getBoolean(FilterListItem.TAG_BLACK_MODE));
            filtered = new ArrayList<>();
            items
                    .stream()
                    .filter(t -> !t.getBoolean(FilterListItem.TAG_BLACK_MODE))
                    .forEach(t -> {
                        ListTag list = t.getList(FilterListItem.TAG_ITEMS, ListTag.TAG_COMPOUND);
                        for (int i = 0; i < list.size(); i++) {
                            CompoundTag tmp = list.getCompound(i);
                            ItemStack item = ItemStack.of(tmp.getCompound(FilterListItem.TAG_ITEMS_ITEM));
                            filtered.add(new Pair<>(item, t.getBoolean(FilterListItem.TAG_MATCH_TAG)));
                        }
                    });
            items
                    .stream()
                    .filter(t -> t.getBoolean(FilterListItem.TAG_BLACK_MODE))
                    .forEach(t -> {
                        ListTag list = t.getList(FilterListItem.TAG_ITEMS, ListTag.TAG_COMPOUND);
                        for (int i = 0; i < list.size(); i++) {
                            CompoundTag tmp = list.getCompound(i);
                            ItemStack item = ItemStack.of(tmp.getCompound(FilterListItem.TAG_ITEMS_ITEM));
                            if (isBlackMode)
                                filtered.add(new Pair<>(item, t.getBoolean(FilterListItem.TAG_MATCH_TAG)));
                            else {
                                //白名单模式下，黑名单列表的合并方式：移除撞车的
                                for (int j = 0; j < filtered.size(); j++) {
                                    Pair<ItemStack, Boolean> pair = filtered.get(j);
                                    if (ItemStack.isSameItem(pair.getFirst(), item)) {
                                        if (!tmp.getBoolean(FilterListItem.TAG_MATCH_TAG) || !pair.getSecond() || ItemStack.isSameItemSameTags(pair.getFirst(), item)) {
                                            filtered.remove(pair);
                                            j--;
                                        }
                                    }
                                }
                            }
                        }
                    });

        }
    }

    @Override
    public boolean isAvailable(ItemStack itemStack) {
        for (Pair<ItemStack, Boolean> pair : filtered) {
            if (ItemStack.isSameItem(pair.getFirst(), itemStack)) {
                if (!pair.getSecond() || ItemStack.isSameItemSameTags(pair.getFirst(), itemStack))
                    return !isBlackMode;
            }
        }
        return isBlackMode;
    }

    @Override
    public boolean isWhitelist() {
        return !isBlackMode;
    }

    @Override
    public boolean isRequestOnly() {
        return requestOnly;
    }
}
