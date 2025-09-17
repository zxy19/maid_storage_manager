package studio.fantasyit.tour_guide.event;


import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.tour_guide.api.TourManager;
import studio.fantasyit.tour_guide.data.TourData;

@Mod.EventBusSubscriber(modid = MaidStorageManager.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RegisterCommandEvent {

    @SubscribeEvent
    public static void onRegisterCommand(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("tour_guide")
                        .then(Commands.literal("start")
                                .then(Commands.argument("id", new ResourceLocationArgument())
                                        .executes(t -> {
                                            ServerPlayer player = t.getSource().getPlayerOrException();
                                            if (TourManager.start(player, ResourceLocationArgument.getId(t, "id")))
                                                return 1;
                                            return 0;
                                        })
                                )
                        )
                        .then(Commands.literal("stop")
                                .executes(t -> {
                                    TourManager.stop(t.getSource().getPlayerOrException());
                                    return 1;
                                })
                        )
                        .then(Commands.literal("check")
                                .executes(t -> {
                                    TourData tourData = TourManager.get(t.getSource().getPlayerOrException());
                                    if (tourData == null)
                                        return 0;
                                    tourData.doneAndTryNextStep();
                                    return 1;
                                })
                        )
                        .then(Commands.literal("skip")
                                .executes(t -> {
                                    TourData tourData = TourManager.get(t.getSource().getPlayerOrException());
                                    if (tourData == null)
                                        return 0;
                                    tourData.skipAndTryNextStep();
                                    return 1;
                                })
                        )
        );
    }

}
