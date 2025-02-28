package studio.fantasyit.maid_storage_manager.storage.base;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

import java.util.function.Function;

public interface IStorageContext {
    void start(EntityMaid maid, ServerLevel level, BlockPos target);

    default void finish() {
    }

    default boolean isDone() {
        return false;
    }
}
