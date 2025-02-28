package studio.fantasyit.maid_storage_manager.event;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.items.render.FilterListModel;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = MaidStorageManager.MODID, value = Dist.CLIENT)
public class ModelBaked {
    @SubscribeEvent
    public static void onModelBaked(ModelEvent.ModifyBakingResult event) {
        event.getModels().put(new ModelResourceLocation(MaidStorageManager.MODID, "filter_list", "inventory"), new FilterListModel());
    }
    @SubscribeEvent
    public static void onModelBaked(ModelEvent.RegisterAdditional event) {
        event.register(new ModelResourceLocation(MaidStorageManager.MODID, "filter_list_base", "inventory"));
    }
}
