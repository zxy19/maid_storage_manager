package studio.fantasyit.maid_storage_manager.event;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;

@EventBusSubscriber(modid = MaidStorageManager.MODID, value = Dist.CLIENT)
public class ModelBaked {
}