package studio.fantasyit.maid_storage_manager.storage.qio;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.lib.inventory.HashedItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageExtractableContext;
import studio.fantasyit.maid_storage_manager.util.InvUtil;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class QIOCollectContext extends QIOBaseContext implements IStorageExtractableContext {
    private List<ItemStack> list;
    private List<ItemStack> toExtract;
    private int index;
    private boolean matchNbt;
    EntityMaid maid;

    @Override
    public void start(EntityMaid maid, ServerLevel level, Target target) {
        super.start(maid, level, target);
        this.maid = maid;
        if (frequency != null) {
            Map<HashedItem, QIOFrequency.QIOItemTypeData> data = frequency.getItemDataMap();
            list = data.entrySet()
                    .stream()
                    .map(e -> e.getKey().getInternalStack().copyWithCount((int) (e.getValue().getCount() > 1e9 ? 1e9 : e.getValue().getCount())))
                    .toList();
        }
    }

    @Override
    public boolean isDone() {
        return toExtract != null && index >= toExtract.size();
    }

    @Override
    public boolean hasTask() {
        return toExtract != null;
    }

    @Override
    public void clearTask() {
        toExtract = null;
    }

    @Override
    public void tick(Function<ItemStack, ItemStack> process) {
        if (toExtract == null || index >= toExtract.size())
            return;
        ItemStack item = toExtract.get(index++);
        ItemStack itemStack = frequency.removeItem(item, item.getCount());
        ItemStack remain = process.apply(itemStack);
        if (!remain.isEmpty()) {
            ItemStack remain2 = frequency.addItem(remain);
            if (remain2.isEmpty())
                InvUtil.throwItem(maid, remain2);
        }
    }

    @Override
    public void setExtract(List<ItemStack> itemList, boolean matchNbt) {
        toExtract = itemList;
        index = 0;
        this.matchNbt = matchNbt;
    }

    @Override
    public void setExtractByExisting(Predicate<ItemStack> predicate) {
        setExtract(
                list.stream()
                        .filter(predicate)
                        .toList(), true);
    }
}