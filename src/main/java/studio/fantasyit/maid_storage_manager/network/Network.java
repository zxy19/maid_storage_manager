package studio.fantasyit.maid_storage_manager.network;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.apache.commons.lang3.tuple.Pair;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
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
import studio.fantasyit.maid_storage_manager.registry.DataAttachmentRegistry;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.registry.MemoryModuleRegistry;

import java.util.List;
import java.util.UUID;

public class Network {
    private static final String PROTOCOL_VERSION = "1";

    public static void sendItemSelectorGuiPacket(ItemSelectorGuiPacket.SlotType type, int key, int value) {
        PacketDistributor.sendToServer(new ItemSelectorGuiPacket(type, key, value));
    }

    public static void sendItemSelectorSetItemPacket(List<Pair<Integer, ItemStack>> list) {
        PacketDistributor.sendToServer(new ItemSelectorSetItemPacket(list));
    }

    public static void sendItemSelectorSetItemPacket(Integer slot, ItemStack item) {
        sendItemSelectorSetItemPacket(List.of(Pair.of(slot, item)));
    }

    public static void sendRequestListPacket(UUID uuid) {
        PacketDistributor.sendToServer(new PartialInventoryListData(uuid, List.of()));
    }

    public static void sendMaidDataSync(MaidDataSyncPacket.Type type, int id, int value) {
        PacketDistributor.sendToServer(new MaidDataSyncPacket(type, id, value));
    }

    public static void sendShowInvPacket(ServerPlayer player, InventoryItem item, int time) {
        PacketDistributor.sendToPlayer(player, new ShowInvPacket(item, time));
    }

    private static void registerMessage(PayloadRegistrar registrar) {
        registrar.playToServer(
                ItemSelectorGuiPacket.TYPE,
                ItemSelectorGuiPacket.STREAM_CODEC,
                (msg, context) -> {
                    context.enqueueWork(() -> {
                        if (!(context.player() instanceof ServerPlayer sender)) return;
                        if (sender.containerMenu instanceof ItemSelectorMenu ism) {
                            ism.handleUpdate(msg.type, msg.key, msg.value);
                        } else if (sender.containerMenu instanceof FilterMenu ifm) {
                            ifm.handleUpdate(msg.type, msg.key, msg.value);
                        } else if (sender.containerMenu instanceof LogisticsGuideMenu lgm) {
                            lgm.handleUpdate(msg.type, msg.key, msg.value);
                        }
                    });
                }
        );
        registrar.playToServer(
                ItemSelectorSetItemPacket.TYPE,
                ItemSelectorSetItemPacket.STREAM_CODEC,
                (msg, context) -> {
                    context.enqueueWork(() -> {
                        if (!(context.player() instanceof ServerPlayer sender)) return;
                        if (sender.containerMenu instanceof ItemSelectorMenu ism) {
                            msg.items.forEach((p) -> ism.filteredItems.setItem(p.getLeft(), p.getRight()));
                            ism.save();
                            ism.broadcastChanges();
                        } else if (sender.containerMenu instanceof FilterMenu ism) {
                            msg.items.forEach((p) -> ism.filteredItems.setItem(p.getLeft(), p.getRight()));
                            ism.save();
                            ism.broadcastChanges();
                        }
                    });
                }
        );
        registrar.playToClient(
                DebugDataPacket.TYPE,
                DebugDataPacket.STREAM_CODEC,
                (msg, context) -> {
                    context.enqueueWork(() -> {
                        if (!Config.enableDebug) return;
                        if (Minecraft.getInstance().player != null) {
                            Minecraft.getInstance().player.sendSystemMessage(Component.literal(msg.data));
                        }
                    });
                }
        );
        registrar.playBidirectional(
                PartialInventoryListData.TYPE,
                PartialInventoryListData.STREAM_CODEC,
                (msg, context) -> {
                    context.enqueueWork(() -> {
                        if (context.flow().isClientbound()) {
                            InventoryListDataClient.getInstance().patch(msg.key, msg.data);
                        } else {
                            context
                                    .player()
                                    .getServer()
                                    .overworld()
                                    .getData(DataAttachmentRegistry.INVENTORY_LIST_DATA)
                                    .sendTo(msg.key, (ServerPlayer) context.player());
                        }
                    });
                }
        );
        registrar.playToServer(
                ClientInputPacket.TYPE,
                ClientInputPacket.STREAM_CODEC,
                (msg, context) -> {
                    if (!(context.player() instanceof ServerPlayer sender)) return;
                    context.enqueueWork(() -> {
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
                    });
                }
        );
        registrar.playToServer(
                MaidDataSyncPacket.TYPE,
                MaidDataSyncPacket.STREAM_CODEC,
                (msg, context) -> {
                    if (!(context.player() instanceof ServerPlayer sender)) return;
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
                }
        );
        registrar.playToServer(
                CraftGuideGuiPacket.TYPE,
                CraftGuideGuiPacket.STREAM_CODEC,
                (msg, context) -> {
                    Player sender = context.player();
                    context.enqueueWork(() -> {
                        if (sender.containerMenu instanceof ICraftGuiPacketReceiver icgpr) {
                            icgpr.handleGuiPacket(msg.type, msg.key, msg.value, msg.data);
                        }
                    });
                }
        );
        registrar.playToClient(
                RenderEntityPacket.TYPE,
                RenderEntityPacket.STREAM_CODEC,
                (msg, context) -> {
                    context.enqueueWork(() -> {
                        BindingData.setEntityIds(msg.entityIds);
                    });
                }
        );
        registrar.playToClient(
                ShowInvPacket.TYPE,
                ShowInvPacket.STREAM_CODEC,
                (msg, context) -> {
                    context.enqueueWork(() -> {
                        InventoryListDataClient.setShowingInv(msg.data, msg.time);
                    });
                }
        );
        registrar.playToServer(
                JEIRequestPacket.TYPE,
                JEIRequestPacket.STREAM_CODEC,
                (msg, context) -> {
                    if (!(context.player() instanceof ServerPlayer sender)) return;
                    context.enqueueWork(() -> {
                        IngredientRequest.onRequest(sender, msg.data, msg.targetMaidId);
                    });
                }
        );
        registrar.playToClient(
                JEIRequestResultPacket.TYPE,
                JEIRequestResultPacket.STREAM_CODEC,
                (msg, context) -> {
                    context.enqueueWork(() -> {
                        InScreenTipData.show(msg.result, 5.0f);
                    });
                }
        );
        registrar.playToClient(
                MaidDataSyncToClientPacket.TYPE,
                MaidDataSyncToClientPacket.STREAM_CODEC,
                (msg, context) -> {
                    context.enqueueWork(() -> {
                        Player sender = context.player();
                        if (sender.level().getEntity(msg.id) instanceof EntityMaid maid) {
                            if (msg.type == MaidDataSyncToClientPacket.Type.WORKING) {
                                maid.getBrain().setMemory(
                                        MemoryModuleRegistry.CURRENTLY_WORKING.get(),
                                        ScheduleBehavior.Schedule.values()[msg.value.getInt("id")]
                                );
                            } else if (msg.type == MaidDataSyncToClientPacket.Type.BAUBLE) {
                                maid.getMaidBauble().deserializeNBT(sender.registryAccess(), msg.value);
                            }
                        }
                    });
                }
        );
        registrar.playToServer(
                CreateStockManagerPacket.TYPE,
                CreateStockManagerPacket.STREAM_CODEC,
                (packet, context) -> {
                    if (!(context.player() instanceof ServerPlayer sender)) return;
                    context.enqueueWork(() -> {
                        Entity target = sender.level().getEntity(packet.id);
                        if (target instanceof EntityMaid maid) {
                            if (packet.data == CreateStockManagerPacket.Type.OPEN_SCREEN) {
                                StockManagerInteract.onHandleStockManager(sender, maid, packet.ticker);
                            } else if (packet.data == CreateStockManagerPacket.Type.SHOP_LIST) {
                                StockManagerInteract.onHandleShoppingList(sender, maid, packet.ticker);
                            }
                        }
                    });
                }
        );
        registrar.playToClient(
                ProgressPadUpdatePacket.TYPE,
                ProgressPadUpdatePacket.STREAM_CODEC,
                (packet, context) -> {
                    context.enqueueWork(() -> {
                        ProgressPadUpdatePacket.handle(packet);
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

    @EventBusSubscriber(modid = MaidStorageManager.MODID, bus = EventBusSubscriber.Bus.MOD)
    public static class Event {
        @SubscribeEvent
        public static void regis(RegisterPayloadHandlersEvent event) {
            registerMessage(event.registrar(PROTOCOL_VERSION));
        }
    }
}
