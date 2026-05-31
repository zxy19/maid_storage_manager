package studio.fantasyit.maid_storage_manager.craft.context.special;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.craft.WorkBlockTags;
import studio.fantasyit.maid_storage_manager.craft.context.AbstractCraftActionContext;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.work.CraftLayer;
import studio.fantasyit.maid_storage_manager.util.InvUtil;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;
import studio.fantasyit.maid_storage_manager.util.RecipeUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class SmithingRecipeAction extends AbstractCraftActionContext {
    public static final Identifier TYPE = Identifier.fromNamespaceAndPath(MaidStorageManager.MODID, "smithing");

    public SmithingRecipeAction(EntityMaid maid, CraftGuideData craftGuideData, CraftGuideStepData craftGuideStepData, CraftLayer layer) {
        super(maid, craftGuideData, craftGuideStepData, layer);
    }

    @Override
    public Result start() {
        if (craftGuideStepData.getStorage() == null)
            return Result.FAIL;
        return Result.CONTINUE;
    }

    @Override
    public Result tick() {
        Level level = maid.level();
        if (!level.getBlockState(craftGuideStepData.storage.pos).is(WorkBlockTags.SMITHING_TABLE))
            return Result.NOT_DONE;
        ResourceHandler<ItemResource> inv = maid.getAvailableInv(false);
        List<ItemStack> input = craftGuideStepData.getInput();
        List<ItemStack> output = craftGuideStepData.getOutput();
        int[] slotExtractCount = new int[inv.size()];
        Arrays.fill(slotExtractCount, 0);
        boolean allMatch = true;
        for (int i = 0; i < input.size(); i++) {
            boolean found = false;
            if (input.get(i).isEmpty()) continue;
            for (int j = 0; j < inv.size(); j++) {
                if (ItemStack.isSameItem(ItemUtil.getStack(inv, j), input.get(i))) {
                    //还有剩余（
                    if (ItemUtil.getStack(inv, j).getCount() > slotExtractCount[j]) {
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
            Optional<RecipeHolder<SmithingRecipe>> recipe = RecipeUtil.getSmithingRecipe(level, input);
            if (recipe.isPresent()) {
                SmithingRecipeInput recipeInput = new SmithingRecipeInput(input.get(0), input.get(1), input.get(2));
                ItemStack result = recipe.get().value().assemble(recipeInput);
                if (ItemStackUtil.isSameInCrafting(result, output.get(0))) {
                    craftLayer.addCurrentStepPlacedCounts(0, result.getCount());
                }

                int maxCanPlace = InvUtil.maxCanPlace(inv, result);
                if (maxCanPlace >= result.getCount()) {
                    InvUtil.tryPlace(inv, result);
                    for (int j = 0; j < inv.size(); j++) {
                        ItemStack slotStack = ItemUtil.getStack(inv, j);
                        try (var tx = Transaction.open(null)) {
                            inv.extract(j, ItemResource.of(slotStack), slotExtractCount[j], tx);
                            tx.commit();
                        }
                    }
                    level.levelEvent(1044, craftGuideStepData.storage.pos, 0);
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
