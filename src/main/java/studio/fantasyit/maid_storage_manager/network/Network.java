package studio.fantasyit.maid_storage_manager.network;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.capability.InventoryListDataProvider;
import studio.fantasyit.maid_storage_manager.data.InventoryListDataClient;
import studio.fantasyit.maid_storage_manager.debug.DebugData;
import studio.fantasyit.maid_storage_manager.items.CraftGuide;
import studio.fantasyit.maid_storage_manager.items.StorageDefineBauble;
import studio.fantasyit.maid_storage_manager.maid.data.StorageManagerConfigData;
import studio.fantasyit.maid_storage_manager.menu.CraftGuideMenu;
import studio.fantasyit.maid_storage_manager.menu.FilterMenu;
import studio.fantasyit.maid_storage_manager.menu.ItemSelectorMenu;
import studio.fantasyit.maid_storage_manager.menu.container.FilterContainer;
import studio.fantasyit.maid_storage_manager.menu.container.FilterSlot;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;

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

    public static void sendMaidDataSync(MaidDataSyncPacket.Type type, int id, int value) {
        INSTANCE.send(PacketDistributor.SERVER.noArg(), new MaidDataSyncPacket(type, id, value));
    }

    private static void registerMessage() {
        Network.INSTANCE.registerMessage(0,
                ItemSelectorGuiPacket.class,
                ItemSelectorGuiPacket::toBytes,
                ItemSelectorGuiPacket::new,
                (msg, context) -> {
                    context.get().enqueueWork(() -> {
                        ServerPlayer sender = context.get().getSender();
                        if (sender == null) return;
                        if (sender.containerMenu instanceof ItemSelectorMenu ism) {
                            ism.handleUpdate(msg.type, msg.key, msg.value);
                        } else if (sender.containerMenu instanceof FilterMenu ifm) {
                            ifm.handleUpdate(msg.type, msg.key, msg.value);
                        } else if (sender.containerMenu instanceof CraftGuideMenu cm) {
                            cm.handleUpdate(msg.type, msg.key, msg.value);
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
                        } else if (sender != null && sender.containerMenu instanceof FilterMenu ism) {
                            msg.items.forEach((p) -> ism.filteredItems.setItem(p.getLeft(), p.getRight()));
                            ism.save();
                            ism.broadcastChanges();
                        } else if (sender != null && sender.containerMenu instanceof CraftGuideMenu ism) {
                            msg.items.forEach((p) -> {
                                if (ism.getSlot(p.getLeft()) instanceof FilterSlot fs) {
                                    Integer iid = ism.iid.get(fs.index);
                                    FilterContainer filter = ism.filters.get(fs.index);
                                    if (filter != null && iid != null) {
                                        filter.count[iid].setValue(p.getRight().getCount());
                                        filter.setItem(iid, p.getRight());
                                    }
                                }
                                ism.recalcRecipe();
                                ism.recheckValidation();
                                ism.save();
                            });
                            ism.recalcRecipe();
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
        Network.INSTANCE.registerMessage(4,
                ClientInputPacket.class,
                ClientInputPacket::toBytes,
                ClientInputPacket::new,
                (msg, context) -> {
                    NetworkEvent.Context context1 = context.get();
                    context1.enqueueWork(() -> {
                        ServerPlayer sender = context1.getSender();
                        if (sender != null) {
                            ItemStack item = sender.getItemInHand(InteractionHand.MAIN_HAND);
                            if (item.is(ItemRegistry.CRAFT_GUIDE.get()) && msg.type == ClientInputPacket.Type.SCROLL) {
                                CraftGuide.rollMode(item, sender, msg.value);
                            } else if (item.is(ItemRegistry.STORAGE_DEFINE_BAUBLE.get()) && msg.type == ClientInputPacket.Type.SCROLL) {
                                StorageDefineBauble.rollMode(item, sender, msg.value);
                            }
                        }
                    });
                }
        );
        Network.INSTANCE.registerMessage(5,
                MaidDataSyncPacket.class,
                MaidDataSyncPacket::toBytes,
                MaidDataSyncPacket::new,
                (msg, context) -> {
                    @Nullable ServerPlayer sender = context.get().getSender();
                    if (sender == null) return;
                    Entity entity = sender.level().getEntity(msg.id);
                    if (entity instanceof EntityMaid maid) {
                        if (msg.type == MaidDataSyncPacket.Type.MemoryAssistant) {
                            StorageManagerConfigData.Data data = maid.getOrCreateData(
                                    StorageManagerConfigData.KEY,
                                    StorageManagerConfigData.Data.getDefault()
                            );
                            data.memoryAssistant(StorageManagerConfigData.MemoryAssistant.values()[msg.value]);
                            maid.setAndSyncData(StorageManagerConfigData.KEY, data);
                        } else if (msg.type == MaidDataSyncPacket.Type.NoPlaceSort) {
                            StorageManagerConfigData.Data data = maid.getOrCreateData(
                                    StorageManagerConfigData.KEY,
                                    StorageManagerConfigData.Data.getDefault()
                            );
                            data.noSortPlacement(msg.value == 1);
                            maid.setAndSyncData(StorageManagerConfigData.KEY, data);
                        }else if(msg.type == MaidDataSyncPacket.Type.CoWork){
                            StorageManagerConfigData.Data data = maid.getOrCreateData(
                                    StorageManagerConfigData.KEY,
                                    StorageManagerConfigData.Data.getDefault()
                            );
                            data.coWorkMode(msg.value == 1);
                            maid.setAndSyncData(StorageManagerConfigData.KEY, data);
                        }
                    }
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
