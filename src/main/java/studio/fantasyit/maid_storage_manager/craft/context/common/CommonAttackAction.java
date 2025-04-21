package studio.fantasyit.maid_storage_manager.craft.context.common;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.craft.context.AbstractCraftActionContext;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftLayer;
import studio.fantasyit.maid_storage_manager.util.InvUtil;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static net.minecraftforge.eventbus.api.Event.Result.DENY;

public class CommonAttackAction extends AbstractCraftActionContext {
    public static final ResourceLocation TYPE_L = new ResourceLocation(MaidStorageManager.MODID, "destroy");
    FakePlayer fakePlayer;
    boolean startDestroyBlock = false;
    float progress = 0.0f;
    int storedSlot = -1;

    public CommonAttackAction(EntityMaid maid, CraftGuideData craftGuideData, CraftGuideStepData craftGuideStepData, CraftLayer layer) {
        super(maid, craftGuideData, craftGuideStepData, layer);
    }

    @Override
    public Result start() {
        fakePlayer = FakePlayerFactory.get((ServerLevel) maid.level(), new GameProfile(UUID.randomUUID(), maid.getName().getString()));
        maid.getNavigation().stop();
        return Result.CONTINUE;
    }

    @Override
    public Result tick() {
        if (startDestroyBlock) {
            return tickDestroyBlock();
        }
        if (maid.getDeltaMovement().length() > 0.1) return Result.CONTINUE;
        maid.swing(InteractionHand.MAIN_HAND);
        @Nullable List<ItemStack> ret = interactWithItemAndGetReturn(craftGuideStepData.getInput().get(0));

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
            if (ItemStackUtil.isSame(itemStack, craftGuideStepData.getOutput().get(0), craftGuideStepData.matchTag)) {
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

    private Result tickDestroyBlock() {
        maid.swing(InteractionHand.MAIN_HAND);
        ItemStack tool = ItemStack.EMPTY;
        if (!craftGuideStepData.getNonEmptyInput().isEmpty())
            tool = craftGuideStepData.getNonEmptyInput().get(0);

        BlockPos target = craftGuideStepData.getStorage().pos;
        BlockState targetBs = maid.level().getBlockState(target);
        fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, tool);
        float speed = fakePlayer.getDigSpeed(targetBs, craftGuideStepData.getStorage().pos) / tool.getDestroySpeed(targetBs);
        if (fakePlayer.hasCorrectToolForDrops(targetBs)) {
            progress += speed;
        }
        if (progress >= 1.0f) {
            List<ItemStack> items = new ArrayList<>();
            List<ItemStack> original = new ArrayList<>();
            CombinedInvWrapper availableInv = maid.getAvailableInv(false);
            for (int i = 0; i < availableInv.getSlots(); i++) {
                original.add(availableInv.getStackInSlot(i).copy());
            }
            maid.destroyBlock(target, true);
            for (int i = 0; i < availableInv.getSlots(); i++) {
                if (!availableInv.getStackInSlot(i).isEmpty()) {
                    ItemStackUtil.addToList(items, availableInv.getStackInSlot(i).copy(), true);
                }
            }
            for (ItemStack itemStack : original) {
                ItemStackUtil.removeIsMatchInList(items, itemStack, true);
            }
            int totalGet = 0;
            for (ItemStack itemStack : items) {
                if (ItemStackUtil.isSame(itemStack, craftGuideStepData.getOutput().get(0), craftGuideStepData.matchTag)) {
                    totalGet += itemStack.getCount();
                }
            }
            ItemStack tmpSwap = maid.getItemInHand(InteractionHand.MAIN_HAND);
            tmpSwap.hurt(1, maid.level().random, fakePlayer);
            maid.setItemInHand(InteractionHand.MAIN_HAND, maid.getAvailableInv(true).getStackInSlot(storedSlot));
            maid.getAvailableInv(true).setStackInSlot(storedSlot, tmpSwap);

            MemoryUtil.getCrafting(maid).setSwappingHandWhenCrafting(false);
            if (totalGet >= craftGuideStepData.getOutput().get(0).getCount()) {
                return Result.SUCCESS;
            } else {
                return Result.FAIL;
            }
        }

        return Result.CONTINUE;
    }

    private @Nullable List<ItemStack> interactWithItemAndGetReturn(ItemStack itemStack) {
        BlockPos target = craftGuideStepData.getStorage().getPos();
        ServerLevel level = (ServerLevel) maid.level();

        fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
        ClipContext rayTraceContext =
                new ClipContext(maid.getPosition(0).add(0, maid.getEyeHeight(), 0),
                        target.getCenter(),
                        ClipContext.Block.OUTLINE,
                        ClipContext.Fluid.NONE,
                        fakePlayer);
        BlockHitResult result = level.clip(rayTraceContext);
        if (!result.getBlockPos().equals(target)) return null;
        PlayerInteractEvent.LeftClickBlock event = ForgeHooks.onLeftClickBlock(fakePlayer,
                target,
                craftGuideStepData.getStorage().side,
                ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK
        );

        if (event.getUseBlock() != DENY) {
            onStartDestroyBlock(level, target);
        }
        Inventory inventory = fakePlayer.getInventory();
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (!inventory.getItem(i).isEmpty()) {
                ItemStackUtil.addToList(items, inventory.getItem(i), true);
            }
        }
        inventory.clearContent();
        if (!fakePlayer.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
            ItemStackUtil.addToList(items, fakePlayer.getItemInHand(InteractionHand.MAIN_HAND), true);
        }
        return items;
    }

    private void onStartDestroyBlock(ServerLevel level, BlockPos target) {
        CombinedInvWrapper inv = maid.getAvailableInv(true);
        for (int i = 0; i < inv.getSlots(); i++) {
            if (inv.getStackInSlot(i).isEmpty()) {
                storedSlot = i;
                break;
            }
        }
        if (storedSlot == -1) return;

        //假人手上的应当就是需要使用的物品吧？交换到女仆主手
        inv.setStackInSlot(storedSlot, maid.getMainHandItem());
        maid.setItemInHand(InteractionHand.MAIN_HAND, fakePlayer.getItemInHand(InteractionHand.MAIN_HAND));
        fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        MemoryUtil.getCrafting(maid).setSwappingHandWhenCrafting(true);

        level.getBlockState(target).attack(level, target, fakePlayer);
        this.startDestroyBlock = true;
        this.progress = 0.0f;
    }

    @Override
    public void stop() {
        fakePlayer.remove(Entity.RemovalReason.DISCARDED);
    }
}
