package studio.fantasyit.maid_storage_manager.menu.craft.common;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import studio.fantasyit.maid_storage_manager.craft.action.CraftAction;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.menu.container.FilterContainer;
import studio.fantasyit.maid_storage_manager.menu.container.ISaveFilter;

public class CommonStepDataContainer extends FilterContainer implements ISaveFilter {
    public final CraftGuideStepData step;
    CraftAction actionType;
    public boolean optional;
    public boolean matchTag;
    public int inputCount = 0;
    public int padCount = 0;
    public int outputCount = 0;

    public CommonStepDataContainer(CraftGuideStepData step, AbstractContainerMenu menu) {
        super(3, menu);
        this.step = step;
        actionType = step.actionType;
        inputCount = step.actionType.inputCount();
        outputCount = step.actionType.outputCount();
        if (inputCount + outputCount < 3)
            padCount = 3 - inputCount - outputCount;
        else padCount = 0;
        if (inputCount + outputCount > 3)
            outputCount = 3 - inputCount;
        if (outputCount < 0) {
            outputCount = 0;
            inputCount = 3;
        }
        for (int i = 0; i < inputCount; i++) {
            setItemNoTrigger(i, step.getInput().get(i));
            setCount(i, step.getInput().get(i).getCount());
        }
        int inputOffset = inputCount == 0 ? 0 : Math.max(inputCount, 2);
        for (int i = 0; i < outputCount; i++) {
            setItemNoTrigger(inputOffset + i, step.getOutput().get(i));
            setCount(inputOffset + i, step.getOutput().get(i).getCount());
        }
        optional = step.isOptional();
    }

    @Override
    public int getContainerSize() {
        return actionType.inputCount() + actionType.outputCount() + padCount;
    }

    public void setAction(ResourceLocation action) {
        step.setAction(action);

        actionType = step.actionType;
        inputCount = step.actionType.inputCount();
        outputCount = step.actionType.outputCount();
        if (inputCount + outputCount < 3)
            padCount = 3 - inputCount - outputCount;
        else padCount = 0;
        if (inputCount + outputCount > 3)
            outputCount = 3 - inputCount;
        if (outputCount < 0) {
            outputCount = 0;
            inputCount = 3;
        }
    }

    @Override
    public void save() {
        if (inputCount != 0)
            for (int i = 0; i < inputCount; i++) {
                int count = this.count[i].getValue();
                step.setInput(i, getItem(i).copyWithCount(count));
            }
        else
            step.clearInput();

        int inputOffset = inputCount + padCount;
        if (outputCount != 0)
            for (int i = 0; i < outputCount; i++) {
                int count = this.count[inputOffset + i].getValue();
                step.setOutput(i, getItem(inputOffset + i).copyWithCount(count));
            }
        else
            step.clearOutput();
        step.optional = optional;
    }

}
