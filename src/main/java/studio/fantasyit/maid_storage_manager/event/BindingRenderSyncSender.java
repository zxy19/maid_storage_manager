package studio.fantasyit.maid_storage_manager.event;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.data.BindingData;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.network.MaidDataSyncToClientPacket;
import studio.fantasyit.maid_storage_manager.network.RenderEntityPacket;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;

import java.util.List;
import java.util.UUID;

@EventBusSubscriber(modid = MaidStorageManager.MODID, bus = EventBusSubscriber.Bus.GAME)
public class BindingRenderSyncSender {
    @SubscribeEvent
    public static void onTickPlayer(PlayerTickEvent.Post event) {
        Player entity1 = event.getEntity();
        if (entity1 instanceof ServerPlayer player) {
            if (BindingData.isDifferentAndUpdateItemOnHand(player)) {
                if (player.getMainHandItem().is(ItemRegistry.REQUEST_LIST_ITEM.get())) {
                    UUID entityId = RequestListItem.getStorageEntity(player.getMainHandItem());
                    if (entityId != null) {
                        Entity entity = ((ServerLevel) player.level()).getEntity(entityId);
                        if (entity != null && entity.isAlive()) {
                            PacketDistributor.sendToPlayer(player, new RenderEntityPacket(List.of(entity.getId())));
                            return;
                        }
                    }
                }
                if (player.getMainHandItem().is(ItemRegistry.WORK_CARD.get())) {
                    player.level().getEntities(
                            EntityTypeTest.forClass(EntityMaid.class),
                            player.getBoundingBox().inflate(32),
                            t -> true
                    ).forEach(maid -> {
                        PacketDistributor.sendToPlayer(player,
                                new MaidDataSyncToClientPacket(MaidDataSyncToClientPacket.Type.BAUBLE, maid.getId(), maid.getMaidBauble().serializeNBT(player.registryAccess()))
                        );
                    });
                }
                PacketDistributor.sendToPlayer(player, new RenderEntityPacket(List.of()));
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        BindingData.clearFor(event.getEntity());
    }
}
