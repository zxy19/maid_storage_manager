package studio.fantasyit.maid_storage_manager.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;

public class SoundEventRegistry {
    public static DeferredRegister<SoundEvent> SOUND = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MaidStorageManager.MODID);


    public static ResourceLocation craft_action_roll = new ResourceLocation(MaidStorageManager.MODID, "craft_action_roll");
    public static RegistryObject<SoundEvent> CRAFT_ACTION_ROLL = SOUND.register("craft_action_roll", () -> SoundEvent.createVariableRangeEvent(craft_action_roll));

    public static void register(IEventBus event) {
        SOUND.register(event);
    }
}
