package studio.fantasyit.maid_storage_manager.mixin;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class LivingEntityBrainSerializeWrapper extends Entity {

    public LivingEntityBrainSerializeWrapper(EntityType<?> p_19870_, Level p_19871_) {
        super(p_19870_, p_19871_);
    }

    @WrapOperation(method = "addAdditionalSaveData", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/Brain;serializeStart(Lcom/mojang/serialization/DynamicOps;)Lcom/mojang/serialization/DataResult;"), require = 0)
    private <T> DataResult<T> serializeBrain(Brain<?> instance, DynamicOps<T> p_21915_, Operation<DataResult<T>> original) {
        //noinspection ConstantValue
        if (((Entity) this instanceof EntityMaid) && !(p_21915_ instanceof RegistryOps<?>)) {
            return instance.serializeStart(this.level().registryAccess().createSerializationContext(p_21915_));
        }
        return original.call(instance, p_21915_);
    }
}
