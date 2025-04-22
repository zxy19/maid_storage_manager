package studio.fantasyit.maid_storage_manager.menu.craft.base;

import net.minecraft.world.inventory.AbstractContainerMenu;
import studio.fantasyit.maid_storage_manager.craft.action.CraftAction;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.menu.container.FilterContainer;
import studio.fantasyit.maid_storage_manager.menu.container.ISaveFilter;

public class StepDataContainer extends FilterContainer implements ISaveFilter {
    public final CraftGuideStepData step;
    CraftAction actionType;
    public boolean optional;
    public boolean matchTag;
    public int inputCount = 0;
    public int outputCount = 0;

    public StepDataContainer(CraftGuideStepData step, AbstractContainerMenu menu) {
        super(step.actionType.inputCount() + step.actionType.outputCount(), menu);
        this.step = step;
        actionType = step.actionType;
        inputCount = step.actionType.inputCount();
        outputCount = step.actionType.outputCount();
        for (int i = 0; i < inputCount; i++) {
            setItemNoTrigger(i, step.getInput().get(i));
            setCount(i, step.getInput().get(i).getCount());
        }
        for (int i = 0; i < outputCount; i++) {
            setItemNoTrigger(inputCount + i, step.getOutput().get(i));
            setCount(inputCount + i, step.getOutput().get(i).getCount());
        }
        matchTag = step.isMatchTag();
    }

    @Override
    public int getContainerSize() {
        return actionType.inputCount() + actionType.outputCount();
    }

    @Override
    public void save() {
        for (int i = 0; i < inputCount; i++) {
            int count = this.count[i].getValue();
            step.setInput(i, getItem(i).copyWithCount(count));
        }
        for (int i = 0; i < outputCount; i++) {
            int count = this.count[inputCount + i].getValue();
            step.setOutput(i, getItem(inputCount + i).copyWithCount(count));
        }
        step.optional = optional;
        step.matchTag = matchTag;
    }
}
