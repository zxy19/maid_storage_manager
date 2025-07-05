package studio.fantasyit.maid_storage_manager.craft.context.special;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.craft.WorkBlockTags;
import studio.fantasyit.maid_storage_manager.craft.context.AbstractCraftActionContext;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.work.CraftLayer;
import studio.fantasyit.maid_storage_manager.util.InvUtil;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;
import studio.fantasyit.maid_storage_manager.util.RecipeUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class CraftingRecipeAction extends AbstractCraftActionContext {
    public static final ResourceLocation TYPE = new ResourceLocation(MaidStorageManager.MODID, "craft");

    public CraftingRecipeAction(EntityMaid maid, CraftGuideData craftGuideData, CraftGuideStepData craftGuideStepData, CraftLayer layer) {
        super(maid, craftGuideData, craftGuideStepData, layer);
    }

    @Override
    public Result start() {
        if(craftGuideStepData.getStorage() == null)
            return Result.FAIL;
        return Result.CONTINUE;
    }

    @Override
    public Result tick() {
        Level level = maid.level();
        if (!level.getBlockState(craftGuideStepData.storage.pos).is(WorkBlockTags.CRAFTING_TABLE))
            return Result.NOT_DONE;
        CombinedInvWrapper inv = maid.getAvailableInv(false);
        List<ItemStack> input = craftGuideStepData.getInput();
        List<ItemStack> output = craftGuideStepData.getOutput();
        List<ItemStack> realInput = new ArrayList<>();
        int[] slotExtractCount = new int[inv.getSlots()];
        Arrays.fill(slotExtractCount, 0);
        boolean allMatch = true;
        for (int i = 0; i < input.size(); i++) {
            boolean found = false;
            if (input.get(i).isEmpty()) {
                realInput.add(ItemStack.EMPTY);
                continue;
            }
            for (int j = 0; j < inv.getSlots(); j++) {
                if (ItemStack.isSameItem(inv.getStackInSlot(j), input.get(i))) {
                    //还有剩余（
                    if (inv.getStackInSlot(j).getCount() > slotExtractCount[j]) {
                        realInput.add(inv.getStackInSlot(j).copyWithCount(input.get(i).getCount()));
                        found = true;
                        slotExtractCount[j] += 1;
                        break;
                    }
                }
            }
            if (!found) {
                allMatch = false;
                break;
            }
        }
        if (allMatch) {
            CraftingContainer container = RecipeUtil.wrapCraftingContainer(realInput, 3, 3);
            Optional<CraftingRecipe> recipe = RecipeUtil.getCraftingRecipe(level, container);
            if (recipe.isPresent()) {
                ItemStack result = recipe.get().assemble(container, level.registryAccess());
                if (ItemStackUtil.isSameInCrafting(result, output.get(0))) {
                    craftLayer.addCurrentStepPlacedCounts(0, result.getCount());
                }

                int maxCanPlace = InvUtil.maxCanPlace(inv, result);
                if (maxCanPlace >= result.getCount()) {
                    InvUtil.tryPlace(inv, result);
                    for (int j = 0; j < inv.getSlots(); j++) {
                        inv.extractItem(j, slotExtractCount[j], false);
                    }

                    NonNullList<ItemStack> remain = recipe.get().getRemainingItems(container);
                    for (int j = 0; j < remain.size(); j++) {
                        if (!remain.get(j).isEmpty()) {
                            int total = remain.get(j).getCount();
                            for (int k = 0; k < output.size(); k++) {
                                int rem = output.get(k).getCount() - craftLayer.getCurrentStepCount(k);
                                if (ItemStackUtil.isSameInCrafting(remain.get(j), output.get(k)) && rem > 0) {
                                    craftLayer.addCurrentStepPlacedCounts(k, Math.min(total, rem));
                                }
                                total -= rem;
                                if (total <= 0) break;
                            }
                            ItemStack itemStack = InvUtil.tryPlace(inv, remain.get(j));
                            if (!itemStack.isEmpty()) {
                                InvUtil.throwItem(maid, itemStack);
                                return Result.FAIL;
                            }
                        }
                    }

                    return Result.SUCCESS;
                } else {
                    return Result.FAIL;
                }
            }
        } else {
            return Result.FAIL;
        }
        return Result.FAIL;
    }

    @Override
    public void stop() {

    }
}
