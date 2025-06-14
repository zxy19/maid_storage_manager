package studio.fantasyit.maid_storage_manager.storage.ae2;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageInsertableContext;

public class Ae2PlacingContext extends Ae2BaseContext implements IStorageInsertableContext {

    @Override
    public void start(EntityMaid maid, ServerLevel level, Target target) {
        super.start(maid, level, target);
        this.init(maid, level, target);
    }

    @Override
    public ItemStack insert(ItemStack item) {
        if (inv == null) return item;
        AEItemKey key = AEItemKey.of(item);
        if (key == null) return item;
        long insert = this.inv.insert(key, item.getCount(), Actionable.MODULATE, IActionSource.empty());
        ItemStack result = item.copy();
        result.shrink((int) insert);
        return result;
    }
}
