package studio.fantasyit.maid_storage_manager.craft.context.special;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.craft.WorkBlockTags;
import studio.fantasyit.maid_storage_manager.craft.context.AbstractCraftActionContext;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.work.CraftLayer;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;
import studio.fantasyit.maid_storage_manager.util.RecipeUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CraftingRecipeAction extends AbstractCraftActionContext {
    public static final Identifier TYPE = Identifier.fromNamespaceAndPath(MaidStorageManager.MODID, "craft");

    public CraftingRecipeAction(EntityMaid maid, CraftGuideData craftGuideData, CraftGuideStepData craftGuideStepData, CraftLayer layer) {
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
        if (!level.getBlockState(craftGuideStepData.storage.pos).is(WorkBlockTags.CRAFTING_TABLE))
            return Result.NOT_DONE;
        ResourceHandler<ItemResource> inv = maid.getAvailableInv(false);
        List<ItemStack> input = craftGuideStepData.getInput();
        List<ItemStack> output = craftGuideStepData.getOutput();
        List<ItemStack> realInput = new ArrayList<>();
        try (Transaction transaction = Transaction.openRoot()) {
            for (ItemStack itemStack : input) {
                realInput.add(itemStack.copy());
                if (itemStack.isEmpty()) continue;
                ItemResource itemResource = ItemResource.of(itemStack);
                int count = itemStack.getCount();
                if (inv.extract(itemResource, count, transaction) != count)
                    return Result.FAIL;
            }
            Optional<RecipeHolder<CraftingRecipe>> result = ((ServerLevel) level).recipeAccess().getRecipeFor(RecipeType.CRAFTING, RecipeUtil.wrapCraftingContainer(realInput, 3, 3).asCraftInput(), level);
            if (result.isEmpty())
                return Result.FAIL;
            CraftingRecipe recipe = result.get().value();
            CraftingInput craftingInput = RecipeUtil.wrapCraftingContainer(realInput, recipe);
            ItemStack tmpResult = recipe.assemble(craftingInput);
            if (!ItemStackUtil.isSameInCrafting(tmpResult, output.getFirst()))
                return Result.FAIL;
            if (inv.insert(ItemResource.of(tmpResult), tmpResult.getCount(), transaction) != tmpResult.getCount())
                return Result.FAIL;
            for (ItemStack itemStack : craftingInput.items()) {
                if (itemStack.isEmpty()) continue;
                if (itemStack.getCraftingRemainder() == null) continue;
                ItemStack reminder = itemStack.getCraftingRemainder().create();
                if (reminder.isEmpty()) continue;
                if (inv.insert(ItemResource.of(reminder), reminder.getCount(), transaction) != reminder.getCount())
                    return Result.FAIL;
            }
            transaction.commit();
        }
        return Result.SUCCESS;
    }

    @Override
    public void stop() {

    }
}
