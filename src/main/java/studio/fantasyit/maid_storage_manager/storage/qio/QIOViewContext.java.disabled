package studio.fantasyit.maid_storage_manager.storage.qio;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.lib.inventory.HashedItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageInteractContext;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class QIOViewContext extends QIOBaseContext implements IStorageInteractContext {
    private List<ItemStack> list;
    private int index;

    @Override
    public void start(EntityMaid maid, ServerLevel level, Target target) {
        super.start(maid, level, target);
        if (frequency != null) {
            Map<HashedItem, QIOFrequency.QIOItemTypeData> data = frequency.getItemDataMap();
            list = data.entrySet()
                    .stream()
                    .map(e -> e.getKey().getInternalStack().copyWithCount((int) (e.getValue().getCount() > 1e9 ? 1e9 : e.getValue().getCount())))
                    .toList();
        }
    }

    @Override
    public void tick(Function<ItemStack, ItemStack> process) {
        for (int i = 0; i < 20; i++) {
            if (index >= list.size()) return;
            process.apply(list.get(index));
            index++;
        }
    }

    @Override
    public boolean isDone() {
        return list == null || index >= list.size();
    }
}
