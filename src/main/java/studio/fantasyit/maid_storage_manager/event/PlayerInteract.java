package studio.fantasyit.maid_storage_manager.event;

import com.github.tartaricacid.touhoulittlemaid.api.event.InteractMaidEvent;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.integration.Integrations;
import studio.fantasyit.maid_storage_manager.integration.create.StockManagerInteract;
import studio.fantasyit.maid_storage_manager.registry.MemoryModuleRegistry;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;
import studio.fantasyit.maid_storage_manager.storage.Target;

import java.util.List;

@EventBusSubscriber(modid = MaidStorageManager.MODID, bus = EventBusSubscriber.Bus.GAME)
public class PlayerInteract {
    public static void onPlayerInteract(ServerLevel level, ServerPlayer player, BlockPos pos, Direction clickedFace) {
        @Nullable Target validStorage = MaidStorage.getInstance().isValidTarget(level, player, pos, clickedFace);
        if (validStorage == null) return;
        List<Entity> maidList = level.getEntities(player, player.getBoundingBox()
                .inflate(10, 10, 10), entity -> entity instanceof EntityMaid && ((EntityMaid) entity).isOwnedBy(player));
        for (Entity eMaid : maidList) {
            EntityMaid maid = (EntityMaid) eMaid;
            maid.getBrain().setMemory(
                    MemoryModuleRegistry.CO_WORK_TARGET_STORAGE.get(),
                    validStorage
            );
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerInteractMaid(InteractMaidEvent event) {
        EntityMaid maid = event.getMaid();
        Player player = event.getPlayer();
        if (Integrations.createStockManager())
            if (StockManagerInteract.onPlayerInteract(player, maid)) {
                event.setCanceled(true);
                return;
            }
        if (player instanceof ServerPlayer sp) {
            if (sp.getMainHandItem().is(Items.EXPERIENCE_BOTTLE)) {
                ItemStack mainHandItem = sp.getMainHandItem();
                int count = sp.isShiftKeyDown() ? mainHandItem.getCount() : 1;
                int amount = (3 + sp.level().random.nextInt(5) + sp.level().random.nextInt(5)) * count;
                maid.setExperience(maid.getExperience() + amount);
                mainHandItem.shrink(count);
                event.setCanceled(true);
            }
        }

    }

    @SubscribeEvent
    public static void onPlayerInteractRc(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide || event.getFace() == null) return;
        onPlayerInteract(
                (ServerLevel) event.getLevel(),
                (ServerPlayer) event.getEntity(),
                event.getPos(),
                event.getFace()
        );
    }

    @SubscribeEvent
    public static void onPlayerInteractLc(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getLevel().isClientSide || event.getFace() == null) return;
        onPlayerInteract(
                (ServerLevel) event.getLevel(),
                (ServerPlayer) event.getEntity(),
                event.getPos(),
                event.getFace()
        );
    }
}
