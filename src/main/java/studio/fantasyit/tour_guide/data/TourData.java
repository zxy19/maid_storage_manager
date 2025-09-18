package studio.fantasyit.tour_guide.data;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.tour_guide.api.TourManager;
import studio.fantasyit.tour_guide.mark.IMark;
import studio.fantasyit.tour_guide.network.Network;
import studio.fantasyit.tour_guide.network.S2CTipMessage;
import studio.fantasyit.tour_guide.network.S2CUpdateTourGuideData;
import studio.fantasyit.tour_guide.step.ITourStepData;
import studio.fantasyit.tour_guide.step.TourStepId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TourData {
    private final ServerPlayer player;
    private final List<ITourStepData<?>> steps;
    private final Map<ResourceLocation, Object> data;
    private final ResourceLocation id;
    private Runnable onStart;
    private Runnable onFinish;

    private ITourStepData<?> currentStep = null;
    private int currentStepIndex = -1;

    public TourData(List<ITourStepData<?>> steps, ResourceLocation id, ServerPlayer player) {
        this.id = id;
        this.steps = steps;
        this.data = new HashMap<>();
        this.player = player;
    }


    public List<ITourStepData<?>> getSteps() {
        return steps;
    }

    public void doneAndTryNextStep() {
        if (currentStep == null)
            throw new IllegalStateException("TourData is not started");
        Component unfinishReason = currentStep.getUnfinishReason(this);
        if (unfinishReason != null) {
            player.sendSystemMessage(unfinishReason);
            return;
        }
        data.put(currentStep.getId().id(), currentStep.finish(this));
        tryNextStep();
    }

    public void tryNextStep() {
        currentStepIndex++;
        if (currentStepIndex >= steps.size()) {
            currentStepIndex = steps.size();
            currentStep = null;
            stop();
            return;
        }
        currentStep = steps.get(currentStepIndex);
        startCurrentAndSync();
    }

    public void start() {
        player.sendSystemMessage(Component.translatable("message.tour_guide.start", Component.translatable("tour.tour_guide." + id.toLanguageKey())).withStyle(ChatFormatting.GREEN));
        if (onStart != null) {
            onStart.run();
        }
        tryNextStep();
    }

    public boolean isFinished() {
        return currentStepIndex >= steps.size();
    }

    public void receiveTrigger(String key) {
        if (currentStep != null && currentStep.receiveTrigger(key) && currentStep.checkIfFinished(this)) {
            doneAndTryNextStep();
        }
    }

    public void skipAndTryNextStep() {
        if (currentStep == null)
            throw new IllegalStateException("TourData is not started");
        if (!currentStep.allowSkip()) {
            player.sendSystemMessage(Component.translatable("message.tour_guide.cannot_skip").withStyle(ChatFormatting.YELLOW));
            return;
        }
        currentStep.skipped(this);
        tryNextStep();
    }


    public void startCurrentAndSync() {
        if (currentStep == null)
            throw new IllegalStateException("TourData is not started");
        List<IMark> init = currentStep.init(this);
        @Nullable List<Component> chats = currentStep.getChatText(this);
        player.sendSystemMessage(Component.translatable("message.tour_guide.step",
                Component.translatable("tour.tour_guide." + id.toLanguageKey())
                , currentStepIndex + 1, steps.size()));
        if (chats != null) {
            for (Component chat : chats) {
                player.sendSystemMessage(Component.translatable("message.tour_guide.prefix").append(chat));
            }
        }
        Network.INSTANCE
                .send(PacketDistributor.PLAYER.with(() -> player), new S2CTipMessage(currentStep.allowSkip()));
        Network.INSTANCE
                .send(PacketDistributor.PLAYER.with(() -> player), new S2CUpdateTourGuideData(init));
    }

    public void stop() {
        if (onFinish != null)
            onFinish.run();
        player.sendSystemMessage(Component.translatable("message.tour_guide.end").withStyle(ChatFormatting.GREEN));
        terminate();
    }

    public void terminate() {
        Network.INSTANCE
                .send(PacketDistributor.PLAYER.with(() -> player), new S2CUpdateTourGuideData(List.of()));
        TourManager.remove(player);
    }


    public TourData setOnStart(Runnable onStart) {
        this.onStart = onStart;
        return this;
    }

    public TourData setOnFinish(Runnable onFinish) {
        this.onFinish = onFinish;
        return this;
    }

    public <T> T getData(ResourceLocation id) {
        return (T) data.get(id);
    }

    public <T> T getData(TourStepId<T> id) {
        return getData(id.id());
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public void goPrevStep() {
        if (currentStepIndex <= 0)
            return;
        currentStepIndex--;
        currentStep = steps.get(currentStepIndex);
        startCurrentAndSync();
    }
}
