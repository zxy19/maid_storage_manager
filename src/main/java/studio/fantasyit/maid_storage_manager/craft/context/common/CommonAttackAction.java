package studio.fantasyit.maid_storage_manager.craft.context.common;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.craft.context.AbstractCraftActionContext;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.work.CraftLayer;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.*;

import java.util.ArrayList;
import java.util.List;


public class CommonAttackAction extends AbstractCraftActionContext {
    public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath(MaidStorageManager.MODID, "destroy");
    FakePlayer fakePlayer;
    boolean startDestroyBlock = false;
    float progress = 0.0f;
    int tickLast = 0;
    int storedSlot = -1;

    public CommonAttackAction(EntityMaid maid, CraftGuideData craftGuideData, CraftGuideStepData craftGuideStepData, CraftLayer layer) {
        super(maid, craftGuideData, craftGuideStepData, layer);
    }

    @Override
    public Result start() {
        fakePlayer = WrappedMaidFakePlayer.get(maid);
        maid.getNavigation().stop();

        ItemStack targetItem = craftGuideStepData.getInput().get(0);
        MemoryUtil.getCrafting(maid).setSwappingHandWhenCrafting(true);
        storedSlot = InvUtil.getTargetIndexInCrafting(maid, targetItem);
        if (storedSlot == -1) return Result.FAIL;
        InvUtil.swapHandAndSlot(maid, storedSlot);

        return Result.CONTINUE;
    }

    @Override
    public Result tick() {
        if (startDestroyBlock) {
            return tickDestroyBlock();
        }
        //破坏任务中不用关心面，只需离开目标所在pos
        if (!MoveUtil.setMovementIfColliedTarget((ServerLevel) maid.level(), maid, craftGuideStepData.storage.pos))
            return Result.CONTINUE;
        if (!hasBlockOnTarget())
            return Result.NOT_DONE_INTERRUPTABLE;
        maid.swing(InteractionHand.MAIN_HAND);
        @Nullable List<ItemStack> ret = interactWithItemAndGetReturn();

        if (ret == null) {
            if (startDestroyBlock) return Result.CONTINUE;
            return Result.FAIL;
        }

        int resultPlaced = 0;
        for (ItemStack itemStack : ret) {
            ItemStack itemStack1 = InvUtil.tryPlace(maid.getAvailableInv(false), itemStack);
            int realPlaced = itemStack.getCount() - itemStack1.getCount();
            if (!itemStack1.isEmpty()) {
                InvUtil.throwItem(maid, itemStack1);
            }
            if (ItemStackUtil.isSameInCrafting(itemStack, craftGuideStepData.getOutput().get(0))) {
                resultPlaced += realPlaced;
            }
        }
        if (startDestroyBlock) return Result.CONTINUE;

        if (resultPlaced >= craftGuideStepData.getOutput().get(0).getCount()) {
            return Result.SUCCESS;
        } else {
            return Result.FAIL;
        }
    }

    private boolean hasBlockOnTarget() {
        return !maid.level().getBlockState(craftGuideStepData.storage.pos).isAir();
    }

    private Result tickDestroyBlock() {
        maid.swing(InteractionHand.MAIN_HAND);
        BlockPos target = craftGuideStepData.getStorage().pos;
        BlockState targetBs = maid.level().getBlockState(target);
        if (fakePlayer.hasCorrectToolForDrops(targetBs)) {
            int tickBetween = maid.tickCount - this.tickLast;
            progress += targetBs.getDestroyProgress(fakePlayer, maid.level(), target) * tickBetween;
            this.tickLast = maid.tickCount;
        } else {
            return Result.FAIL;
        }
        if (progress >= 1.0f) {
            ServerLevel level = (ServerLevel) maid.level();

            FluidState fluidState = level.getFluidState(target);
            if (!(targetBs.getBlock() instanceof BaseFireBlock)) {
                level.levelEvent(2001, target, Block.getId(targetBs));
            }
            MutableInt totalGet = new MutableInt(0);
            BlockEntity blockEntity = targetBs.hasBlockEntity() ? level.getBlockEntity(target) : null;
            //改用MainHandItem来roll loot
            CombinedInvWrapper availableInv = maid.getAvailableInv(false);
            Block.getDrops(targetBs, level, target, blockEntity, maid, maid.getMainHandItem()).forEach((stack) -> {
                ItemStack originalStack = stack.copy();
                ItemStack remindItemStack = ItemHandlerHelper.insertItemStacked(availableInv, stack, false);
                if (ItemStackUtil.isSameInCrafting(originalStack, craftGuideStepData.getOutput().get(0))) {
                    totalGet.add(originalStack.getCount() - remindItemStack.getCount());
                }
                if (!remindItemStack.isEmpty()) {
                    Block.popResource(level, target, remindItemStack);
                }
            });
            targetBs.spawnAfterBreak(level, target, maid.getMainHandItem(), true);
            maid.getMainHandItem().hurtAndBreak(1, fakePlayer, EquipmentSlot.MAINHAND);
            boolean setResult = level.setBlock(target, fluidState.createLegacyBlock(), 3);
            if (setResult) {
                level.gameEvent(GameEvent.BLOCK_DESTROY, target, GameEvent.Context.of(maid, targetBs));
            }
            if (totalGet.getValue() >= craftGuideStepData.getOutput().get(0).getCount()) {
                return Result.SUCCESS;
            } else {
                return Result.FAIL;
            }
        }

        return Result.CONTINUE;
    }

    private @Nullable List<ItemStack> interactWithItemAndGetReturn() {
        BlockPos target = craftGuideStepData.getStorage().getPos();
        ServerLevel level = (ServerLevel) maid.level();
        BlockHitResult result = getBlockHitResult(level, craftGuideStepData.getStorage());
        if (result == null || !result.getBlockPos().equals(target)) return null;
        PlayerInteractEvent.LeftClickBlock event = CommonHooks.onLeftClickBlock(fakePlayer,
                target,
                craftGuideStepData.getStorage().side,
                ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK
        );

        if (event.getUseBlock() != TriState.FALSE) {
            onStartDestroyBlock(level, target);
        }
        Inventory inventory = fakePlayer.getInventory();
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (!inventory.getItem(i).isEmpty()) {
                ItemStackUtil.addToList(items, inventory.getItem(i), true);
                inventory.setItem(i, ItemStack.EMPTY);
            }
        }
        return items;
    }

    private BlockHitResult getBlockHitResult(ServerLevel level, Target target) {
        BlockHitResult result = null;
        for (float disToSize = 0.50f; disToSize > 0; disToSize -= 0.1f) {
            for (Direction direction : Direction.values()) {
                if (craftGuideStepData.getStorage().side != null && craftGuideStepData.getStorage().side != direction)
                    continue;
                ClipContext rayTraceContext = new ClipContext(maid.getPosition(0).add(0, maid.getEyeHeight(), 0),
                        target.pos.getCenter().relative(direction, disToSize),
                        ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE,
                        fakePlayer);
                result = level.clip(rayTraceContext);
                if (result.getBlockPos().equals(target.pos))
                    if (target.side == null || result.getDirection() == target.side)
                        break;
                result = null;
            }
            if (result != null) break;
        }
        if (result == null) return null;
        return result;
    }

    private void onStartDestroyBlock(ServerLevel level, BlockPos target) {
        level.getBlockState(target).attack(level, target, fakePlayer);
        this.startDestroyBlock = true;
        this.progress = 0.0f;
        this.tickLast = maid.tickCount;
    }

    @Override
    public void stop() {
        if (storedSlot != -1) {
            InvUtil.swapHandAndSlot(maid, storedSlot);
        }
        MemoryUtil.getCrafting(maid).setSwappingHandWhenCrafting(false);
    }
}
