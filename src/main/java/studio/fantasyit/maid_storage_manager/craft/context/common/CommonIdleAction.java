package studio.fantasyit.maid_storage_manager.craft.context.common;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.craft.context.AbstractCraftActionContext;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.work.CraftLayer;

import java.util.Objects;

public class CommonIdleAction extends AbstractCraftActionContext {
    public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath(MaidStorageManager.MODID, "idle");

    public CommonIdleAction(EntityMaid maid, CraftGuideData craftGuideData, CraftGuideStepData craftGuideStepData, CraftLayer layer) {
        super(maid, craftGuideData, craftGuideStepData, layer);
    }

    int endTick = 0;

    @Override
    public void loadEnv(CompoundTag env) {
        if (env.contains("endTick"))
            endTick = env.getInt("endTick");
        else
            endTick = 0;
    }

    @Override
    public CompoundTag saveEnv(CompoundTag env) {
        env.putInt("endTick", endTick);
        return super.saveEnv(env);
    }

    @Override
    public Result start() {
        CompoundTag extraData = craftGuideStepData.getExtraData();
        int time = extraData.getInt("time");
        int u = extraData.getInt("u");
        int v = time * (u == 0 ? 1 : 20);
        if (endTick == 0)
            endTick = Objects.requireNonNull(maid.level().getServer()).getTickCount() + v;
        return Result.CONTINUE;
    }

    @Override
    public Result tick() {
        if (maid.getDeltaMovement().length() > 0.1) return Result.CONTINUE_INTERRUPTABLE;
        if (Objects.requireNonNull(maid.level().getServer()).getTickCount() < endTick)
            return Result.CONTINUE_INTERRUPTABLE;
        return Result.SUCCESS;
    }

    @Override
    public void stop() {
    }
}
