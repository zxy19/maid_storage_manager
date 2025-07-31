package studio.fantasyit.maid_storage_manager.mixin;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import studio.fantasyit.maid_storage_manager.craft.work.ProgressData;
import studio.fantasyit.maid_storage_manager.items.ProgressPad;
import studio.fantasyit.maid_storage_manager.network.Network;
import studio.fantasyit.maid_storage_manager.network.ProgressPadUpdatePacket;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;

import java.util.UUID;

@Mixin(HangingEntity.class)
public abstract class ItemFrameTickMixin extends Entity {
    public ItemFrameTickMixin(EntityType<?> p_19870_, Level p_19871_) {
        super(p_19870_, p_19871_);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        if (level().isClientSide) return;
        if (((Object) this) instanceof ItemFrame ifr)
            if (ifr.getItem().is(ItemRegistry.PROGRESS_PAD.get()) && ifr.tickCount % 5 == 0) {
                UUID uuid = ProgressPad.getBindingUUID(ifr.getItem());
                if (uuid != null && ((ServerLevel) level()).getEntity(uuid) instanceof EntityMaid maid) {
                    int count = ifr.getRotation() == 0 ? 1 : ifr.getRotation() == 1 ? 15 : 10;
                    Network.INSTANCE.send(
                            PacketDistributor.TRACKING_ENTITY.with(() -> ifr),
                            new ProgressPadUpdatePacket(
                                    uuid,
                                    ProgressData.fromMaidAuto(maid, (ServerLevel) level(), ProgressPad.getViewing(ifr.getItem()), count)
                            )
                    );
                }
            }
    }
}
