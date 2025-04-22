package studio.fantasyit.maid_storage_manager.craft.context.special;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import studio.fantasyit.maid_storage_manager.craft.context.AbstractCraftActionContext;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftLayer;
import studio.fantasyit.maid_storage_manager.util.InvUtil;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.UUID;

public class AnvilRecipeAction extends AbstractCraftActionContext {
    FakePlayer player;

    public AnvilRecipeAction(EntityMaid maid, CraftGuideData craftGuideData, CraftGuideStepData craftGuideStepData, CraftLayer layer) {
        super(maid, craftGuideData, craftGuideStepData, layer);
    }

    @Override
    public Result start() {
        player = FakePlayerFactory.get((ServerLevel) maid.level(), new GameProfile(UUID.randomUUID(), maid.getName().getString()));
        AnvilMenu anvilMenu = new AnvilMenu(0, player.getInventory());
        ItemStack t1 = InvUtil.tryExtract(maid.getAvailableInv(false), craftGuideStepData.getInput().get(0), craftGuideStepData.matchTag);
        ItemStack t2 = InvUtil.tryExtract(maid.getAvailableInv(false), craftGuideStepData.getInput().get(1), craftGuideStepData.matchTag);
        if (
                !ItemStackUtil.isSame(t1, craftGuideStepData.getInput().get(0), craftGuideStepData.matchTag)
                        ||
                        !ItemStackUtil.isSame(t2, craftGuideStepData.getInput().get(1), craftGuideStepData.matchTag)
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

        if (result != ItemStack.EMPTY && ItemStackUtil.isSame(result, craftGuideStepData.getOutput().get(0), craftGuideStepData.matchTag)) {
            if (maid.getExperience() >= cost * 40) {
                if (
                        ItemStackUtil.isSame(t1, craftGuideStepData.getInput().get(0), craftGuideStepData.matchTag)
                                &&
                                ItemStackUtil.isSame(t2, craftGuideStepData.getInput().get(1), craftGuideStepData.matchTag)
                ) {
                    InvUtil.tryPlace(maid.getAvailableInv(false), result.copy());
                    maid.setExperience(maid.getExperience() - cost * 40);
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
    public Result tick() {
        return Result.SUCCESS;
    }

    @Override
    public void stop() {

    }
}
