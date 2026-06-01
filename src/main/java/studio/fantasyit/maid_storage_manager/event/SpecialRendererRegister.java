package studio.fantasyit.maid_storage_manager.event;

import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.items.render.CraftGuideSMR;
import studio.fantasyit.maid_storage_manager.items.render.FilterListSMR;
import studio.fantasyit.maid_storage_manager.items.render.LogisticsGuideSMR;

@EventBusSubscriber(modid = MaidStorageManager.MODID, value = Dist.CLIENT)
public class SpecialRendererRegister {
    public static final Identifier FILTER_LIST = Identifier.fromNamespaceAndPath(MaidStorageManager.MODID, "filter_list");
    public static final Identifier CRAFT_GUIDE = Identifier.fromNamespaceAndPath(MaidStorageManager.MODID, "craft_guide");
    public static final Identifier LOGISTICS_GUIDE = Identifier.fromNamespaceAndPath(MaidStorageManager.MODID, "logistics_guide");

    @SubscribeEvent
    public static void registerSpecialRenderers(RegisterSpecialModelRendererEvent event) {
        event.register(FILTER_LIST, FilterListSMR.Unbaked.MAP_CODEC);
        event.register(CRAFT_GUIDE, CraftGuideSMR.Unbaked.MAP_CODEC);
        event.register(LOGISTICS_GUIDE, LogisticsGuideSMR.Unbaked.MAP_CODEC);
    }
}
