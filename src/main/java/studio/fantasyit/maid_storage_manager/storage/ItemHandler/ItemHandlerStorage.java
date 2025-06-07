package studio.fantasyit.maid_storage_manager.storage.ItemHandler;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.storage.base.AbstractFilterableBlockStorage;
import studio.fantasyit.maid_storage_manager.storage.base.IMaidStorage;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageContext;

public class ItemHandlerStorage implements IMaidStorage {
    public static final ResourceLocation TYPE = new ResourceLocation(MaidStorageManager.MODID, "item_handler");

    @Override
    public ResourceLocation getType() {
        return TYPE;
    }

    @Override
    public boolean isValidTarget(ServerLevel level, LivingEntity maid, BlockPos block, @Nullable Direction side) {
        BlockEntity blockEntity = level.getBlockEntity(block);
        if (blockEntity == null) return false;
        @NotNull LazyOptional<IItemHandler> cap;
        if (side == null) {
            cap = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER);
        } else {
            cap = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, side);
        }
        return cap.isPresent();
    }

    @Override
    public @Nullable IStorageContext onStartCollect(ServerLevel level, EntityMaid maid, Target storage) {
        return new ContextItemHandlerCollect();
    }

    @Override
    public @Nullable IStorageContext onStartPlace(ServerLevel level, EntityMaid maid, Target storage) {
        return new ContextItemHandlerStore();
    }

    @Override
    public @Nullable IStorageContext onStartView(ServerLevel level, EntityMaid maid, Target storage) {
        return new ContextItemHandlerView();
    }

    @Override
    public @Nullable IStorageContext onPreviewFilter(ServerLevel level, EntityMaid maid, Target storage) {
        return new AbstractFilterableBlockStorage();
    }
}
