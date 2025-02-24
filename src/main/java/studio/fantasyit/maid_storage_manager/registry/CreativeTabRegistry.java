package studio.fantasyit.maid_storage_manager.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;

public class CreativeTabRegistry {
    public static final String TAB_NAME = "maid_storage_manager_tab_main";
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MaidStorageManager.MODID);
    public static final RegistryObject<CreativeModeTab> CRAFT_TAB =
            CREATIVE_MODE_TABS.register("maid_storage_manager", () ->
                    CreativeModeTab.builder().icon(() -> new ItemStack(ItemRegistry.REQUEST_LIST_ITEM.get()))
                            .title(Component.translatable(TAB_NAME))
                            .displayItems((pParameter, pOutput) -> {
                                ItemRegistry.ITEMS
                                        .getEntries()
                                        .stream()
                                        .map(RegistryObject::get)
                                        .forEach(pOutput::accept);
                            }).build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
