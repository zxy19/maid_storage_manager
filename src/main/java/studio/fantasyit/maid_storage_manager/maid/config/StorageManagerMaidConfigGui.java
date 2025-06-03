package studio.fantasyit.maid_storage_manager.maid.config;

import com.github.tartaricacid.touhoulittlemaid.client.gui.entity.maid.task.MaidTaskConfigGui;
import com.github.tartaricacid.touhoulittlemaid.inventory.container.task.TaskConfigContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import studio.fantasyit.maid_storage_manager.maid.data.StorageManagerConfigData;
import studio.fantasyit.maid_storage_manager.network.MaidDataSyncPacket;
import studio.fantasyit.maid_storage_manager.network.Network;
import studio.fantasyit.maid_storage_manager.registry.GuiRegistry;

public class StorageManagerMaidConfigGui extends MaidTaskConfigGui<StorageManagerMaidConfigGui.Container> {
    private StorageManagerConfigData.Data currentMAData;
    private PatchedConfigButton configButton;

    public StorageManagerMaidConfigGui(Container screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);
    }

    public static class Container extends TaskConfigContainer {
        public Container(int id, Inventory inventory, int entityId) {
            super(GuiRegistry.STORAGE_MANAGER_MAID_CONFIG_GUI.get(), id, inventory, entityId);
        }
    }

    @Override
    protected void initAdditionData() {
        this.currentMAData = this.maid.getOrCreateData(StorageManagerConfigData.KEY, StorageManagerConfigData.Data.getDefault());
    }

    @Override
    protected void initAdditionWidgets() {
        super.initAdditionWidgets();

        int startLeft = leftPos + 87;
        int startTop = topPos + 36;
        this.addRenderableWidget(new PatchedConfigButton(startLeft, startTop + 0,
                Component.translatable("gui.maid_storage_manager.config.memory_assistant"),
                Component.translatable(StorageManagerConfigData.getTranslationKey(this.currentMAData.memoryAssistant())),
                button -> {
                    int oNxt = Math.max(this.currentMAData.memoryAssistant().ordinal() - 1, 0);
                    StorageManagerConfigData.MemoryAssistant v = StorageManagerConfigData.MemoryAssistant.values()[oNxt];
                    this.currentMAData.memoryAssistant(v);
                    button.setValue(Component.translatable(StorageManagerConfigData.getTranslationKey(v)));
                    Network.sendMaidDataSync(MaidDataSyncPacket.Type.MemoryAssistant, this.maid.getId(), v.ordinal());
                },
                button -> {
                    int oNxt = Math.min(this.currentMAData.memoryAssistant().ordinal() + 1, StorageManagerConfigData.MemoryAssistant.values().length - 1);
                    StorageManagerConfigData.MemoryAssistant v = StorageManagerConfigData.MemoryAssistant.values()[oNxt];
                    this.currentMAData.memoryAssistant(v);
                    button.setValue(Component.translatable(StorageManagerConfigData.getTranslationKey(v)));
                    Network.sendMaidDataSync(MaidDataSyncPacket.Type.MemoryAssistant, this.maid.getId(), v.ordinal());
                }
        ));
        this.addRenderableWidget(new PatchedConfigButton(startLeft, startTop + 13,
                Component.translatable("gui.maid_storage_manager.config.no_sort_placement"),
                Component.translatable(StorageManagerConfigData.getTranslationKey(this.currentMAData.noSortPlacement())),
                button -> {
                    this.currentMAData.noSortPlacement(false);
                    button.setValue(Component.translatable(StorageManagerConfigData.getTranslationKey(false)));
                    Network.sendMaidDataSync(MaidDataSyncPacket.Type.NoPlaceSort, this.maid.getId(), 0);
                    configButton.setValue(Component.translatable(StorageManagerConfigData.getTranslationKey(this.currentMAData.suppressStrategy())));
                },
                button -> {
                    this.currentMAData.noSortPlacement(true);
                    button.setValue(Component.translatable(StorageManagerConfigData.getTranslationKey(true)));
                    Network.sendMaidDataSync(MaidDataSyncPacket.Type.NoPlaceSort, this.maid.getId(), 1);
                    configButton.setValue(Component.translatable("gui.maid_storage_manager.config.fast_sort.disable"));
                }
        ));
        this.addRenderableWidget(new PatchedConfigButton(startLeft, startTop + 26,
                Component.translatable("gui.maid_storage_manager.config.co_work"),
                Component.translatable(StorageManagerConfigData.getTranslationKey(this.currentMAData.coWorkMode())),
                button -> {
                    this.currentMAData.coWorkMode(false);
                    button.setValue(Component.translatable(StorageManagerConfigData.getTranslationKey(false)));
                    Network.sendMaidDataSync(MaidDataSyncPacket.Type.CoWork, this.maid.getId(), 0);
                },
                button -> {
                    this.currentMAData.coWorkMode(true);
                    button.setValue(Component.translatable(StorageManagerConfigData.getTranslationKey(true)));
                    Network.sendMaidDataSync(MaidDataSyncPacket.Type.CoWork, this.maid.getId(), 1);
                }
        ));

        configButton = this.addRenderableWidget(new PatchedConfigButton(startLeft, startTop + 39,
                Component.translatable("gui.maid_storage_manager.config.fast_sort_mode"),
                Component.translatable(StorageManagerConfigData.getTranslationKey(this.currentMAData.suppressStrategy())),
                button -> {
                    if (this.currentMAData.noSortPlacement()) return;
                    int oNxt = Math.max(this.currentMAData.suppressStrategy().ordinal() - 1, 0);
                    StorageManagerConfigData.SuppressStrategy v = StorageManagerConfigData.SuppressStrategy.values()[oNxt];
                    this.currentMAData.suppressStrategy(v);
                    button.setValue(Component.translatable(StorageManagerConfigData.getTranslationKey(v)));
                    Network.sendMaidDataSync(MaidDataSyncPacket.Type.FastSort, this.maid.getId(), v.ordinal());
                },
                button -> {
                    if (this.currentMAData.noSortPlacement()) return;
                    int oNxt = Math.min(this.currentMAData.suppressStrategy().ordinal() + 1, StorageManagerConfigData.SuppressStrategy.values().length - 1);
                    StorageManagerConfigData.SuppressStrategy v = StorageManagerConfigData.SuppressStrategy.values()[oNxt];
                    this.currentMAData.suppressStrategy(v);
                    button.setValue(Component.translatable(StorageManagerConfigData.getTranslationKey(v)));
                    Network.sendMaidDataSync(MaidDataSyncPacket.Type.FastSort, this.maid.getId(), v.ordinal());
                }
        ));
        this.addRenderableWidget(new PatchedConfigButton(startLeft, startTop + 52,
                Component.translatable("gui.maid_storage_manager.config.allow_seek_meal"),
                Component.translatable(StorageManagerConfigData.getTranslationKey(this.currentMAData.allowSeekWorkMeal())),
                button -> {
                    this.currentMAData.allowSeekWorkMeal(false);
                    button.setValue(Component.translatable(StorageManagerConfigData.getTranslationKey(false)));
                    Network.sendMaidDataSync(MaidDataSyncPacket.Type.AllowSeekWorkMeal, this.maid.getId(), 0);
                },
                button -> {
                    this.currentMAData.allowSeekWorkMeal(true);
                    button.setValue(Component.translatable(StorageManagerConfigData.getTranslationKey(true)));
                    Network.sendMaidDataSync(MaidDataSyncPacket.Type.AllowSeekWorkMeal, this.maid.getId(), 1);
                }
        ));
    }
}
