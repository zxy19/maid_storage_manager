package studio.fantasyit.tour_guide.data;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.tour_guide.mark.IMark;

import java.util.List;

public interface ITourStepData<T> {
    ResourceLocation getId();

    List<IMark> init(TourData data);

    default boolean allowSkip() {
        return true;
    }

    @Nullable Component getUnfinishReason();

    default boolean checkIfFinished() {
        return getUnfinishReason() == null;
    }

    void skipped();

    T finish();

    default boolean receiveTrigger(String key) {
        return false;
    }
}
