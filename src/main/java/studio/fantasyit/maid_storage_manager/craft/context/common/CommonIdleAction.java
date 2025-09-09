package studio.fantasyit.maid_storage_manager.craft.context.common;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.StringUtils;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.craft.action.ActionOption;
import studio.fantasyit.maid_storage_manager.craft.context.AbstractCraftActionContext;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.work.CraftLayer;

import java.util.Objects;

public class CommonIdleAction extends AbstractCraftActionContext {
    public static final ActionOption<Boolean> OPTION_WAIT = new ActionOption<>(
            new ResourceLocation(MaidStorageManager.MODID, "wait"),
            new Component[]{
                    Component.translatable("gui.maid_storage_manager.craft_guide.common.idle_second"),
                    Component.translatable("gui.maid_storage_manager.craft_guide.common.idle_tick")
            },
            new ResourceLocation[]{
                    new ResourceLocation("maid_storage_manager:textures/gui/craft/option/wait_second.png"),
                    new ResourceLocation("maid_storage_manager:textures/gui/craft/option/wait_tick.png")
            },
            "",
            new ActionOption.BiConverter<>(
                    i -> i != 0, b -> b ? 1 : 0
            ),
            ActionOption.ValuePredicatorOrGetter.predicator(t -> (t.isBlank() || (StringUtils.isNumeric(t) && Integer.parseInt(t) <= 999)))
    );
    public static final ResourceLocation TYPE = new ResourceLocation(MaidStorageManager.MODID, "idle");

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
        boolean u = craftGuideStepData.getOptionSelection(OPTION_WAIT).orElse(false);
        String timeStr = craftGuideStepData.getOptionValue(OPTION_WAIT);
        if (timeStr.isBlank())
            timeStr = "0";
        int time = Integer.parseInt(timeStr);
        int v = time * (u ? 1 : 20);
        if (endTick == 0)
            endTick = Objects.requireNonNull(maid.level().getServer()).getTickCount() + v;
        return Result.CONTINUE;
    }

    @Override
    public Result tick() {
        if (maid.getDeltaMovement().length() > 0.1) return Result.NOT_DONE;
        if (Objects.requireNonNull(maid.level().getServer()).getTickCount() < endTick)
            return Result.CONTINUE_INTERRUPTABLE;
        return Result.SUCCESS;
    }

    @Override
    public void stop() {
    }
}
