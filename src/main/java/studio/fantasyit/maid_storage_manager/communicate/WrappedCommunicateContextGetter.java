package studio.fantasyit.maid_storage_manager.communicate;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.api.communicate.ICommunicatable;
import studio.fantasyit.maid_storage_manager.api.communicate.context.ICommunicateContext;

public class WrappedCommunicateContextGetter {
    private final ICommunicatable communicatable;
    private final @Nullable ItemStack stack;

    public WrappedCommunicateContextGetter(ICommunicatable communicatable, @Nullable ItemStack stack) {
        this.communicatable = communicatable;
        this.stack = stack;
    }
    public @Nullable ICommunicateContext get(EntityMaid maid, EntityMaid handler) {
        return communicatable.startCommunicate(maid, stack, handler);
    }
}
