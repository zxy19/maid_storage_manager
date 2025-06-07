package studio.fantasyit.maid_storage_manager.storage.base;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.items.FilterListItem;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.InvUtil;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;

public class AbstractFilterableBlockStorage implements IFilterable, IStorageContext {
    List<Pair<ItemStack, Boolean>> filtered;
    boolean isBlackMode;

    @Override
    public void start(EntityMaid maid, ServerLevel level, Target target) {
        init(level, target);
    }
    public void init(ServerLevel level, Target target) {
        List<Pair<Target, ItemStack>> marksWithSameContainer = InvUtil.getMarksWithSameContainer(level, target);
        if (marksWithSameContainer.isEmpty()) {
            filtered = new ArrayList<>();
            isBlackMode = true;
        } else {
            List<CompoundTag> items = marksWithSameContainer
                    .stream()
                    .map(Pair::getB)
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
                                    if (ItemStackUtil.isSame(pair.getA(), item, tmp.getBoolean(FilterListItem.TAG_MATCH_TAG) && pair.getB())) {
                                        filtered.remove(pair);
                                        j--;
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
            if (ItemStackUtil.isSame(pair.getA(), itemStack, pair.getB())) {
                return !isBlackMode;
            }
        }
        return isBlackMode;
    }

    @Override
    public boolean isWhitelist() {
        return !isBlackMode;
    }
}
