package studio.fantasyit.maid_storage_manager.integration.create;

import net.minecraftforge.common.MinecraftForge;
import studio.fantasyit.maid_storage_manager.integration.Integrations;

public class CreateIntegration {
    public static void init() {
        if (Integrations.createStockManager())
            MinecraftForge.EVENT_BUS.addListener(AddCreateStockButtonForMaid::addStockButton);
    }

}
