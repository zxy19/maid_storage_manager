package studio.fantasyit.maid_storage_manager.event;

import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.argument.CraftingDebugControlArgument;
import studio.fantasyit.maid_storage_manager.argument.ProgressDebugControlArgument;
import studio.fantasyit.maid_storage_manager.craft.debug.CraftingDebugContext;
import studio.fantasyit.maid_storage_manager.craft.debug.CraftingDebugManager;
import studio.fantasyit.maid_storage_manager.craft.debug.ProgressDebugManager;

@Mod.EventBusSubscriber(modid = MaidStorageManager.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
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
                        .then(Commands.literal("debug_maid")
                                .then(Commands.argument("control", new ProgressDebugControlArgument())
                                        .executes(t -> {
                                            ProgressDebugManager.preparePlayer(t.getSource().getPlayerOrException(), t.getArgument("control", String.class));
                                            t.getSource().sendSystemMessage(Component.literal("Click at a maid to setup debug context"));
                                            return 1;
                                        }))
                                .executes(t -> {
                                    ProgressDebugManager.preparePlayer(t.getSource().getPlayerOrException(), "");
                                    t.getSource().sendSystemMessage(Component.literal("Click at a maid to setup debug context"));
                                    return 1;
                                }))
        );
    }
}
