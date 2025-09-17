package studio.fantasyit.tour_guide.data;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;
import studio.fantasyit.tour_guide.api.TourManager;
import studio.fantasyit.tour_guide.mark.IMark;
import studio.fantasyit.tour_guide.network.Network;
import studio.fantasyit.tour_guide.network.S2CUpdateTourGuideData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TourData {
    private final ServerPlayer player;
    private final List<ITourStepData<?>> steps;
    private final Map<ResourceLocation, Object> data;
    private Runnable onStart;
    private Runnable onFinish;

    private ITourStepData<?> currentStep = null;
    private int currentStepIndex = -1;

    public TourData(List<ITourStepData<?>> steps, ServerPlayer player) {
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
        Component unfinishReason = currentStep.getUnfinishReason();
        if (unfinishReason != null) {
            player.sendSystemMessage(unfinishReason);
            return;
        }
        data.put(currentStep.getId(), currentStep.finish());
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
        if (onStart != null)
            onStart.run();
        tryNextStep();
    }

    public boolean isFinished() {
        return currentStepIndex >= steps.size();
    }

    public void receiveTrigger(String key) {
        if (currentStep != null && currentStep.receiveTrigger(key) && currentStep.checkIfFinished()) {
            doneAndTryNextStep();
        }
    }

    public void skipAndTryNextStep() {
        if (currentStep == null)
            throw new IllegalStateException("TourData is not started");
        if (!currentStep.allowSkip())
            return;
        currentStep.skipped();
        tryNextStep();
    }


    public void startCurrentAndSync() {
        if (currentStep == null)
            throw new IllegalStateException("TourData is not started");
        List<IMark> init = currentStep.init(this);
        Network.INSTANCE
                .send(PacketDistributor.PLAYER.with(() -> player), new S2CUpdateTourGuideData(init));
    }

    public void stop() {
        if (onFinish != null)
            onFinish.run();
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
}
