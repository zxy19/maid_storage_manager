package studio.fantasyit.maid_storage_manager.craft.context.common;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.craft.context.AbstractCraftActionContext;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftLayer;
import studio.fantasyit.maid_storage_manager.util.InvUtil;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static net.minecraftforge.eventbus.api.Event.Result.DENY;

public class CommonUseAction extends AbstractCraftActionContext {
    public static final ResourceLocation TYPE_R = new ResourceLocation(MaidStorageManager.MODID, "use");
    protected FakePlayer fakePlayer;

    public CommonUseAction(EntityMaid maid, CraftGuideData craftGuideData, CraftGuideStepData craftGuideStepData, CraftLayer layer) {
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
        if (maid.getDeltaMovement().length() > 0.1) return Result.CONTINUE;

        maid.swing(InteractionHand.MAIN_HAND);
        @Nullable List<ItemStack> ret = interactWithItemAndGetReturn(craftGuideStepData.getInput().get(0));
        if (ret == null) {
            if (craftGuideStepData.isOptional())
                return Result.SUCCESS;
            else
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
        if (resultPlaced >= craftGuideStepData.getOutput().get(0).getCount()) {
            return Result.SUCCESS;
        } else {
            if (craftGuideStepData.isOptional())
                return Result.SUCCESS;
            else
                return Result.FAIL;
        }
    }

    private @Nullable List<ItemStack> interactWithItemAndGetReturn(ItemStack itemStack) {
        BlockPos target = craftGuideStepData.getStorage().getPos();
        ServerLevel level = (ServerLevel) maid.level();

        ItemStack itemStack1 = InvUtil.tryExtract(maid.getAvailableInv(false), itemStack, craftGuideStepData.matchTag);
        fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, itemStack1);

        BlockHitResult result = null;
        for (Direction direction : Direction.values()) {
            if (craftGuideStepData.getStorage().side != null && craftGuideStepData.getStorage().side != direction)
                continue;
            ClipContext rayTraceContext = new ClipContext(maid.getPosition(0).add(0, maid.getEyeHeight(), 0),
                    target.getCenter().relative(direction, 0.3),
                    ClipContext.Block.OUTLINE,
                    ClipContext.Fluid.NONE,
                    fakePlayer);
            result = level.clip(rayTraceContext);
            if (result.getBlockPos().equals(target)) break;
            result = null;
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
        inventory.clearContent();
        if (!fakePlayer.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
            ItemStackUtil.addToList(items, fakePlayer.getItemInHand(InteractionHand.MAIN_HAND), true);
        }
        return items;
    }

    @Override
    public void stop() {
        fakePlayer.remove(Entity.RemovalReason.DISCARDED);
    }
}
