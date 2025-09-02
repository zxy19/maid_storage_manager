package studio.fantasyit.maid_storage_manager.craft.action;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public record ActionOption<T>(
        ResourceLocation id,
        Component[] tooltip,
        ResourceLocation[] icon,
        String defaultValue,
        @NotNull BiConverter<Integer, T> converter,
        @NotNull ValuePredicatorOrGetter<T> valuePredicatorOrGetter
) {
    public static class ValuePredicatorOrGetter<T> {
        public final @Nullable Predicate<String> predicate;
        public final @Nullable Function<T, Component> valueGetter;

        public static <T> ValuePredicatorOrGetter<T> predicator(Predicate<String> predicate) {
            return new ValuePredicatorOrGetter<>(predicate, null);
        }

        public static <T> ValuePredicatorOrGetter<T> getter(Function<T, Component> valueGetter) {
            return new ValuePredicatorOrGetter<>(null, valueGetter);
        }

        public ValuePredicatorOrGetter(@Nullable Predicate<String> predicate, @Nullable Function<T, Component> valueGetter) {
            if (predicate == null && valueGetter == null)
                throw new IllegalArgumentException("predicate or valueGetter must not be null");
            this.predicate = predicate;
            this.valueGetter = valueGetter;
        }

        public boolean predicate(String value) {
            if (predicate == null)
                return false;
            return predicate.test(value);
        }

        public Optional<Component> getValue(T value) {
            if (valueGetter == null)
                return Optional.empty();
            return Optional.of(valueGetter.apply(value));
        }

        public boolean hasPredicator() {
            return predicate != null;
        }
    }

    public static class BiConverter<T, R> {
        public final @NotNull Function<T, R> from;
        public final @NotNull Function<R, T> to;

        public BiConverter(@NotNull Function<T, R> from, @NotNull Function<R, T> to) {
            this.from = from;
            this.to = to;
        }

        public R ab(T value) {
            return from.apply(value);
        }

        public T ba(R value) {
            return to.apply(value);
        }
    }

    public ActionOption(ResourceLocation id, String defaultValue, @NotNull BiConverter<Integer, T> converter, @NotNull ValuePredicatorOrGetter<T> valuePredicatorOrGetter) {
        this(id, new Component[0], new ResourceLocation[0], defaultValue, converter, valuePredicatorOrGetter);
    }

    public static ActionOption<Boolean> valueOnly(ResourceLocation id, String defaultValue) {
        return new ActionOption<>(id, defaultValue, new BiConverter<>(value -> true, value -> 0), ValuePredicatorOrGetter.predicator(value -> true));
    }

    public static final ActionOption<Boolean> OPTIONAL = new ActionOption<>(
            new ResourceLocation("maid_storage_manager", "optional"),
            new Component[]{
                    Component.translatable("gui.maid_storage_manager.craft_guide.common.required"),
                    Component.translatable("gui.maid_storage_manager.craft_guide.common.optional")
            },
            new ResourceLocation[]{
                    new ResourceLocation("maid_storage_manager:textures/gui/craft/option/required.png"),
                    new ResourceLocation("maid_storage_manager:textures/gui/craft/option/optional.png")
            },
            "",
            new BiConverter<>(value -> value == 1, value -> value ? 1 : 0),
            ValuePredicatorOrGetter.getter(
                    value -> value ?
                            Component.translatable("gui.maid_storage_manager.craft_guide.common.optional") :
                            Component.translatable("gui.maid_storage_manager.craft_guide.common.required")
            )
    );

    private void assertValid(CraftGuideStepData craftGuideStepData) {
        if (craftGuideStepData.actionType.options().stream().noneMatch(o -> o.id().equals(id)))
            throw new IllegalArgumentException("Option " + id + " not found on action " + craftGuideStepData.action);
    }

    public Optional<T> getOptionSelection(CraftGuideStepData craftGuideStepData) {
        assertValid(craftGuideStepData);
        CompoundTag extraData = craftGuideStepData.getExtraData();
        if (extraData.contains(id.toString()) && extraData.getCompound(id.toString()).contains("selection")) {
            return Optional.of(converter.ab(extraData.getCompound(id.toString()).getInt("selection")));
        }
        return Optional.empty();
    }

    public Optional<Integer> getOptionSelectionId(CraftGuideStepData craftGuideStepData) {
        assertValid(craftGuideStepData);
        CompoundTag extraData = craftGuideStepData.getExtraData();
        if (extraData.contains(id.toString()) && extraData.getCompound(id.toString()).contains("selection")) {
            return Optional.of(extraData.getCompound(id.toString()).getInt("selection"));
        }
        return Optional.empty();
    }

    public String getOptionValue(CraftGuideStepData craftGuideStepData) {
        assertValid(craftGuideStepData);
        CompoundTag extraData = craftGuideStepData.getExtraData();
        if (extraData.contains(id.toString()) && extraData.getCompound(id.toString()).contains("value")) {
            return extraData.getCompound(id.toString()).getString("value");
        }
        return defaultValue();
    }

    public void setOptionSelection(CraftGuideStepData craftGuideStepData, T selection) {
        assertValid(craftGuideStepData);
        setOptionSelectionId(craftGuideStepData, converter.ba(selection));
    }

    public void setOptionSelectionId(CraftGuideStepData craftGuideStepData, int selection) {
        assertValid(craftGuideStepData);
        CompoundTag extraData = craftGuideStepData.getExtraData();
        if (!extraData.contains(id.toString()))
            extraData.put(id.toString(), new CompoundTag());
        extraData.getCompound(id.toString()).putInt("selection", selection);
        craftGuideStepData.setExtraData(extraData);
    }

    public void setOptionValue(CraftGuideStepData craftGuideStepData, String value) {
        assertValid(craftGuideStepData);
        CompoundTag extraData = craftGuideStepData.getExtraData();
        if (!extraData.contains(id.toString()))
            extraData.put(id.toString(), new CompoundTag());
        extraData.getCompound(id.toString()).putString("value", value);
        craftGuideStepData.setExtraData(extraData);
    }

}
