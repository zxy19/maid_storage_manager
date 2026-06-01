package studio.fantasyit.maid_storage_manager.event;

import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
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

    public static final StandaloneModelKey<BlockStateModelPart> CRAFT_GUIDE_BLANK_KEY =
            new StandaloneModelKey<>(() -> "craft_guide_blank");

    @SubscribeEvent
    public static void registerStandaloneModels(ModelEvent.RegisterStandalone event) {
        Identifier blankModelId = Identifier.fromNamespaceAndPath(
                MaidStorageManager.MODID, "item/craft_guide_blank");
        event.register(CRAFT_GUIDE_BLANK_KEY,
                SimpleUnbakedStandaloneModel.simpleModelWrapper(blankModelId));
    }
}