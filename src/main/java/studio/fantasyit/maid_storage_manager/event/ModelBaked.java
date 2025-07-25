package studio.fantasyit.maid_storage_manager.event;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.items.render.CustomEmptyModel;
import studio.fantasyit.maid_storage_manager.items.render.CustomItemRenderer;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = MaidStorageManager.MODID, value = Dist.CLIENT)
public class ModelBaked {
    @SubscribeEvent
    public static void onModelBaked(ModelEvent.ModifyBakingResult event) {
        event.getModels().put(new ModelResourceLocation(ResourceLocation.fromNamespaceAndPath(MaidStorageManager.MODID, "filter_list"), "inventory"), new CustomEmptyModel());
        event.getModels().put(new ModelResourceLocation(ResourceLocation.fromNamespaceAndPath(MaidStorageManager.MODID, "craft_guide"), "inventory"), new CustomEmptyModel());
    }

    @SubscribeEvent
    public static void onModelBaked(ModelEvent.RegisterAdditional event) {
        event.register(new ModelResourceLocation(ResourceLocation.fromNamespaceAndPath(MaidStorageManager.MODID, "filter_list_base"), "inventory"));
        event.register(new ModelResourceLocation(ResourceLocation.fromNamespaceAndPath(MaidStorageManager.MODID, "craft_guide_base"), "inventory"));
        event.register(new ModelResourceLocation(ResourceLocation.fromNamespaceAndPath(MaidStorageManager.MODID, "craft_guide_base_blank"), "inventory"));
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(
                new IClientItemExtensions() {
                    @Override
                    public @NotNull BlockEntityWithoutLevelRenderer getCustomRenderer() {
                        return CustomItemRenderer.getInstance();
                    }
                },
                ItemRegistry.CRAFT_GUIDE, ItemRegistry.FILTER_LIST
        );
    }
}