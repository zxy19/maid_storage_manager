package studio.fantasyit.maid_storage_manager.craft.context.special;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import studio.fantasyit.maid_storage_manager.craft.context.AbstractCraftActionContext;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.work.CraftLayer;
import studio.fantasyit.maid_storage_manager.util.InvUtil;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;

public class AnvilRecipeAction extends AbstractCraftActionContext {
    protected static class Access implements ContainerLevelAccess {
        private final BlockPos pos;
        private final Level level;

        public Access(Level level, BlockPos pos) {
            this.level = level;
            this.pos = pos;
        }

        @Override
        public <T> Optional<T> evaluate(BiFunction<Level, BlockPos, T> p_39298_) {
            return Optional.ofNullable(p_39298_.apply(level, pos));
        }
    }

    FakePlayer player;
    Access access;
    ServerLevel level;

    public AnvilRecipeAction(EntityMaid maid, CraftGuideData craftGuideData, CraftGuideStepData craftGuideStepData, CraftLayer layer) {
        super(maid, craftGuideData, craftGuideStepData, layer);
        if (craftGuideStepData.getStorage() != null)
            access = new Access(maid.level(), craftGuideStepData.getStorage().getPos());
        else
            access = null;
        level = (ServerLevel) maid.level();
    }

    @Override
    public Result start() {
        if (access == null) return Result.FAIL;
        player = FakePlayerFactory.get((ServerLevel) maid.level(), new GameProfile(UUID.randomUUID(), maid.getName().getString()));
        return Result.CONTINUE;
    }

    private int getCostForLevel(int experience, int costLevel) {
        int level = 0;
        int tExp = experience;
        int tLevelCost = getToNext(level);
        while (tExp >= tLevelCost && level < 10_0000) {
            level++;
            tExp -= tLevelCost;
            tLevelCost = getToNext(level);
        }
        if (level < costLevel)
            return Integer.MAX_VALUE;
        int costPoint = 0;
        for (int i = 0; i < costLevel; i++) {
            costPoint += getToNext(level - i - 1);
        }
        return costPoint;
    }

    private int getToNext(int level) {
        if (level >= 30) {
            return 112 + (level - 30) * 9;
        } else {
            return level >= 15 ? 37 + (level - 15) * 5 : 7 + level * 2;
        }
    }

    @Override
    public Result tick() {
        if (!level.getBlockState(craftGuideStepData.storage.pos).is(BlockTags.ANVIL)) {
            return Result.NOT_DONE;
        }
        AnvilMenu anvilMenu = new AnvilMenu(0, player.getInventory(), access);
        ItemStack t1 = InvUtil.tryExtractForCrafting(maid.getAvailableInv(false), craftGuideStepData.getInput().get(0));
        ItemStack t2 = InvUtil.tryExtractForCrafting(maid.getAvailableInv(false), craftGuideStepData.getInput().get(1));
        if (
                !ItemStackUtil.isSameInCrafting(t1, craftGuideStepData.getInput().get(0))
                        ||
                        !ItemStackUtil.isSameInCrafting(t2, craftGuideStepData.getInput().get(1))
        ) {
            InvUtil.tryPlace(maid.getAvailableInv(false), t1);
            InvUtil.tryPlace(maid.getAvailableInv(false), t2);
            return Result.FAIL;
        }
        anvilMenu.setItem(0, 0, t1);
        anvilMenu.setItem(1, 0, t2);
        anvilMenu.setItemName(craftGuideStepData.getExtraData().getString("name"));
        anvilMenu.createResult();
        ItemStack result = anvilMenu.getSlot(2).getItem();
        int cost = anvilMenu.getCost();
        int pointCost = getCostForLevel(maid.getExperience(), cost);
        if (result != ItemStack.EMPTY && ItemStackUtil.isSameInCrafting(result, craftGuideStepData.getOutput().get(0))) {
            if (maid.getExperience() >= pointCost) {
                if (
                        ItemStackUtil.isSameInCrafting(t1, craftGuideStepData.getInput().get(0))
                                &&
                                ItemStackUtil.isSameInCrafting(t2, craftGuideStepData.getInput().get(1))
                ) {
                    anvilMenu.onTake(player, result);
                    InvUtil.tryPlace(maid.getAvailableInv(false), result.copy());
                    maid.setExperience(maid.getExperience() - pointCost);
                    craftLayer.addCurrentStepPlacedCounts(0, result.getCount());
                    return Result.SUCCESS;
                }
            }
        }
        InvUtil.tryPlace(maid.getAvailableInv(false), t1);
        InvUtil.tryPlace(maid.getAvailableInv(false), t2);
        return Result.FAIL;
    }

    @Override
    public void stop() {
    }
}
