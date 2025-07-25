package studio.fantasyit.maid_storage_manager.craft.context.common;

import com.github.tartaricacid.touhoulittlemaid.data.PowerAttachment;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
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


public class CommonUseAction extends AbstractCraftActionContext {
    public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath(MaidStorageManager.MODID, "use");
    protected WrappedMaidFakePlayer fakePlayer;
    private int storedSlot;
    int failCount = 0;
    float powerPointAtStart = 0;

    public CommonUseAction(EntityMaid maid, CraftGuideData craftGuideData, CraftGuideStepData craftGuideStepData, CraftLayer layer) {
        super(maid, craftGuideData, craftGuideStepData, layer);
    }

    @Override
    public void loadEnv(CompoundTag env) {
        failCount = env.contains("failCount") ? env.getInt("failCount") : 0;
    }

    @Override
    public CompoundTag saveEnv(CompoundTag env) {
        env.putInt("failCount", failCount);
        return super.saveEnv(env);
    }

    @Override
    public Result start() {
        fakePlayer = WrappedMaidFakePlayer.get(maid);
        fakePlayer.getData(PowerAttachment.TYPE).set(maid.getExperience() * 4);
        powerPointAtStart = fakePlayer.getData(PowerAttachment.TYPE).get();
        maid.getNavigation().stop();
        MemoryUtil.getCrafting(maid).setSwappingHandWhenCrafting(true);
        storedSlot = InvUtil.getTargetIndexInCrafting(maid, craftGuideStepData.getInput().get(0));
        if (storedSlot == -1) return Result.FAIL;
        InvUtil.swapHandAndSlot(maid, storedSlot);
        failCount = 0;
        return Result.CONTINUE;
    }

    @Override
    public Result tick() {
        if (!MoveUtil.setMovementIfColliedTarget((ServerLevel) maid.level(), maid, craftGuideStepData.storage))
            return Result.CONTINUE;
        maid.swing(InteractionHand.MAIN_HAND);
        MoveUtil.setMovementTowardsTargetSlowly(maid);
        @Nullable List<ItemStack> ret = interactWithItemAndGetReturn();
        if (ret == null) {
            if (++failCount > 10) {
                if (craftGuideStepData.isOptional())
                    return Result.SUCCESS;
                else
                    return Result.FAIL;
            }
            return Result.CONTINUE_INTERRUPTABLE;
        }

        int resultPlaced = 0;
        //物品栏新增的物品
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
        //如果主手包含目标物品，也视为返回
        if (ItemStackUtil.isSameInCrafting(craftGuideStepData.getOutput().get(0), fakePlayer.getMainHandItem())) {
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
        Vec3 eyePos = maid.getPosition(0).add(0, maid.getEyeHeight(), 0);
        Vec3 viewVec = null;

        BlockHitResult result = null;
        for (float disToSize = 0.50f; disToSize > 0; disToSize -= 0.1f) {
            for (Direction direction : Direction.values()) {
                if (craftGuideStepData.getStorage().side != null && craftGuideStepData.getStorage().side != direction)
                    continue;
                ClipContext rayTraceContext = new ClipContext(eyePos,
                        target.getCenter().relative(direction, disToSize),
                        ClipContext.Block.COLLIDER,
                        shouldUseFluidClip(level, target) ? ClipContext.Fluid.SOURCE_ONLY : ClipContext.Fluid.NONE,
                        fakePlayer);
                viewVec = target.getCenter().relative(direction, disToSize).subtract(eyePos);
                result = level.clip(rayTraceContext);
                if (result.getBlockPos().equals(target))
                    if (storage.side == null || result.getDirection() == storage.side)
                        break;
                result = null;
            }
            if (result != null) break;
        }
        if (result == null) return null;


        fakePlayer.overrideXYRot(MathUtil.vec2RotX(viewVec), MathUtil.vec2RotY(viewVec));
        PlayerInteractEvent.RightClickBlock event = CommonHooks.onRightClickBlock(fakePlayer,
                InteractionHand.MAIN_HAND,
                target,
                result
        );
        BlockState targetState = level.getBlockState(target);
        if (event.getUseBlock() != TriState.FALSE) {
            boolean consume;
            if (fakePlayer.getMainHandItem().isEmpty())
                consume = targetState.useWithoutItem(level, fakePlayer, result).consumesAction();
            else
                consume = targetState.useItemOn(fakePlayer.getMainHandItem(), level, fakePlayer, InteractionHand.MAIN_HAND, result).consumesAction();
            if (!consume) {
                UseOnContext useContext = new UseOnContext(fakePlayer, InteractionHand.MAIN_HAND, result);
                InteractionResult actionresult = fakePlayer.getItemInHand(InteractionHand.MAIN_HAND).onItemUseFirst(useContext);
                if (actionresult == InteractionResult.PASS) {
                    InteractionResult interactionResult = fakePlayer.getItemInHand(InteractionHand.MAIN_HAND).useOn(useContext);
                    if (!interactionResult.consumesAction()) {
                        InteractionResultHolder<ItemStack> use1 = fakePlayer.getItemInHand(InteractionHand.MAIN_HAND).use(level, fakePlayer, InteractionHand.MAIN_HAND);
                        fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, use1.getObject());
                    }
                }
            }
        }
        fakePlayer.overrideXYRot(null, null);
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

    private boolean shouldUseFluidClip(ServerLevel level, BlockPos target) {
        if (level.getFluidState(target).isSource()) return true;
        if (craftGuideStepData.getInput().stream().anyMatch(t -> t.getCapability(Capabilities.FluidHandler.ITEM) != null)) {
            return true;
        }
        return false;
    }

    @Override
    public void stop() {
        if (storedSlot != -1) {
            InvUtil.swapHandAndSlot(maid, storedSlot);
        }


        if (fakePlayer.getData(PowerAttachment.TYPE).get() != powerPointAtStart) {
            float deltaPP = fakePlayer.getData(PowerAttachment.TYPE).get() - powerPointAtStart;
            maid.setExperience(maid.getExperience() - (int) Math.ceil(deltaPP / 4));
        }
        MemoryUtil.getCrafting(maid).setSwappingHandWhenCrafting(false);
    }
}
