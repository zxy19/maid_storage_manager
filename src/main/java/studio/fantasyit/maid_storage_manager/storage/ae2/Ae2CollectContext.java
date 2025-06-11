package studio.fantasyit.maid_storage_manager.storage.ae2;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageExtractableContext;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class Ae2CollectContext extends Ae2BaseContext implements IStorageExtractableContext {
    private int current = 0;
    private List<AEItemKey> keys;

    @Override
    public void start(EntityMaid maid, ServerLevel level, Target target) {
        if (this.init(maid, level, target))
            this.keys = inv
                    .getAvailableStacks()
                    .keySet()
                    .stream()
                    .filter(key -> key instanceof AEItemKey)
                    .map(key -> (AEItemKey) key)
                    .toList();
    }

    List<ItemStack> itemList = null;
    boolean matchNbt = false;
    boolean lastDone = false;

    @Override
    public void setExtract(List<ItemStack> itemList, boolean matchNbt) {
        this.itemList = itemList;
        this.matchNbt = matchNbt;
        this.current = 0;
        setDone(false);
        lastDone = false;
    }

    @Override
    public void setExtractByExisting(Predicate<ItemStack> predicate) {
        if (this.keys != null)
            setExtract(
                    keys.stream()
                            .map(AEItemKey::getReadOnlyStack)
                            .filter(predicate)
                            .toList(), true);
        else
            setExtract(List.of(), true);
    }

    @Override
    public void reset() {
        current = 0;
        setDone(false);
    }


    @Override
    public boolean hasTask() {
        return itemList != null;
    }

    @Override
    public void clearTask() {
        itemList = null;
    }

    @Override
    public void tick(Function<ItemStack, ItemStack> process) {
        if (inv == null) return;
        for (; current < itemList.size(); current++) {
            ItemStack item = itemList.get(current);
            if (item.isEmpty()) continue;
            AEItemKey keyTmp = AEItemKey.of(item);
            List<AEItemKey> filteredKey = keyTmp == null ? List.of() : List.of(keyTmp);
            if (!matchNbt) {
                filteredKey = keys.stream()
                        .filter(aeItemKey -> aeItemKey.getItem() == item.getItem())
                        .toList();
            }
            int totalRestCount = item.getCount();
            for (AEItemKey key : filteredKey) {
                long extract = inv.extract(key, totalRestCount, Actionable.SIMULATE, IActionSource.empty());
                if (extract == 0) continue;
                while (extract > 0) {
                    int scheduled = (int) Math.min(extract, item.getMaxStackSize());
                    ItemStack tmp = key
                            .getReadOnlyStack()
                            .copyWithCount(scheduled);
                    ItemStack apply = process.apply(tmp);
                    int costed = scheduled - apply.getCount();
                    if (costed != 0) {
                        inv.extract(key, costed, Actionable.MODULATE, IActionSource.empty());
                        extract -= costed;
                        totalRestCount -= costed;
                    } else break;
                }
                if (totalRestCount <= 0)
                    break;
            }
        }
        lastDone = true;
        setDone(true);
    }
}
