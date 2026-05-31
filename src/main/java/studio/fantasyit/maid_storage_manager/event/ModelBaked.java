package studio.fantasyit.maid_storage_manager.event;

import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;

@EventBusSubscriber(modid = MaidStorageManager.MODID, value = Dist.CLIENT)
public class ModelBaked {
    @SubscribeEvent
    public static void onModelBakedAdd(ModelEvent.RegisterStandalone event) {
        event.register(
                new StandaloneModelKey<>(() -> Identifier.fromNamespaceAndPath(MaidStorageManager.MODID, "item/filter_list_base").toString()),
                SimpleUnbakedStandaloneModel.quadCollection(Identifier.fromNamespaceAndPath(MaidStorageManager.MODID, "item/filter_list_base"))
        );
        event.register(
                new StandaloneModelKey<>(() -> Identifier.fromNamespaceAndPath(MaidStorageManager.MODID, "item/craft_guide_base").toString()),
                SimpleUnbakedStandaloneModel.quadCollection(Identifier.fromNamespaceAndPath(MaidStorageManager.MODID, "item/craft_guide_base"))
        );
        event.register(
                new StandaloneModelKey<>(() -> Identifier.fromNamespaceAndPath(MaidStorageManager.MODID, "item/craft_guide_base_blank").toString()),
                SimpleUnbakedStandaloneModel.quadCollection(Identifier.fromNamespaceAndPath(MaidStorageManager.MODID, "item/craft_guide_base_blank"))
        );
    }
}