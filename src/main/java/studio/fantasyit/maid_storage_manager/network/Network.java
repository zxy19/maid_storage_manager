package studio.fantasyit.maid_storage_manager.network;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.commons.lang3.tuple.Pair;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.capability.InventoryListDataProvider;
import studio.fantasyit.maid_storage_manager.data.InventoryListDataClient;
import studio.fantasyit.maid_storage_manager.debug.DebugData;
import studio.fantasyit.maid_storage_manager.menu.FilterMenu;
import studio.fantasyit.maid_storage_manager.menu.ItemSelectorMenu;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Network {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MaidStorageManager.MODID, "go_target"),
            () -> PROTOCOL_VERSION,
            (v) -> true,
            (v) -> true
    );

    public static void sendItemSelectorGuiPacket(ItemSelectorGuiPacket.SlotType type, int key, int value) {
        INSTANCE.send(PacketDistributor.SERVER.noArg(), new ItemSelectorGuiPacket(type, key, value));
    }

    public static void sendItemSelectorSetItemPacket(List<Pair<Integer, ItemStack>> list) {
        INSTANCE.send(PacketDistributor.SERVER.noArg(), new ItemSelectorSetItemPacket(list));
    }

    public static void sendItemSelectorSetItemPacket(Integer slot, ItemStack item) {
        sendItemSelectorSetItemPacket(List.of(Pair.of(slot, item)));
    }

    public static void sendDebugDataPacket(String type, CompoundTag data) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), new DebugDataPacket(type, data));
    }

    public static void sendRequestListPacket(UUID uuid) {
        INSTANCE.send(PacketDistributor.SERVER.noArg(), new PartialInventoryListData(uuid, List.of()));
    }

    private static void registerMessage() {
        Network.INSTANCE.registerMessage(0,
                ItemSelectorGuiPacket.class,
                ItemSelectorGuiPacket::toBytes,
                ItemSelectorGuiPacket::new,
                (msg, context) -> {
                    context.get().enqueueWork(() -> {
                        ServerPlayer sender = context.get().getSender();
                        if (sender != null && sender.containerMenu instanceof ItemSelectorMenu ism) {
                            ism.handleUpdate(msg.type, msg.key, msg.value);
                        } else if (sender.containerMenu instanceof FilterMenu ifm) {
                            ifm.handleUpdate(msg.type, msg.key, msg.value);
                        }
                    });
                    context.get().setPacketHandled(true);
                }
        );
        Network.INSTANCE.registerMessage(1,
                ItemSelectorSetItemPacket.class,
                ItemSelectorSetItemPacket::toBytes,
                ItemSelectorSetItemPacket::new,
                (msg, context) -> {
                    context.get().enqueueWork(() -> {
                        ServerPlayer sender = context.get().getSender();
                        if (sender != null && sender.containerMenu instanceof ItemSelectorMenu ism) {
                            msg.items.forEach((p) -> ism.filteredItems.setItem(p.getLeft(), p.getRight()));
                            ism.save();
                            ism.broadcastChanges();
                        }
                    });
                    context.get().setPacketHandled(true);
                }
        );
        Network.INSTANCE.registerMessage(2,
                DebugDataPacket.class,
                DebugDataPacket::toBytes,
                DebugDataPacket::new,
                (msg, context) -> {
                    context.get().enqueueWork(() -> {
                        if (!Config.enableDebug) return;
                        if (Objects.equals(msg.type, DebugData.TYPE_DEBUG_MSG)) {
                            if (Minecraft.getInstance().player != null) {
                                Minecraft.getInstance().player.sendSystemMessage(Component.literal(msg.data.getString("msg")));
                            }
                        } else
                            DebugData.getInstance().setData(msg.type, msg.data);
                    });
                    context.get().setPacketHandled(true);
                }
        );
        Network.INSTANCE.registerMessage(3,
                PartialInventoryListData.class,
                PartialInventoryListData::toBytes,
                PartialInventoryListData::new,
                (msg, context) -> {
                    context.get().enqueueWork(() -> {
                        if (context.get().getDirection().getReceptionSide().isClient()) {
                            InventoryListDataClient.getInstance().patch(msg.key, msg.data);
                        } else {
                            context.get().getSender().getServer().overworld().getCapability(InventoryListDataProvider.INVENTORY_LIST_DATA_CAPABILITY)
                                    .ifPresent(inventoryListData -> inventoryListData.sendTo(msg.key, context.get().getSender()));
                        }
                    });
                }
        );
    }

    @Mod.EventBusSubscriber(modid = MaidStorageManager.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.DEDICATED_SERVER)
    public static class Server {
        @SubscribeEvent
        public static void FMLClientSetupEvent(FMLDedicatedServerSetupEvent event) {
            registerMessage();
        }
    }

    @Mod.EventBusSubscriber(modid = MaidStorageManager.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class Client {
        @SubscribeEvent
        public static void FMLClientSetupEvent(FMLClientSetupEvent event) {
            registerMessage();
        }

    }
}
