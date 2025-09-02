package studio.fantasyit.maid_storage_manager.menu.craft.common;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.craft.action.ActionOption;
import studio.fantasyit.maid_storage_manager.craft.action.CraftAction;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.menu.container.FilterContainer;
import studio.fantasyit.maid_storage_manager.menu.container.ISaveFilter;

import java.util.List;

public class CommonStepDataContainer extends FilterContainer implements ISaveFilter {
    public CraftGuideStepData step;
    CraftAction actionType;
    public int inputCount = 0;
    public int padCount = 4;
    public int outputCount = 0;
    public List<ActionOption<?>> options = List.of();

    public CommonStepDataContainer(AbstractContainerMenu menu) {
        super(4, menu);
    }


    public void setStep(CraftGuideStepData step) {
        this.step = step;
        actionType = step.actionType;
        recalculateSlots();
        for (int i = 0; i < inputCount; i++) {
            setItemNoTrigger(i, step.getInput().get(i));
            setCount(i, step.getInput().get(i).getCount());
        }
        for (int i = 0; i < outputCount; i++) {
            setItemNoTrigger(inputCount + padCount + i, step.getOutput().get(i));
            setCount(inputCount + padCount + i, step.getOutput().get(i).getCount());
        }
        options = actionType.options();
    }

    public void clearStep() {
        step = null;
        inputCount = 0;
        padCount = 4;
        outputCount = 0;
        for (int i = 0; i < 4; i++) {
            setItemNoTrigger(i, ItemStack.EMPTY);
            setCount(i, 0);
        }
        options = List.of();
    }

    private void recalculateSlots() {
        inputCount = step.actionType.inputCount();
        outputCount = step.actionType.outputCount();
        if (inputCount + outputCount < 4)
            padCount = 4 - inputCount - outputCount;
        else padCount = 0;
        if (inputCount + outputCount > 4)
            outputCount = 4 - inputCount;
        if (outputCount < 0) {
            outputCount = 0;
            inputCount = 4;
        }
    }


    @Override
    public int getContainerSize() {
        return 4;
    }

    public void setAction(ResourceLocation action) {
        if (step == null)
            return;
        step.setAction(action);
        actionType = step.actionType;
        options = actionType.options();
        recalculateSlots();
        step.setExtraData(new CompoundTag());
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
    }

    public void setOption(int index, int selection, String value) {
        options.get(index).setOptionSelectionId(step, selection);
        options.get(index).setOptionValue(step, value);
    }

}
