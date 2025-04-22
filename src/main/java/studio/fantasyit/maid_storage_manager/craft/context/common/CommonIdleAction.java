package studio.fantasyit.maid_storage_manager.craft.context.common;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.craft.context.AbstractCraftActionContext;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftLayer;

import java.util.Objects;

public class CommonIdleAction extends AbstractCraftActionContext {
    public static final ResourceLocation TYPE = new ResourceLocation(MaidStorageManager.MODID, "idle");

    public CommonIdleAction(EntityMaid maid, CraftGuideData craftGuideData, CraftGuideStepData craftGuideStepData, CraftLayer layer) {
        super(maid, craftGuideData, craftGuideStepData, layer);
    }

    int endTick = 0;

    @Override
    public Result start() {
        CompoundTag extraData = craftGuideStepData.getExtraData();
        int time = extraData.getInt("time");
        int u = extraData.getInt("u");
        int v = time * (u == 0 ? 1 : 20);
        endTick = Objects.requireNonNull(maid.level().getServer()).getTickCount() + v;
        return Result.CONTINUE;
    }

    @Override
    public Result tick() {
        if (maid.getDeltaMovement().length() > 0.1) return Result.CONTINUE;
        if (Objects.requireNonNull(maid.level().getServer()).getTickCount() < endTick)
            return Result.CONTINUE;
        return Result.SUCCESS;
    }

    @Override
    public void stop() {
    }
}
