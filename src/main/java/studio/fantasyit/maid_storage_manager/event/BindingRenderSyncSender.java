package studio.fantasyit.maid_storage_manager.event;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.craft.work.ProgressData;
import studio.fantasyit.maid_storage_manager.data.BindingData;
import studio.fantasyit.maid_storage_manager.items.ProgressPad;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.network.MaidDataSyncToClientPacket;
import studio.fantasyit.maid_storage_manager.network.ProgressPadUpdatePacket;
import studio.fantasyit.maid_storage_manager.network.RenderEntityPacket;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;

import java.util.List;
import java.util.UUID;

@EventBusSubscriber(modid = MaidStorageManager.MODID, bus = EventBusSubscriber.Bus.GAME)
public class BindingRenderSyncSender {
    @SubscribeEvent
    public static void onTickPlayer(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            syncEntitySelector(player);
            syncProgressPad(player, player.getMainHandItem());
            syncProgressPad(player, player.getOffhandItem());
        }
    }

    public static void syncEntitySelector(ServerPlayer player) {
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

    public static void syncProgressPad(ServerPlayer player, ItemStack itemStack) {
        if (itemStack.is(ItemRegistry.PROGRESS_PAD.get())) {
            UUID uuid = ProgressPad.getBindingUUID(itemStack);
            if (uuid != null) {
                if (player.tickCount % 5 == 0 && player.level() instanceof ServerLevel level && level.getEntity(uuid) instanceof EntityMaid maid) {
                    int count = 10;
                    if (ProgressPad.getStyle(itemStack) == ProgressPad.Style.SMALL)
                        count *= 2;
                    PacketDistributor.sendToPlayer(player,
                            new ProgressPadUpdatePacket(
                                    uuid,
                                    ProgressData.fromMaidAuto(maid, level, ProgressPad.getViewing(itemStack), count)
                            )
                    );
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        BindingData.clearFor(event.getEntity());
    }
}
