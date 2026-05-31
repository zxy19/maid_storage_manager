package studio.fantasyit.maid_storage_manager.storage.ItemHandler;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.storage.base.AbstractFilterableBlockStorage;
import studio.fantasyit.maid_storage_manager.storage.base.IMaidStorage;
import studio.fantasyit.maid_storage_manager.storage.base.ISortSlotContext;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageContext;

public class ItemHandlerStorage implements IMaidStorage {
    public static final Identifier TYPE = Identifier.fromNamespaceAndPath(MaidStorageManager.MODID, "item_handler");

    @Override
    public Identifier getType() {
        return TYPE;
    }

    @Override
    public boolean isValidTarget(ServerLevel level, LivingEntity maid, BlockPos block, @Nullable Direction side, BlockState blockState, BlockEntity blockEntity) {
        if (blockEntity == null) return false;
        ResourceHandler<ItemResource> cap;
        if (side == null) {
            cap = level.getCapability(Capabilities.Item.BLOCK, block, null);
        } else {
            cap = level.getCapability(Capabilities.Item.BLOCK, block, side);
        }
        return cap != null;
    }

    @Override
    public @Nullable IStorageContext onStartCollect(ServerLevel level, EntityMaid maid, Target storage) {
        // ContextItemHandlerCollect disabled
        return null;
    }

    @Override
    public @Nullable IStorageContext onStartPlace(ServerLevel level, EntityMaid maid, Target storage) {
        // ContextItemHandlerStore disabled
        return null;
    }

    @Override
    public @Nullable IStorageContext onStartView(ServerLevel level, EntityMaid maid, Target storage) {
        // ContextItemHandlerView disabled
        return null;
    }

    @Override
    public @Nullable IStorageContext onPreviewFilter(ServerLevel level, EntityMaid maid, Target storage) {
        return new AbstractFilterableBlockStorage();
    }

    public static TagKey<Block> SORTABLE = TagKey.create(BuiltInRegistries.BLOCK.key(), Identifier.fromNamespaceAndPath(MaidStorageManager.MODID, "sortable_chests"));
    @Override
    public @Nullable ISortSlotContext onStartSorting(ServerLevel level, EntityMaid maid, Target target) {
        // AbstractItemHandlerContext disabled
        // if(level.getBlockState(target.getPos()).is(SORTABLE))
        //     return new AbstractItemHandlerContext();
        return null;
    }
}
