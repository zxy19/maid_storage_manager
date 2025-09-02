package studio.fantasyit.maid_storage_manager.craft.context.common;

import com.github.tartaricacid.touhoulittlemaid.data.PowerAttachment;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.craft.action.ActionOption;
import studio.fantasyit.maid_storage_manager.craft.context.AbstractCraftActionContext;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.work.CraftLayer;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.*;

import java.util.ArrayList;
import java.util.List;


public class CommonUseAction extends AbstractCraftActionContext {
    public enum USE_TYPE {
        SINGLE,
        LONG
    }

    public static final ActionOption<USE_TYPE> OPTION_USE_METHOD = new ActionOption<>(
            ResourceLocation.fromNamespaceAndPath(MaidStorageManager.MODID, "use_mode"),
            new Component[]{
                    Component.translatable("gui.maid_storage_manager.craft_guide.common.use_single"),
                    Component.translatable("gui.maid_storage_manager.craft_guide.common.use_long")
            },
            new ResourceLocation[]{
                    ResourceLocation.fromNamespaceAndPath("maid_storage_manager", "textures/gui/craft/option/use_single.png"),
                    ResourceLocation.fromNamespaceAndPath("maid_storage_manager", "textures/gui/craft/option/use_long.png")
            },
            "",
            new ActionOption.BiConverter<>(
                    i -> USE_TYPE.values()[i], Enum::ordinal
            ),
            ActionOption.ValuePredicatorOrGetter.getter(t ->
                    switch (t) {
                        case LONG -> Component.translatable("gui.maid_storage_manager.craft_guide.common.use_long");
                        case SINGLE -> Component.translatable("gui.maid_storage_manager.craft_guide.common.use_single");
                    }
            )
    );

    public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath(MaidStorageManager.MODID, "use");
    protected WrappedMaidFakePlayer fakePlayer;
    int storedSlotMainHand = -1;
    int storedSlotOffHand = -1;
    int failCount = 0;
    float powerPointAtStart = 0;
    boolean hasStartUsing = false;

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
        if (fakePlayer.isUsingItem())
            fakePlayer.stopUsingItem();
        maid.getNavigation().stop();
        ItemStack targetItem = craftGuideStepData.getInput().get(0);
        ItemStack targetItem2 = craftGuideStepData.getInput().get(1);
        if (!targetItem.isEmpty()) {
            storedSlotMainHand = InvUtil.getTargetIndexInCrafting(maid, targetItem);
            if (storedSlotMainHand == -1) {
                return Result.FAIL;
            }
        }
        if (!targetItem2.isEmpty()) {
            storedSlotOffHand = InvUtil.getTargetIndexInCrafting(maid, targetItem2);
            if (storedSlotOffHand == -1)
                return Result.FAIL;
        }
        MemoryUtil.getCrafting(maid).setSwappingHandWhenCrafting(true);
        if (storedSlotOffHand != -1)
            InvUtil.swapHandAndSlot(maid, InteractionHand.OFF_HAND, storedSlotOffHand);
        if (storedSlotMainHand != -1)
            InvUtil.swapHandAndSlot(maid, InteractionHand.MAIN_HAND, storedSlotMainHand);
        failCount = 0;
        return Result.CONTINUE;
    }

    @Override
    public Result tick() {
        if (!MoveUtil.setMovementIfColliedTarget((ServerLevel) maid.level(), maid, craftGuideStepData.storage))
            return Result.CONTINUE;
        return switch (craftGuideStepData.getOptionSelection(OPTION_USE_METHOD).orElse(USE_TYPE.SINGLE)) {
            case SINGLE -> workForSingleUse();
            case LONG -> workForLongUse();
        };
    }

    private @NotNull Result workForSingleUse() {
        maid.swing(InteractionHand.MAIN_HAND);
        @Nullable List<ItemStack> ret = interactWithItemAndGetReturn();
        if (ret == null) {
            if (++failCount > 10) {
                if (craftGuideStepData.isOptional())
                    return Result.SUCCESS;
                else
                    return Result.FAIL;
            }
            MoveUtil.setMovementTowardsTargetSlowly(maid);
            return Result.CONTINUE_INTERRUPTABLE;
        }

        return checkAndGetResult(ret);
    }

    public @NotNull Result workForLongUse() {
        if (hasStartUsing) {
            fakePlayer.updatingUsingItem();
            if (fakePlayer.getUseItem().isEmpty()) {
                List<ItemStack> inventoryReturn = getAndClearFakePlayerInventory();
                return checkAndGetResult(inventoryReturn);
            }
            if (fakePlayer.getUseItemRemainingTicks() < 0) {
                fakePlayer.releaseUsingItem();
            }
            return Result.CONTINUE;
        }

        maid.swing(InteractionHand.MAIN_HAND);
        Target storage = craftGuideStepData.getStorage();
        BlockPos target = craftGuideStepData.getStorage().getPos();
        ServerLevel level = (ServerLevel) maid.level();
        BlockHitResult blockHitResult = getBlockHitResult(target, level, storage);
        if (blockHitResult != null) {
            useItemSingle(target, blockHitResult, level);
            if (!fakePlayer.getUseItem().isEmpty())
                hasStartUsing = true;
        }
        if (blockHitResult == null || !hasStartUsing) {
            if (++failCount > 10) {
                if (craftGuideStepData.isOptional()) {
                    return Result.SUCCESS;
                } else {
                    return Result.FAIL;
                }
            }
            MoveUtil.setMovementTowardsTargetSlowly(maid);
            return Result.CONTINUE_INTERRUPTABLE;
        }
        return Result.CONTINUE;
    }

    private @NotNull Result checkAndGetResult(@NotNull List<ItemStack> ret) {
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
        //如果副手包含目标物品，也视为返回
        if (ItemStackUtil.isSameInCrafting(craftGuideStepData.getOutput().get(0), fakePlayer.getOffhandItem())) {
            resultPlaced += fakePlayer.getOffhandItem().getCount();
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
        BlockHitResult result = getBlockHitResult(target, level, storage);
        if (result == null) return null;
        useItemSingle(target, result, level);
        fakePlayer.overrideXYRot(null, null);
        return getAndClearFakePlayerInventory();
    }

    private @NotNull List<ItemStack> getAndClearFakePlayerInventory() {
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

    private void useItemSingle(BlockPos target, BlockHitResult result, ServerLevel level) {
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
    }

    private @Nullable BlockHitResult getBlockHitResult(BlockPos target, ServerLevel level, Target storage) {
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
        return result;
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
        if (storedSlotMainHand != -1)
            InvUtil.swapHandAndSlot(maid, InteractionHand.MAIN_HAND, storedSlotMainHand);
        if (storedSlotOffHand != -1)
            InvUtil.swapHandAndSlot(maid, InteractionHand.OFF_HAND, storedSlotOffHand);

        if (fakePlayer.getData(PowerAttachment.TYPE).get() != powerPointAtStart) {
            float deltaPP = fakePlayer.getData(PowerAttachment.TYPE).get() - powerPointAtStart;
            maid.setExperience(maid.getExperience() - (int) Math.ceil(deltaPP / 4));
        }
        if (fakePlayer.isUsingItem())
            fakePlayer.stopUsingItem();
        MemoryUtil.getCrafting(maid).setSwappingHandWhenCrafting(false);
    }

    @Override
    public boolean skipNextBreath() {
        return hasStartUsing;
    }
}
