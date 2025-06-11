package studio.fantasyit.maid_storage_manager.mixin;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestMenu;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestScreen;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.integration.Integrations;
import studio.fantasyit.maid_storage_manager.integration.create.StockManagerInteract;

import java.lang.ref.WeakReference;
import java.util.List;

@Mixin(StockKeeperRequestScreen.class)
public abstract class CreateStockKeeperScreenMixin {
    @Shadow(remap = false)
    StockTickerBlockEntity blockEntity;

    @Shadow(remap = false)
    WeakReference<LivingEntity> stockKeeper;

    @Shadow(remap = false)
    WeakReference<BlazeBurnerBlockEntity> blaze;

    @Inject(at = @At("RETURN"), method = "<init>")
    private void init(StockKeeperRequestMenu container, Inventory inv, Component title, CallbackInfo ci) {
        if (Integrations.createStockManager()) {
            ClientLevel level = Minecraft.getInstance().level;
            if (level != null) {
                List<EntityMaid> entities = level.getEntities(
                        EntityTypeTest.forClass(EntityMaid.class),
                        new AABB(blockEntity.getBlockPos()).inflate(Config.createStockKeeperRangeV, Config.createStockKeeperRangeH, Config.createStockKeeperRangeV),
                        t -> t.getId() == StockManagerInteract.lastInteractedMaidId
                );
                if (!entities.isEmpty()) {
                    stockKeeper = new WeakReference<>(entities.get(0));
                    blaze = new WeakReference<>(null);
                }
            }
            StockManagerInteract.setInteractedMaidId(-1);
        }
    }
}
