package studio.fantasyit.maid_storage_manager.event;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.registry.MemoryModuleRegistry;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;
import studio.fantasyit.maid_storage_manager.storage.Storage;

import java.util.List;

@Mod.EventBusSubscriber(modid = MaidStorageManager.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerInteract {
    public static void onPlayerInteract(ServerLevel level, ServerPlayer player, BlockPos pos, Direction clickedFace) {
        @Nullable Storage validStorage = MaidStorage.getInstance().isValidTarget(level, player, pos, clickedFace);
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

    @SubscribeEvent
    public static void onPlayerInteractRc(PlayerInteractEvent.RightClickBlock event) {
        if(event.getLevel().isClientSide || event.getFace() == null) return;
        onPlayerInteract(
                (ServerLevel) event.getLevel(),
                (ServerPlayer) event.getEntity(),
                event.getPos(),
                event.getFace()
        );
    }

    @SubscribeEvent
    public static void onPlayerInteractLc(PlayerInteractEvent.LeftClickBlock event) {
        if(event.getLevel().isClientSide || event.getFace() == null) return;
        onPlayerInteract(
                (ServerLevel) event.getLevel(),
                (ServerPlayer) event.getEntity(),
                event.getPos(),
                event.getFace()
        );
    }
}
