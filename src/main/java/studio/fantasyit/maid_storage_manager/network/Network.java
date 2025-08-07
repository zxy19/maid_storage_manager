package studio.fantasyit.maid_storage_manager.network;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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
import studio.fantasyit.maid_storage_manager.data.BindingData;
import studio.fantasyit.maid_storage_manager.data.InScreenTipData;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;
import studio.fantasyit.maid_storage_manager.data.InventoryListDataClient;
import studio.fantasyit.maid_storage_manager.integration.create.StockManagerInteract;
import studio.fantasyit.maid_storage_manager.integration.request.IngredientRequest;
import studio.fantasyit.maid_storage_manager.items.CraftGuide;
import studio.fantasyit.maid_storage_manager.items.LogisticsGuide;
import studio.fantasyit.maid_storage_manager.items.ProgressPad;
import studio.fantasyit.maid_storage_manager.items.StorageDefineBauble;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.maid.data.StorageManagerConfigData;
import studio.fantasyit.maid_storage_manager.menu.FilterMenu;
import studio.fantasyit.maid_storage_manager.menu.ItemSelectorMenu;
import studio.fantasyit.maid_storage_manager.menu.LogisticsGuideMenu;
import studio.fantasyit.maid_storage_manager.menu.craft.base.ICraftGuiPacketReceiver;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.registry.MemoryModuleRegistry;

import java.util.List;
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

    public static void sendRequestListPacket(UUID uuid) {
        INSTANCE.send(PacketDistributor.SERVER.noArg(), new PartialInventoryListData(uuid, List.of()));
    }

    public static void sendMaidDataSync(MaidDataSyncPacket.Type type, int id, int value) {
        INSTANCE.send(PacketDistributor.SERVER.noArg(), new MaidDataSyncPacket(type, id, value));
    }

    public static void sendShowInvPacket(ServerPlayer player, InventoryItem item, int time) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ShowInvPacket(item, time));
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
                        } else if (sender.containerMenu instanceof LogisticsGuideMenu lgm) {
                            lgm.handleUpdate(msg.type, msg.key, msg.value);
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
                        if (Minecraft.getInstance().player != null) {
                            Minecraft.getInstance().player.sendSystemMessage(Component.literal(msg.data));
                        }
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
                        context.get().setPacketHandled(true);
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
                                CraftGuide.rollMode(item, sender, msg.value > 0 ? -1 : 1);
                            } else if (item.is(ItemRegistry.CRAFT_GUIDE.get()) && msg.type == ClientInputPacket.Type.ALT_SCROLL) {
                                CraftGuide.rollSpecial(item, sender, msg.value > 0 ? -1 : 1);
                            } else if (item.is(ItemRegistry.STORAGE_DEFINE_BAUBLE.get()) && msg.type == ClientInputPacket.Type.SCROLL) {
                                StorageDefineBauble.rollMode(item, sender, msg.value > 0 ? -1 : 1);
                            } else if (item.is(ItemRegistry.LOGISTICS_GUIDE.get()) && msg.type == ClientInputPacket.Type.SCROLL) {
                                LogisticsGuide.rollMode(item, sender, msg.value > 0 ? -1 : 1);
                            } else if (item.is(ItemRegistry.PROGRESS_PAD.get())) {
                                if (msg.type == ClientInputPacket.Type.SCROLL)
                                    ProgressPad.rollValue(item, sender, msg.value > 0 ? -1 : 1);
                                else ProgressPad.rollSelecting(item, sender, msg.value > 0 ? -1 : 1);
                            }
                        }
                        context.get().setPacketHandled(true);
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
                        } else if (msg.type == MaidDataSyncPacket.Type.CoWork) {
                            StorageManagerConfigData.Data data = maid.getOrCreateData(
                                    StorageManagerConfigData.KEY,
                                    StorageManagerConfigData.Data.getDefault()
                            );
                            data.coWorkMode(msg.value == 1);
                            maid.setAndSyncData(StorageManagerConfigData.KEY, data);
                        } else if (msg.type == MaidDataSyncPacket.Type.AllowSeekWorkMeal) {
                            StorageManagerConfigData.Data data = maid.getOrCreateData(
                                    StorageManagerConfigData.KEY,
                                    StorageManagerConfigData.Data.getDefault()
                            );
                            data.allowSeekWorkMeal(msg.value == 1);
                            maid.setAndSyncData(StorageManagerConfigData.KEY, data);
                        } else if (msg.type == MaidDataSyncPacket.Type.FastSort) {
                            StorageManagerConfigData.Data data = maid.getOrCreateData(
                                    StorageManagerConfigData.KEY,
                                    StorageManagerConfigData.Data.getDefault()
                            );
                            data.suppressStrategy(StorageManagerConfigData.SuppressStrategy.values()[msg.value]);
                            maid.setAndSyncData(StorageManagerConfigData.KEY, data);
                        } else if (msg.type == MaidDataSyncPacket.Type.MemorizeCraftGuide) {
                            StorageManagerConfigData.Data data = maid.getOrCreateData(
                                    StorageManagerConfigData.KEY,
                                    StorageManagerConfigData.Data.getDefault()
                            );
                            data.useMemorizedCraftGuide(msg.value == 1);
                            maid.setAndSyncData(StorageManagerConfigData.KEY, data);
                        } else if (msg.type == MaidDataSyncPacket.Type.MaxParallel) {
                            StorageManagerConfigData.Data data = maid.getOrCreateData(
                                    StorageManagerConfigData.KEY,
                                    StorageManagerConfigData.Data.getDefault()
                            );
                            data.maxParallel(msg.value);
                            maid.setAndSyncData(StorageManagerConfigData.KEY, data);
                        } else if (msg.type == MaidDataSyncPacket.Type.CraftingRepeatCount) {
                            StorageManagerConfigData.Data data = maid.getOrCreateData(
                                    StorageManagerConfigData.KEY,
                                    StorageManagerConfigData.Data.getDefault()
                            );
                            data.maxCraftingLayerRepeatCount(msg.value);
                            maid.setAndSyncData(StorageManagerConfigData.KEY, data);
                        }
                    }
                    context.get().setPacketHandled(true);
                }
        );
        Network.INSTANCE.registerMessage(6,
                CraftGuideGuiPacket.class,
                CraftGuideGuiPacket::toBytes,
                CraftGuideGuiPacket::new,
                (msg, context) -> {
                    context.get().enqueueWork(() -> {
                        @Nullable Player sender = context.get().getSender();
                        if (sender == null) sender = getLocalPlayer();
                        if (sender.containerMenu instanceof ICraftGuiPacketReceiver icgpr) {
                            icgpr.handleGuiPacket(msg.type, msg.key, msg.value, msg.data);
                        }
                        context.get().setPacketHandled(true);
                    });
                }
        );
        Network.INSTANCE.registerMessage(7,
                RenderEntityPacket.class,
                RenderEntityPacket::toBytes,
                RenderEntityPacket::new,
                (msg, context) -> {
                    context.get().enqueueWork(() -> {
                        BindingData.setEntityIds(msg.entityIds);
                        context.get().setPacketHandled(true);
                    });
                }
        );
        Network.INSTANCE.registerMessage(8,
                ShowInvPacket.class,
                ShowInvPacket::toBytes,
                ShowInvPacket::new,
                (msg, context) -> {
                    context.get().enqueueWork(() -> {
                        InventoryListDataClient.setShowingInv(msg.data, msg.time);
                        context.get().setPacketHandled(true);
                    });
                }
        );
        Network.INSTANCE.registerMessage(9,
                JEIRequestPacket.class,
                JEIRequestPacket::toBytes,
                JEIRequestPacket::new,
                (msg, context) -> {
                    context.get().enqueueWork(() -> {
                        ServerPlayer sender = context.get().getSender();
                        if (sender == null) return;
                        IngredientRequest.onRequest(sender, msg.data, msg.targetMaidId);
                        context.get().setPacketHandled(true);
                    });
                }
        );
        Network.INSTANCE.registerMessage(10,
                JEIRequestResultPacket.class,
                JEIRequestResultPacket::toBytes,
                JEIRequestResultPacket::new,
                (msg, context) -> {
                    context.get().enqueueWork(() -> {
                        InScreenTipData.show(msg.result, 5.0f);
                        context.get().setPacketHandled(true);
                    });
                }
        );
        Network.INSTANCE.registerMessage(11,
                MaidDataSyncToClientPacket.class,
                MaidDataSyncToClientPacket::toBytes,
                MaidDataSyncToClientPacket::new,
                (msg, context) -> {
                    context.get().enqueueWork(() -> {
                        Player sender = getLocalPlayer();
                        if (sender.level().getEntity(msg.id) instanceof EntityMaid maid) {
                            if (msg.type == MaidDataSyncToClientPacket.Type.WORKING) {
                                maid.getBrain().setMemory(
                                        MemoryModuleRegistry.CURRENTLY_WORKING.get(),
                                        ScheduleBehavior.Schedule.values()[msg.value.getInt("id")]
                                );
                            } else if (msg.type == MaidDataSyncToClientPacket.Type.BAUBLE) {
                                maid.getMaidBauble().deserializeNBT(msg.value);
                            }
                        }
                        context.get().setPacketHandled(true);
                    });
                }
        );
        Network.INSTANCE.registerMessage(12,
                CreateStockManagerPacket.class,
                CreateStockManagerPacket::toBytes,
                CreateStockManagerPacket::new,
                (packet, context) -> {
                    context.get().enqueueWork(() -> {
                        @Nullable ServerPlayer sender = context.get().getSender();
                        if (sender != null) {
                            Entity target = sender.level().getEntity(packet.id);
                            if (target instanceof EntityMaid maid) {
                                if (packet.data == CreateStockManagerPacket.Type.OPEN_SCREEN) {
                                    StockManagerInteract.onHandleStockManager(sender, maid, packet.ticker);
                                } else if (packet.data == CreateStockManagerPacket.Type.SHOP_LIST) {
                                    StockManagerInteract.onHandleShoppingList(sender, maid, packet.ticker);
                                }
                            }
                        }
                        context.get().setPacketHandled(true);
                    });
                }
        );
        Network.INSTANCE.registerMessage(13,
                ProgressPadUpdatePacket.class,
                ProgressPadUpdatePacket::toNetwork,
                ProgressPadUpdatePacket::new,
                (p, c) -> {
                    c.get().enqueueWork(() -> {
                        ProgressPadUpdatePacket.handle(p);
                        c.get().setPacketHandled(true);
                    });
                }
        );

        Network.INSTANCE.registerMessage(14,
                ShowCommonPacket.class,
                ShowCommonPacket::toBytes,
                ShowCommonPacket::new,
                (p, c) -> {
                    c.get().enqueueWork(() -> {
                        ShowCommonPacket.handle(p);
                        c.get().setPacketHandled(true);
                    });
                }
        );

    }

    @OnlyIn(Dist.CLIENT)
    private static Player getLocalPlayer() {
        return Minecraft.getInstance().player;
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
