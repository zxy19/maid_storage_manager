package studio.fantasyit.maid_storage_manager.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;

public class SoundEventRegistry {
    public static DeferredRegister<SoundEvent> SOUND = DeferredRegister.create(Registries.SOUND_EVENT, MaidStorageManager.MODID);


    public static Identifier craft_action_roll = Identifier.fromNamespaceAndPath(MaidStorageManager.MODID, "craft_action_roll");
    public static DeferredHolder<SoundEvent, SoundEvent> CRAFT_ACTION_ROLL = SOUND.register("craft_action_roll", () -> SoundEvent.createVariableRangeEvent(craft_action_roll));

    public static void register(IEventBus event) {
        SOUND.register(event);
    }
}
