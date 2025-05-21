package studio.fantasyit.maid_storage_manager.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.registry.EntityRegistry;

public class VirtualDisplayEntity extends ItemFrame {

    public VirtualDisplayEntity(EntityType<VirtualDisplayEntity> entityEntityType, Level level) {
        super(entityEntityType, level);
        this.setInvisible(!Config.usingVisibleFrame);
    }

    public static VirtualDisplayEntity create(Level level, BlockPos blockpos, Direction direction, ItemStack itemstack) {
        VirtualDisplayEntity virtualDisplayEntity = new VirtualDisplayEntity(EntityRegistry.VIRTUAL_DISPLAY_ENTITY.get(), level);
        virtualDisplayEntity.setItem(itemstack);
        virtualDisplayEntity.setInvisible(!Config.usingVisibleFrame);
        virtualDisplayEntity.pos = blockpos;
        virtualDisplayEntity.setDirection(direction);
        return virtualDisplayEntity;
    }

    @Override
    public boolean hurt(DamageSource p_31776_, float p_31777_) {
        if (!this.isRemoved() && !this.level().isClientSide) {
            this.kill();
            this.markHurt();
            this.dropItem(p_31776_.getEntity());
        }
        return true;
    }

    @Override
    protected ItemStack getFrameItemStack() {
        return ItemStack.EMPTY;
    }

    @Override
    public void load(CompoundTag p_20259_) {
        super.load(p_20259_);
        this.setInvisible(!Config.usingVisibleFrame);
    }
}
