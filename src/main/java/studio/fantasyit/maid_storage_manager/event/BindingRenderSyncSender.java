package studio.fantasyit.maid_storage_manager.event;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.craft.work.ProgressData;
import studio.fantasyit.maid_storage_manager.data.BindingData;
import studio.fantasyit.maid_storage_manager.items.ProgressPad;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.network.MaidDataSyncToClientPacket;
import studio.fantasyit.maid_storage_manager.network.Network;
import studio.fantasyit.maid_storage_manager.network.ProgressPadUpdatePacket;
import studio.fantasyit.maid_storage_manager.network.RenderEntityPacket;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;

import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = MaidStorageManager.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BindingRenderSyncSender {
    @SubscribeEvent
    public static void onTickPlayer(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.side == LogicalSide.SERVER) {
            ServerPlayer player = (ServerPlayer) event.player;
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
                        Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new RenderEntityPacket(List.of(entity.getId())));
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
                    Network.INSTANCE.send(
                            PacketDistributor.PLAYER.with(() -> player),
                            new MaidDataSyncToClientPacket(MaidDataSyncToClientPacket.Type.BAUBLE, maid.getId(), maid.getMaidBauble().serializeNBT())
                    );
                });
            }
            Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new RenderEntityPacket(List.of()));
        }

    }

    public static void syncProgressPad(ServerPlayer player, ItemStack itemStack) {
        if (itemStack.is(ItemRegistry.PROGRESS_PAD.get())) {
            UUID uuid = ProgressPad.getBindingUUID(itemStack);
            if (uuid != null)
                if (player.tickCount % 5 == 0 && player.level() instanceof ServerLevel level && level.getEntity(uuid) instanceof EntityMaid maid) {
                    Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                            new ProgressPadUpdatePacket(
                                    uuid,
                                    ProgressData.fromMaidAuto(maid, level, ProgressPad.getViewing(itemStack), 10)
                            )
                    );
                }
        }
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        BindingData.clearFor(event.getEntity());
    }
}
