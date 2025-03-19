package studio.fantasyit.maid_storage_manager.maid.config;

import com.github.tartaricacid.touhoulittlemaid.client.gui.entity.maid.task.MaidTaskConfigGui;
import com.github.tartaricacid.touhoulittlemaid.client.gui.widget.button.MaidConfigButton;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.PickType;
import com.github.tartaricacid.touhoulittlemaid.inventory.container.task.TaskConfigContainer;
import com.github.tartaricacid.touhoulittlemaid.network.NetworkHandler;
import com.github.tartaricacid.touhoulittlemaid.network.message.MaidSubConfigMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.maid.data.StorageManagerConfigData;
import studio.fantasyit.maid_storage_manager.network.ClientInputPacket;
import studio.fantasyit.maid_storage_manager.network.MaidDataSyncPacket;
import studio.fantasyit.maid_storage_manager.network.Network;
import studio.fantasyit.maid_storage_manager.registry.GuiRegistry;

public class StorageManagerMaidConfigGui extends MaidTaskConfigGui<StorageManagerMaidConfigGui.Container> {
    private StorageManagerConfigData.Data currentMAData;

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
        this.addRenderableWidget(new MaidConfigButton(startLeft, startTop + 0,
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
        this.addRenderableWidget(new MaidConfigButton(startLeft, startTop + 13,
                Component.translatable("gui.maid_storage_manager.config.no_sort_placement"),
                Component.translatable(StorageManagerConfigData.getTranslationKey(this.currentMAData.noSortPlacement())),
                button -> {
                    this.currentMAData.noSortPlacement(false);
                    button.setValue(Component.translatable(StorageManagerConfigData.getTranslationKey(false)));
                    Network.sendMaidDataSync(MaidDataSyncPacket.Type.NoPlaceSort, this.maid.getId(), 0);
                },
                button -> {
                    this.currentMAData.noSortPlacement(true);
                    button.setValue(Component.translatable(StorageManagerConfigData.getTranslationKey(true)));
                    Network.sendMaidDataSync(MaidDataSyncPacket.Type.NoPlaceSort, this.maid.getId(), 1);
                }
        ));
        this.addRenderableWidget(new MaidConfigButton(startLeft, startTop + 26,
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
    }
}
