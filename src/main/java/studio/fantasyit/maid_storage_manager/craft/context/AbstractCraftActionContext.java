package studio.fantasyit.maid_storage_manager.craft.context;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.nbt.CompoundTag;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.work.CraftLayer;

public abstract class AbstractCraftActionContext {

    public enum Result {
        SUCCESS,
        FAIL,
        CONTINUE,
        CONTINUE_INTERRUPTABLE,
        NOT_DONE,
        NOT_DONE_INTERRUPTABLE
    }

    protected CraftGuideStepData craftGuideStepData;
    protected CraftGuideData craftGuideData;
    protected int idx;
    protected EntityMaid maid;
    protected CraftLayer craftLayer;

    public AbstractCraftActionContext(EntityMaid maid, CraftGuideData craftGuideData, CraftGuideStepData craftGuideStepData, CraftLayer layer) {
        this.craftGuideData = craftGuideData;
        this.craftGuideStepData = craftGuideStepData;
        this.maid = maid;
        this.craftLayer = layer;
    }

    public abstract Result start();

    public abstract Result tick();

    public abstract void stop();

    public void loadEnv(CompoundTag env) {

    }

    public CompoundTag saveEnv(CompoundTag env) {
        return env == null ? new CompoundTag() : env.copy();
    }
}
