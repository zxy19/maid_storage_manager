package studio.fantasyit.maid_storage_manager.event;

import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.argument.CraftingDebugControlArgument;
import studio.fantasyit.maid_storage_manager.craft.debug.CraftingDebugContext;
import studio.fantasyit.maid_storage_manager.craft.debug.CraftingDebugManager;

@EventBusSubscriber(modid = MaidStorageManager.MODID, bus = EventBusSubscriber.Bus.GAME)
public class CommandRegisterEvent {

    @SubscribeEvent
    public static void onRegisterCommand(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("maid_storage_manager")
                        .then(Commands.literal("debug_crafting")
                                .then(Commands.argument("control", new CraftingDebugControlArgument())
                                        .executes(t -> {
                                            CraftingDebugContext d = CraftingDebugManager.prepareForPlayer(t.getSource().getPlayerOrException().getUUID(), t.getArgument("control", String.class));
                                            t.getSource().sendSystemMessage(Component.literal("Crafting debug prepared with ID " + d.id));
                                            return 1;
                                        }))
                                .executes(t -> {
                                    CraftingDebugContext d = CraftingDebugManager.prepareForPlayer(t.getSource().getPlayerOrException().getUUID(), "");
                                    t.getSource().sendSystemMessage(Component.literal("Crafting debug prepared with ID " + d.id));
                                    return 1;
                                }))
        );
    }
}
