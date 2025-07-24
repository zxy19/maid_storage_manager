package studio.fantasyit.maid_storage_manager.craft.context.special;

import appeng.api.networking.crafting.*;
import appeng.api.networking.security.IActionHost;
import appeng.api.stacks.AEItemKey;
import appeng.me.helpers.PlayerSource;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonTakeItemAction;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.work.CraftLayer;
import studio.fantasyit.maid_storage_manager.storage.ae2.Ae2BaseContext;
import studio.fantasyit.maid_storage_manager.util.WrappedMaidFakePlayer;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Future;

import static appeng.api.networking.crafting.CraftingSubmitErrorCode.CPU_BUSY;

public class AeCraftingAction extends CommonTakeItemAction {
    public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath(MaidStorageManager.MODID, "ae2_craft");
    int nextCrafting = 0;
    Future<ICraftingPlan> currentCalculating = null;
    Queue<ICraftingLink> craftingLinks = new LinkedList<>();

    int toTick = 0;

    public AeCraftingAction(EntityMaid maid, CraftGuideData craftGuideData, CraftGuideStepData craftGuideStepData, CraftLayer layer) {
        super(maid, craftGuideData, craftGuideStepData, layer);
    }

    @Override
    public Result start() {
        return super.start();
    }

    @Override
    public Result tick() {
        Result result = super.tick();
        if (result == Result.SUCCESS)
            return Result.SUCCESS;
        if (storageContext instanceof Ae2BaseContext ae2BaseContext) {
            if (nextCrafting >= craftGuideStepData.getOutput().size()) {
                if (maid.level().getServer().getTickCount() > toTick)
                    return Result.NOT_DONE;
                return Result.CONTINUE;
            }
            ICraftingService craftingService = ae2BaseContext.getCraftingService();

            //当前没有计算任务
            if (currentCalculating == null) {
                if (craftingService.getCpus().stream().allMatch(ICraftingCPU::isBusy))
                    return Result.CONTINUE;
                ItemStack itemStack = craftGuideStepData.getOutput().get(nextCrafting);
                currentCalculating = craftingService.beginCraftingCalculation(maid.level(),
                        () -> new PlayerSource(WrappedMaidFakePlayer.get(maid), (IActionHost) ae2BaseContext.getPart()),
                        AEItemKey.of(itemStack),
                        itemStack.getCount(),
                        CalculationStrategy.CRAFT_LESS);
            } else if (currentCalculating.isDone()) {
                try {
                    ICraftingPlan plan = currentCalculating.get();
                    if (plan != null && plan.missingItems().isEmpty()) {
                        ICraftingSubmitResult iCraftingSubmitResult = craftingService.submitJob(plan,
                                null,
                                null,
                                true,
                                new PlayerSource(WrappedMaidFakePlayer.get(maid), (IActionHost) ae2BaseContext.getPart())
                        );
                        if (!iCraftingSubmitResult.successful()) {
                            if (iCraftingSubmitResult.errorCode() == CPU_BUSY)
                                return Result.CONTINUE;
                            return Result.FAIL;
                        }
                    }
                    currentCalculating = null;
                    nextCrafting++;
                    //多等待两分钟
                    toTick = maid.level().getServer().getTickCount() + 2400;
                } catch (Exception ignored) {
                    return Result.FAIL;
                }
                return Result.CONTINUE;
            }
            return Result.CONTINUE;
        } else {
            return Result.FAIL;
        }
    }
}
