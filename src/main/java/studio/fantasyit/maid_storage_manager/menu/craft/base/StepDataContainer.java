package studio.fantasyit.maid_storage_manager.menu.craft.base;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.craft.CraftManager;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.menu.container.FilterContainer;
import studio.fantasyit.maid_storage_manager.menu.container.ISaveFilter;

public class StepDataContainer implements Container, ISaveFilter {
    public final CraftGuideStepData step;
    public final FilterContainer inputs;
    public final FilterContainer outputs;
    CraftManager.CraftAction actionType;
    public boolean optional;
    public boolean matchTag;

    public StepDataContainer(CraftGuideStepData step, AbstractContainerMenu menu) {
        this.step = step;
        actionType = step.actionType;
        if (step.actionType.inputCount() > 0) {
            inputs = new FilterContainer(step.actionType.inputCount(), menu);
            for (int i = 0; i < step.actionType.inputCount(); i++) {
                inputs.setItem(i, step.getOutput().get(i));
            }
        } else {
            inputs = null;
        }
        if (step.actionType.outputCount() > 0) {
            outputs = new FilterContainer(step.actionType.outputCount(), menu);
            for (int i = 0; i < step.actionType.outputCount(); i++) {
                outputs.setItem(i, step.getOutput().get(i));
            }
        } else {
            outputs = null;
        }
        optional = step.isOptional();
        matchTag = step.isMatchTag();
    }

    @Override
    public int getContainerSize() {
        return actionType.inputCount() + actionType.outputCount();
    }

    @Override
    public boolean isEmpty() {
        return inputs.isEmpty() && outputs.isEmpty();
    }

    @Override
    public ItemStack getItem(int p_18941_) {
        if (p_18941_ < inputs.getContainerSize())
            return inputs.getItem(p_18941_);
        return outputs.getItem(p_18941_ - inputs.getContainerSize());
    }

    @Override
    public ItemStack removeItem(int p_18942_, int p_18943_) {
        if (p_18942_ < inputs.getContainerSize())
            return inputs.removeItem(p_18942_, p_18943_);
        return outputs.removeItem(p_18942_ - inputs.getContainerSize(), p_18943_);
    }

    @Override
    public ItemStack removeItemNoUpdate(int p_18951_) {
        if (p_18951_ < inputs.getContainerSize())
            return inputs.removeItemNoUpdate(p_18951_);
        return outputs.removeItemNoUpdate(p_18951_ - inputs.getContainerSize());
    }

    @Override
    public void setItem(int p_18944_, ItemStack p_18945_) {
        if (p_18944_ < inputs.getContainerSize())
            inputs.setItem(p_18944_, p_18945_);
        else
            outputs.setItem(p_18944_ - inputs.getContainerSize(), p_18945_);
    }

    @Override
    public void setChanged() {
        if (inputs != null)
            inputs.setChanged();
        if (outputs != null)
            outputs.setChanged();
    }

    @Override
    public boolean stillValid(Player p_18946_) {
        return true;
    }

    @Override
    public void clearContent() {
        if (inputs != null)
            inputs.clearContent();
        if (outputs != null)
            outputs.clearContent();
    }
    public void setCount(int index,int count){
        if (index < inputs.getContainerSize())
            inputs.count[index].setValue(count);
        else
            outputs.count[index - inputs.getContainerSize()].setValue(count);
    }
    public int getCount(int index){
        if (index < inputs.getContainerSize())
            return inputs.count[index].getValue();
        else
            return outputs.count[index - inputs.getContainerSize()].getValue();
    }

    @Override
    public void save() {
        for (int i = 0; i < inputs.getContainerSize(); i++) {
            int count = inputs.count[i].getValue();
            step.getInput().set(i, inputs.getItem(i).copyWithCount(count));
        }
        for (int i = 0; i < outputs.getContainerSize(); i++) {
            int count = outputs.count[i].getValue();
            step.getOutput().set(i, outputs.getItem(i).copyWithCount(count));
        }
        step.optional = optional;
        step.matchTag = matchTag;
    }

}
