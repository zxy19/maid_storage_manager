package studio.fantasyit.maid_storage_manager.craft.context.common;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.craft.context.AbstractCraftActionContext;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftLayer;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.InvUtil;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;
import studio.fantasyit.maid_storage_manager.util.WrappedMaidFakePlayer;

import java.util.ArrayList;
import java.util.List;

import static net.minecraftforge.eventbus.api.Event.Result.DENY;

public class CommonUseAction extends AbstractCraftActionContext {
    public static final ResourceLocation TYPE_R = new ResourceLocation(MaidStorageManager.MODID, "use");
    protected FakePlayer fakePlayer;
    private int storedSlot;

    public CommonUseAction(EntityMaid maid, CraftGuideData craftGuideData, CraftGuideStepData craftGuideStepData, CraftLayer layer) {
        super(maid, craftGuideData, craftGuideStepData, layer);
    }

    @Override
    public Result start() {
        fakePlayer = WrappedMaidFakePlayer.get(maid);
        maid.getNavigation().stop();
        MemoryUtil.getCrafting(maid).setSwappingHandWhenCrafting(true);
        storedSlot = InvUtil.getTargetIndex(maid, craftGuideStepData.getInput().get(0), craftGuideStepData.matchTag);
        if (storedSlot == -1) return Result.FAIL;
        InvUtil.swapHandAndSlot(maid, storedSlot);
        return Result.CONTINUE;
    }

    @Override
    public Result tick() {
        if (maid.getDeltaMovement().length() > 0.1) return Result.CONTINUE;
        maid.swing(InteractionHand.MAIN_HAND);
        @Nullable List<ItemStack> ret = interactWithItemAndGetReturn();
        if (ret == null) {
            if (craftGuideStepData.isOptional())
                return Result.SUCCESS;
            else
                return Result.FAIL;
        }

        int resultPlaced = 0;
        //物品栏新增的物品
        for (ItemStack itemStack : ret) {
            ItemStack itemStack1 = InvUtil.tryPlace(maid.getAvailableInv(false), itemStack);
            int realPlaced = itemStack.getCount() - itemStack1.getCount();
            if (!itemStack1.isEmpty()) {
                InvUtil.throwItem(maid, itemStack1);
            }
            if (ItemStackUtil.isSame(itemStack, craftGuideStepData.getOutput().get(0), craftGuideStepData.matchTag)) {
                resultPlaced += realPlaced;
            }
        }
        //如果主手包含目标物品，也视为返回
        if (ItemStackUtil.isSame(craftGuideStepData.getOutput().get(0), fakePlayer.getMainHandItem(), craftGuideStepData.matchTag)) {
            resultPlaced += fakePlayer.getMainHandItem().getCount();
        }

        if (resultPlaced >= craftGuideStepData.getOutput().get(0).getCount()) {
            return Result.SUCCESS;
        } else {
            if (craftGuideStepData.isOptional())
                return Result.SUCCESS;
            else
                return Result.FAIL;
        }
    }

    private @Nullable List<ItemStack> interactWithItemAndGetReturn() {
        Target storage = craftGuideStepData.getStorage();
        BlockPos target = craftGuideStepData.getStorage().getPos();
        ServerLevel level = (ServerLevel) maid.level();

        BlockHitResult result = null;
        for (float disToSize = 0.50f; disToSize > 0; disToSize -= 0.1f) {
            for (Direction direction : Direction.values()) {
                if (craftGuideStepData.getStorage().side != null && craftGuideStepData.getStorage().side != direction)
                    continue;
                ClipContext rayTraceContext = new ClipContext(maid.getPosition(0).add(0, maid.getEyeHeight(), 0),
                        target.getCenter().relative(direction, disToSize),
                        ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE,
                        fakePlayer);
                result = level.clip(rayTraceContext);
                if (result.getBlockPos().equals(target))
                    if (storage.side == null || result.getDirection() == storage.side)
                        break;
                result = null;
            }
            if (result != null) break;
        }
        if (result == null) return null;

        PlayerInteractEvent.RightClickBlock event = ForgeHooks.onRightClickBlock(fakePlayer,
                InteractionHand.MAIN_HAND,
                target,
                result
        );
        BlockState targetState = level.getBlockState(target);
        if (event.getUseBlock() != DENY) {
            InteractionResult use = targetState
                    .use(level, fakePlayer, InteractionHand.MAIN_HAND, result);
            if (!use.consumesAction()) {
                UseOnContext useContext = new UseOnContext(fakePlayer, InteractionHand.MAIN_HAND, result);
                InteractionResult actionresult = fakePlayer.getItemInHand(InteractionHand.MAIN_HAND).onItemUseFirst(useContext);
                if (actionresult == InteractionResult.PASS) {
                    InteractionResult interactionResult = fakePlayer.getItemInHand(InteractionHand.MAIN_HAND).useOn(useContext);
                }
            }
        }
        Inventory inventory = fakePlayer.getInventory();
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (!inventory.getItem(i).isEmpty()) {
                ItemStackUtil.addToList(items, inventory.getItem(i), true);
            }
        }
        return items;
    }

    @Override
    public void stop() {
        if (storedSlot != -1) {
            InvUtil.swapHandAndSlot(maid, storedSlot);
        }
        MemoryUtil.getCrafting(maid).setSwappingHandWhenCrafting(false);
    }
}
