package studio.fantasyit.maid_storage_manager.craft.action;

import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;

import java.util.ArrayList;
import java.util.List;

public class ActionOptionSet {
    public record ActionOptionSetItem<T>(ActionOption<?> option, T selection, @Nullable String value) {
        public ActionOptionSetItem(ActionOption<?> option, T selection) {
            this(option, selection, null);
        }
    }

    public final List<ActionOptionSetItem<?>> options;

    public ActionOptionSet(List<ActionOptionSetItem<?>> options) {
        this.options = new ArrayList<>(options);
    }

    public <T> ActionOptionSet add(ActionOption<T> option, T selection) {
        this.options.add(new ActionOptionSetItem<>(option, selection));
        return this;
    }

    public <T> ActionOptionSet add(ActionOption<T> option, T selection, @Nullable String value) {
        this.options.add(new ActionOptionSetItem<>(option, selection, value));
        return this;
    }

    public static <T> ActionOptionSet with(ActionOption<T> option, T selection) {
        return new ActionOptionSet(List.of(new ActionOptionSetItem<>(option, selection)));
    }

    public static <T> ActionOptionSet with(ActionOption<T> option, T selection, String value) {
        return new ActionOptionSet(List.of(new ActionOptionSetItem<>(option, selection, value)));
    }

    public void applyTo(CraftGuideStepData data) {
        for (ActionOptionSetItem<?> option : options) {
            data.setOptionSelection((ActionOption) option.option, option.selection);
            if (option.value != null) {
                data.setOptionValue(option.option, option.value);
            }
        }
    }
}
