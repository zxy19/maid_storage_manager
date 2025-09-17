package studio.fantasyit.tour_guide.helper;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.tour_guide.data.TourData;
import studio.fantasyit.tour_guide.mark.IMark;
import studio.fantasyit.tour_guide.mark.ServerScreenPredicatorMarks;
import studio.fantasyit.tour_guide.mark.gui.GuiMainTipMark;
import studio.fantasyit.tour_guide.step.ITourStepData;
import studio.fantasyit.tour_guide.step.TourStepId;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class TourStepBuilder<T> {
    private final TourDataBuilder builder;
    private TourStepId<T> id;
    private List<Component> chat = null;
    private BiConsumer<List<Component>, TourData> dynamicChats = null;

    private List<IMark> marks = null;
    private BiConsumer<List<IMark>, TourData> dynamicMarks = null;

    private boolean allowSkip = true;

    private Function<TourData, Component> unfinishReason = null;
    private Function<TourData, T> finishCb = null;
    private Consumer<TourData> skippedCb = null;

    private Consumer<TourData> initCb = null;

    private Predicate<TourData> autoSkip = null;

    private List<String> triggers = null;
    private Predicate<String> triggerPredicate = null;
    private Component mainTip = null;
    private Function<TourData, T> toGetData = null;
    private Component onNoData = null;

    public TourStepBuilder(TourDataBuilder builder, TourStepId<T> id) {
        this.builder = builder;
        this.id = id;
    }

    public TourStepBuilder() {
        this.builder = null;
    }


    public TourStepBuilder<T> chat(Component chat) {
        this.chat = List.of(chat);
        return this;
    }

    public TourStepBuilder<T> chat(List<Component> chat) {
        this.chat = chat;
        return this;
    }

    public TourStepBuilder<T> dynamicChats(BiConsumer<List<Component>, TourData> dynamicChats) {
        this.dynamicChats = dynamicChats;
        return this;
    }

    public TourStepBuilder<T> mainTipNoGui(Component tip) {
        this.mainTip = tip;
        return this;
    }

    public TourStepBuilder<T> marks(List<IMark> marks) {
        this.marks = marks;
        return this;
    }

    public TourStepBuilder<T> dynamicMarks(BiConsumer<List<IMark>, TourData> dynamicMarks) {
        this.dynamicMarks = dynamicMarks;
        return this;
    }

    public TourStepBuilder<T> allowSkip(boolean allowSkip) {
        this.allowSkip = allowSkip;
        return this;
    }

    public TourStepBuilder<T> unfinishReason(Function<TourData, Component> unfinishReason) {
        this.unfinishReason = unfinishReason;
        return this;
    }

    public TourStepBuilder<T> onFinish(Function<TourData, T> finishCb) {
        this.finishCb = finishCb;
        return this;
    }

    public TourStepBuilder<T> onSkipped(Consumer<TourData> skippedCb) {
        this.skippedCb = skippedCb;
        return this;
    }

    public TourStepBuilder<T> onInit(Consumer<TourData> initCb) {
        this.initCb = initCb;
        return this;
    }

    public TourStepBuilder<T> autoSkip(Predicate<TourData> autoSkip) {
        this.autoSkip = autoSkip;
        return this;
    }

    public TourStepBuilder<T> triggers(List<String> triggers) {
        this.triggers = triggers;
        return this;
    }

    public TourStepBuilder<T> triggerPredicate(Predicate<String> triggerPredicate) {
        this.triggerPredicate = triggerPredicate;
        return this;
    }

    public TourStepBuilder<T> withToGetData(Function<TourData, T> dataGetter, Component onNoData) {
        this.toGetData = dataGetter;
        this.onNoData = onNoData;
        return this;
    }

    public ITourStepData<T> build() {
        return new ITourStepData<T>() {
            @Override
            public TourStepId<T> getId() {
                return id;
            }

            @Override
            public @Nullable List<Component> getChatText(TourData data) {
                if (dynamicChats == null && chat == null)
                    return null;
                ArrayList<Component> tChat = new ArrayList<>();
                if (chat != null)
                    tChat.addAll(chat);
                if (dynamicChats != null)
                    dynamicChats.accept(tChat, data);
                return tChat;
            }

            @Override
            public List<IMark> init(TourData data) {
                if (initCb != null)
                    initCb.accept(data);
                if (dynamicMarks == null && marks == null && mainTip == null)
                    return List.of();
                ArrayList<IMark> tMarks = new ArrayList<>();
                if (mainTip != null)
                    tMarks.add(new GuiMainTipMark(ServerScreenPredicatorMarks.NO_GUI, mainTip, allowSkip));
                if (marks != null)
                    tMarks.addAll(marks);
                if (dynamicMarks != null)
                    dynamicMarks.accept(tMarks, data);
                return tMarks;
            }

            @Override
            public @Nullable Component getUnfinishReason(TourData data) {
                Component d = null;
                if (unfinishReason != null)
                    d = unfinishReason.apply(data);
                if (d != null && toGetData != null && toGetData.apply(data) == null)
                    d = onNoData;
                return d;
            }

            @Override
            public T finish(TourData data) {
                T d = null;
                if (finishCb != null)
                    d = finishCb.apply(data);
                if (toGetData != null)
                    return toGetData.apply(data);
                return d;
            }

            @Override
            public boolean allowSkip() {
                return allowSkip;
            }

            @Override
            public void skipped(TourData data) {
                if (skippedCb != null)
                    skippedCb.accept(data);
            }

            @Override
            public boolean shouldSkipAtStart(TourData data) {
                return autoSkip != null && autoSkip.test(data);
            }

            @Override
            public boolean receiveTrigger(String key) {
                if (triggerPredicate != null && triggerPredicate.test(key))
                    return true;
                if (triggers != null && triggers.contains(key))
                    return true;
                return false;
            }
        };
    }

    public TourDataBuilder add() {
        if (builder == null)
            throw new RuntimeException("TourStepBuilder must be added to TourDataBuilder");
        builder.add(build());
        return builder;
    }
}
