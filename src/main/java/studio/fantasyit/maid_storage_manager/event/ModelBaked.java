package studio.fantasyit.maid_storage_manager.event;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.items.render.CustomEmptyModel;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = MaidStorageManager.MODID, value = Dist.CLIENT)
public class ModelBaked {
    @SubscribeEvent
    public static void onModelBaked(ModelEvent.ModifyBakingResult event) {
        event.getModels().put(new ModelResourceLocation(MaidStorageManager.MODID, "filter_list", "inventory"), new CustomEmptyModel());
        event.getModels().put(new ModelResourceLocation(MaidStorageManager.MODID, "craft_guide", "inventory"), new CustomEmptyModel());
    }
    @SubscribeEvent
    public static void onModelBaked(ModelEvent.RegisterAdditional event) {
        event.register(new ModelResourceLocation(MaidStorageManager.MODID, "filter_list_base", "inventory"));
        event.register(new ModelResourceLocation(MaidStorageManager.MODID, "craft_guide_base", "inventory"));
        event.register(new ModelResourceLocation(MaidStorageManager.MODID, "craft_guide_base_blank", "inventory"));
    }
}