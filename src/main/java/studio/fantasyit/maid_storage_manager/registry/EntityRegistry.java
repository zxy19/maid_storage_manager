package studio.fantasyit.maid_storage_manager.registry;


import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.entity.VirtualDisplayEntity;
import studio.fantasyit.maid_storage_manager.entity.VirtualDisplayEntityRender;
import studio.fantasyit.maid_storage_manager.entity.VirtualItemEntity;
import studio.fantasyit.maid_storage_manager.entity.VirtualItemEntityRender;

@EventBusSubscriber(modid = MaidStorageManager.MODID, bus = EventBusSubscriber.Bus.MOD)
public class EntityRegistry {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, MaidStorageManager.MODID);
    public static final DeferredHolder<EntityType<?>, EntityType<VirtualDisplayEntity>> VIRTUAL_DISPLAY_ENTITY = ENTITY_TYPES.register("virtual_display",
            () -> EntityType.Builder.of(VirtualDisplayEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(10)
                    .updateInterval(Integer.MAX_VALUE)
                    .build("virtual_display")
    );
    public static final DeferredHolder<EntityType<?>, EntityType<VirtualItemEntity>> VIRTUAL_ITEM_ENTITY = ENTITY_TYPES.register("virtual_item",
            () -> EntityType.Builder.of(VirtualItemEntity::new, MobCategory.MISC)
                    .sized(0.3F, 0.3F)
                    .clientTrackingRange(10)
                    .build("virtual_item")
    );


    public static void init(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
    }

    @SubscribeEvent
    public static void registerModel(FMLClientSetupEvent event) {
        EntityRenderers.register(VIRTUAL_DISPLAY_ENTITY.get(), VirtualDisplayEntityRender::new);
        EntityRenderers.register(VIRTUAL_ITEM_ENTITY.get(), VirtualItemEntityRender::new);
    }
}
