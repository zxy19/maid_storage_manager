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
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.apache.commons.lang3.tuple.Pair;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.ai.GetStorageFunction;
import studio.fantasyit.maid_storage_manager.data.BindingData;
import studio.fantasyit.maid_storage_manager.data.InScreenTipData;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;
import studio.fantasyit.maid_storage_manager.data.InventoryListDataClient;
import studio.fantasyit.maid_storage_manager.integration.request.IngredientRequest;
import studio.fantasyit.maid_storage_manager.items.CraftGuide;
import studio.fantasyit.maid_storage_manager.items.LogisticsGuide;
import studio.fantasyit.maid_storage_manager.items.ProgressPad;
import studio.fantasyit.maid_storage_manager.items.StorageDefineBauble;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.maid.data.StorageManagerConfigData;
import studio.fantasyit.maid_storage_manager.menu.craft.base.ICraftGuiPacketReceiver;
import studio.fantasyit.maid_storage_manager.menu.filter.FilterMenu;
import studio.fantasyit.maid_storage_manager.menu.logistics.LogisticsGuideMenu;
import studio.fantasyit.maid_storage_manager.menu.request.ItemSelectorMenu;
import studio.fantasyit.maid_storage_manager.registry.DataAttachmentRegistry;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.registry.MemoryModuleRegistry;

import java.util.List;
import java.util.UUID;

//import studio.fantasyit.maid_storage_manager.data.InScreenTipData;
//import studio.fantasyit.maid_storage_manager.integration.create.StockManagerInteract;
//import studio.fantasyit.maid_storage_manager.menu.craft.base.ICraftGuiPacketReceiver;
//import studio.fantasyit.maid_storage_manager.menu.logistics.LogisticsGuideMenu;

public class Network {
    private static final String PROTOCOL_VERSION = "1";

    public static void sendItemSelectorGuiPacket(ItemSelectorGuiPacket.SlotType type, int key, int value) {
        ClientPacketDistributor.sendToServer(new ItemSelectorGuiPacket(type, key, value));
    }


    public static void sendItemSelectorSetItemPacket(List<Pair<Integer, ItemStack>> list) {
        ClientPacketDistributor.sendToServer(new ItemSelectorSetItemPacket(list));
    }

    public static void sendItemSelectorSetItemPacket(Integer slot, ItemStack item) {
        sendItemSelectorSetItemPacket(List.of(Pair.of(slot, item)));
    }

    public static void sendRequestListPacket(UUID uuid) {
        ClientPacketDistributor.sendToServer(new PartialInventoryListData(uuid, List.of()));
    }

    public static void sendMaidDataSync(MaidDataSyncPacket.Type type, int id, int value) {
        ClientPacketDistributor.sendToServer(new MaidDataSyncPacket(type, id, value));
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
                        context
                                .player()
                                .level().getServer()
                                .overworld()
                                .getData(DataAttachmentRegistry.INVENTORY_LIST_DATA)
                                .sendTo(msg.key, (ServerPlayer) context.player());
                    });
                },
                (msg, context) -> InventoryListDataClient.getInstance().patch(msg.key, msg.data)
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
                        StorageManagerConfigData.Data data = StorageManagerConfigData.get(maid);
                        if (msg.type == MaidDataSyncPacket.Type.MemoryAssistant) {
                            data.memoryAssistant(StorageManagerConfigData.MemoryAssistant.values()[msg.value]);
                        } else if (msg.type == MaidDataSyncPacket.Type.NoPlaceSort) {
                            data.noSortPlacement(msg.value == 1);
                        } else if (msg.type == MaidDataSyncPacket.Type.CoWork) {
                            data.coWorkMode(msg.value == 1);
                        } else if (msg.type == MaidDataSyncPacket.Type.AllowSeekWorkMeal) {
                            data.allowSeekWorkMeal(msg.value == 1);
                        } else if (msg.type == MaidDataSyncPacket.Type.FastSort) {
                            data.suppressStrategy(StorageManagerConfigData.SuppressStrategy.values()[msg.value]);
                        } else if (msg.type == MaidDataSyncPacket.Type.MemorizeCraftGuide) {
                            data.useMemorizedCraftGuide(msg.value == 1);
                        } else if (msg.type == MaidDataSyncPacket.Type.MaxParallel) {
                            data.maxParallel(msg.value);
                        } else if (msg.type == MaidDataSyncPacket.Type.CraftingRepeatCount) {
                            data.maxCraftingLayerRepeatCount(msg.value);
                        } else if (msg.type == MaidDataSyncPacket.Type.AutoSorting) {
                            data.autoSorting(msg.value != 0);
                        } else if (msg.type == MaidDataSyncPacket.Type.ItemTypeLimit) {
                            data.itemTypeLimit(msg.value);
                        } else if (msg.type == MaidDataSyncPacket.Type.DoCommunicate) {
                            data.doCommunicate(msg.value != 0);
                        }
                        StorageManagerConfigData.set(maid, data);
                    }
                }
        );
        IPayloadHandler<CraftGuideGuiPacket> handler = (msg, context) -> {
            Player sender = context.player();
            context.enqueueWork(() -> {
                ICraftGuiPacketReceiver disabled;
                if (sender.containerMenu instanceof ICraftGuiPacketReceiver icgpr) {
                    icgpr.handleGuiPacket(msg.type, msg.key, msg.value, msg.data);
                }
            });
        };
        registrar.playBidirectional(
                CraftGuideGuiPacket.TYPE,
                CraftGuideGuiPacket.STREAM_CODEC,
                handler,
                handler
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
                IngredientRequestC2SPacket.TYPE,
                IngredientRequestC2SPacket.STREAM_CODEC,
                (msg, context) -> {
                    if (!(context.player() instanceof ServerPlayer sender)) return;
                    context.enqueueWork(() -> {
                        IngredientRequest.onRequest(sender, msg.data, msg.targetMaidId);
                    });
                }
        );
        registrar.playToClient(
                IngredientRequestResultS2CPacket.TYPE,
                IngredientRequestResultS2CPacket.STREAM_CODEC,
                (msg, context) -> {
                    context.enqueueWork(() -> {
                        InScreenTipData.show(msg.result, 5.0f);
                    });
                }
        );
        registrar.playToClient(
                MaidScheduleSyncPacket.TYPE,
                MaidScheduleSyncPacket.STREAM_CODEC,
                (msg, context) -> {
                    context.enqueueWork(() -> {
                        Player sender = context.player();
                        if (sender.level().getEntity(msg.maidId) instanceof EntityMaid maid) {
                            maid.getBrain().setMemory(
                                    MemoryModuleRegistry.CURRENTLY_WORKING.get(),
                                    ScheduleBehavior.Schedule.values()[msg.scheduleOrdinal]
                            );
                        }
                    });
                }
        );
        registrar.playToClient(
                MaidBaubleSyncPacket.TYPE,
                MaidBaubleSyncPacket.STREAM_CODEC,
                (msg, context) -> {
                    context.enqueueWork(() -> {
                        Player sender = context.player();
                        if (sender.level().getEntity(msg.maidId) instanceof EntityMaid maid) {
                            var baubleHandler = maid.getMaidBauble();
                            try (Transaction tx = Transaction.open(null)) {
                                for (int i = 0; i < msg.baubles.size() && i < baubleHandler.size(); i++) {
                                    ItemStack stack = msg.baubles.get(i);
                                    var old = baubleHandler.getResource(i);
                                    if (!old.isEmpty()) {
                                        baubleHandler.extract(i, old, (int) baubleHandler.getAmountAsLong(i), tx);
                                    }
                                    if (!stack.isEmpty()) {
                                        baubleHandler.insert(i, ItemResource.of(stack), stack.getCount(), tx);
                                    }
                                }
                                tx.commit();
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
                    // TODO wait create
//                    context.enqueueWork(() -> {
//                        Entity target = sender.level().getEntity(packet.id);
//                        if (target instanceof EntityMaid maid) {
//                            if (packet.data == CreateStockManagerPacket.Type.OPEN_SCREEN) {
//                                StockManagerInteract.onHandleStockManager(sender, maid, packet.ticker);
//                            } else if (packet.data == CreateStockManagerPacket.Type.SHOP_LIST) {
//                                StockManagerInteract.onHandleShoppingList(sender, maid, packet.ticker);
//                            }
//                        }
//                    });
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

        registrar.playToClient(
                ShowCommonPacket.TYPE,
                ShowCommonPacket.STREAM_CODEC,
                (p, c) -> {
                    c.enqueueWork(() -> {
                        ShowCommonPacket.handle(p);
                    });
                }
        );
        registrar.playBidirectional(
                CommunicateMarkGuiPacket.TYPE,
                CommunicateMarkGuiPacket.STREAM_CODEC,
                (p, c) -> {
                    c.enqueueWork(() -> {
                        CommunicateMarkGuiPacket.handle(c.player(), p);
                    });
                },
                (p, c) -> {
                    c.enqueueWork(() -> {
                        CommunicateMarkGuiPacket.handle(c.player(), p);
                    });
                }
        );
        registrar.playBidirectional(
                CraftGuideGeneratorUpdate.TYPE,
                CraftGuideGeneratorUpdate.STREAM_CODEC,
                (p, c) -> {
                    c.enqueueWork(() -> {
                        CraftGuideGeneratorUpdate.handle(c.player(), p);
                    });
                },
                (p, c) -> {
                    c.enqueueWork(() -> {
                        CraftGuideGeneratorUpdate.handle(c.player(), p);
                    });
                }
        );
        registrar.playToServer(
                AIMatchLocalizedItemC2SPacket.TYPE,
                AIMatchLocalizedItemC2SPacket.STREAM_CODEC,
                (p, c) -> {
                    c.enqueueWork(() -> {
                        GetStorageFunction.handleRPC(p.rpcId, p.data);
                    });
                }
        );
        registrar.playToClient(
                AIMatchLocalizedItemS2CPacket.TYPE,
                AIMatchLocalizedItemS2CPacket.STREAM_CODEC,
                (p, c) -> {
                    c.enqueueWork(() -> AIMatchLocalizedItemS2CPacket.handle(p));
                }
        );
    }

    @EventBusSubscriber(modid = MaidStorageManager.MODID)
    public static class Event {
        @SubscribeEvent
        public static void regis(RegisterPayloadHandlersEvent event) {
            registerMessage(event.registrar(PROTOCOL_VERSION));
        }
    }
}
