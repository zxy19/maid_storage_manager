package studio.fantasyit.maid_storage_manager.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import studio.fantasyit.maid_storage_manager.registry.EntityRegistry;

public class VirtualItemEntity extends Entity {

    private static final EntityDataAccessor<ItemStack> DATA_ITEM = SynchedEntityData.defineId(ItemEntity.class, EntityDataSerializers.ITEM_STACK);
    private int ttl;
    private int age;
    public final float bobOffs;


    public VirtualItemEntity(EntityType<VirtualItemEntity> entityEntityType, Level level) {
        super(entityEntityType, level);
        this.bobOffs = this.random.nextFloat() * (float) Math.PI * 2.0F;
        this.ttl = 400;
    }

    public static VirtualItemEntity create(Level level, Vec3 pos, ItemStack itemStack) {
        VirtualItemEntity virtualItemEntity = new VirtualItemEntity(EntityRegistry.VIRTUAL_ITEM_ENTITY.get(), level);
        virtualItemEntity.setItem(itemStack);
        virtualItemEntity.setPos(pos);
        return virtualItemEntity;
    }

    @Override
    protected void defineSynchedData() {
        this.getEntityData().define(DATA_ITEM, ItemStack.EMPTY);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag p_20052_) {
        this.setItem(ItemStack.of(p_20052_.getCompound("item")));
        this.ttl = p_20052_.getInt("ttl");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag p_20139_) {
        p_20139_.put("Item", this.getItem().save(new CompoundTag()));
        p_20139_.putInt("TTL", ttl);
    }

    public ItemStack getItem() {
        return this.getEntityData().get(DATA_ITEM);
    }

    public void setItem(ItemStack p_32046_) {
        this.getEntityData().set(DATA_ITEM, p_32046_);
    }

    public int getAge() {
        return age;
    }

    public void tick() {
        if (this.getItem().isEmpty()) {
            this.discard();
        } else {
            age++;
            super.tick();
            this.xo = this.getX();
            this.yo = this.getY();
            this.zo = this.getZ();
            Vec3 vec3 = this.getDeltaMovement();
            float f = this.getEyeHeight() - 0.11111111F;
            if (this.isInWater() && this.getFluidHeight(FluidTags.WATER) > (double) f) {
                this.setUnderwaterMovement();
            } else if (this.isInLava() && this.getFluidHeight(FluidTags.LAVA) > (double) f) {
                this.setUnderLavaMovement();
            } else if (!this.isNoGravity()) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.04D, 0.0D));
            }

            if (this.level().isClientSide) {
                this.noPhysics = false;
            } else {
                this.noPhysics = !this.level().noCollision(this, this.getBoundingBox().deflate(1.0E-7D));
                if (this.noPhysics) {
                    this.moveTowardsClosestSpace(this.getX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0D, this.getZ());
                }
            }

            if (!this.onGround() || this.getDeltaMovement().horizontalDistanceSqr() > (double) 1.0E-5F || (this.tickCount + this.getId()) % 4 == 0) {
                this.move(MoverType.SELF, this.getDeltaMovement());
                float f1 = 0.98F;
                if (this.onGround()) {
                    BlockPos groundPos = getBlockPosBelowThatAffectsMyMovement();
                    f1 = this.level().getBlockState(groundPos).getFriction(level(), groundPos, this) * 0.98F;
                }

                this.setDeltaMovement(this.getDeltaMovement().multiply((double) f1, 0.98D, (double) f1));
                if (this.onGround()) {
                    Vec3 vec31 = this.getDeltaMovement();
                    if (vec31.y < 0.0D) {
                        this.setDeltaMovement(vec31.multiply(1.0D, -0.5D, 1.0D));
                    }
                }
            }

            this.hasImpulse |= this.updateInWaterStateAndDoFluidPushing();
            if (!this.level().isClientSide) {
                double d0 = this.getDeltaMovement().subtract(vec3).lengthSqr();
                if (d0 > 0.01D) {
                    this.hasImpulse = true;
                }
            }

            if (!this.level().isClientSide) {
                if (--this.ttl <= 0 && this.isAlive()) {
                    this.level().addFreshEntity(
                            new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), this.getItem())
                    );
                    this.discard();
                }
            }
        }
    }

    protected BlockPos getBlockPosBelowThatAffectsMyMovement() {
        return this.getOnPos(0.999999F);
    }

    private void setUnderwaterMovement() {
        Vec3 vec3 = this.getDeltaMovement();
        this.setDeltaMovement(vec3.x * (double) 0.99F, vec3.y + (double) (vec3.y < (double) 0.06F ? 5.0E-4F : 0.0F), vec3.z * (double) 0.99F);
    }

    private void setUnderLavaMovement() {
        Vec3 vec3 = this.getDeltaMovement();
        this.setDeltaMovement(vec3.x * (double) 0.95F, vec3.y + (double) (vec3.y < (double) 0.06F ? 5.0E-4F : 0.0F), vec3.z * (double) 0.95F);
    }
}
