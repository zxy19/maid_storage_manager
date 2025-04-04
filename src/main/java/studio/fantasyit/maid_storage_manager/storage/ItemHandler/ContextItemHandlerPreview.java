package studio.fantasyit.maid_storage_manager.storage.ItemHandler;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageContext;

public class ContextItemHandlerPreview extends FilterableItemHandler implements IStorageContext {

    public ContextItemHandlerPreview(Target storage) {
        super(storage);
    }

    @Override
    public void start(EntityMaid maid, ServerLevel level, Target target) {
        super.init(level, target);
    }
}
