package studio.fantasyit.maid_storage_manager.craft.action;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftLayer;
import studio.fantasyit.maid_storage_manager.util.InvUtil;
import studio.fantasyit.maid_storage_manager.util.RecipeUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class CraftingRecipeAction extends AbstractCraftActionContext {
    public static final ResourceLocation TYPE = new ResourceLocation(MaidStorageManager.MODID,"craft");
    public CraftingRecipeAction(EntityMaid maid, CraftGuideData craftGuideData, CraftGuideStepData craftGuideStepData,  CraftLayer layer) {
        super(maid, craftGuideData, craftGuideStepData, layer);
    }

    @Override
    public Result start() {
        if (craftLayer.isOutput())
            return Result.CONTINUE;
        return Result.SUCCESS;
    }

    @Override
    public Result tick() {
        Level level = maid.level();
        CombinedInvWrapper inv = maid.getAvailableInv(false);
        List<ItemStack> needs = craftGuideData.getAllInputItems();
        int[] slotExtractCount = new int[inv.getSlots()];
        Arrays.fill(slotExtractCount, 0);
        boolean allMatch = true;
        for (int i = 0; i < needs.size(); i++) {
            boolean found = false;
            if (needs.get(i).isEmpty()) continue;
            for (int j = 0; j < inv.getSlots(); j++) {
                if (ItemStack.isSameItem(inv.getStackInSlot(j), needs.get(i))) {
                    //还有剩余（
                    if (inv.getStackInSlot(j).getCount() > slotExtractCount[j]) {
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
            CraftingContainer container = RecipeUtil.wrapContainer(
                    craftGuideData.getAllInputItems()
                    , 3, 3);
            Optional<CraftingRecipe> recipe = RecipeUtil.getRecipe(level, container);
            if (recipe.isPresent()) {
                ItemStack result = recipe.get().assemble(container, level.registryAccess());
                if (ItemStack.isSameItem(result, craftGuideStepData.getItems().get(0))) {
                    craftLayer.addCurrentStepPlacedCounts(0, 1);
                }
                int maxCanPlace = InvUtil.maxCanPlace(inv, result);
                if (maxCanPlace >= result.getCount()) {
                    InvUtil.tryPlace(inv, result);
                    for (int j = 0; j < inv.getSlots(); j++) {
                        inv.extractItem(j, slotExtractCount[j], false);
                    }
                    //剩余物品暂时没有计算，但是也不能吞了，先尝试放背包或者扔地上吧
                    NonNullList<ItemStack> remain = recipe.get().getRemainingItems(container);
                    for (int j = 0; j < remain.size(); j++) {
                        if (!remain.get(j).isEmpty()) {
                            ItemStack itemStack = InvUtil.tryPlace(inv, remain.get(j));
                            if (!itemStack.isEmpty()) {
                                InvUtil.throwItem(maid, itemStack);
                                return Result.FAIL;
                            }
                        }
                    }
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
