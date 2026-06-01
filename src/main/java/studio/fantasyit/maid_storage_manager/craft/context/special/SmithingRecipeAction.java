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

        try (Transaction transaction = Transaction.openRoot()) {
            // 1. 提取输入物品
            List<ItemStack> realInput = new ArrayList<>();
            for (ItemStack itemStack : input) {
                realInput.add(itemStack.copy());
                if (itemStack.isEmpty()) continue;
                ItemResource itemResource = ItemResource.of(itemStack);
                int count = itemStack.getCount();
                if (inv.extract(itemResource, count, transaction) != count)
                    return Result.FAIL;
            }

            Optional<RecipeHolder<SmithingRecipe>> recipeOpt = RecipeUtil.getSmithingRecipe(level, realInput);
            if (recipeOpt.isEmpty())
                return Result.FAIL;
            SmithingRecipe recipe = recipeOpt.get().value();

            ItemStack template = realInput.size() > 0 ? realInput.get(0) : ItemStack.EMPTY;
            ItemStack base     = realInput.size() > 1 ? realInput.get(1) : ItemStack.EMPTY;
            ItemStack addition = realInput.size() > 2 ? realInput.get(2) : ItemStack.EMPTY;
            SmithingRecipeInput recipeInput = new SmithingRecipeInput(template, base, addition);
            ItemStack result = recipe.assemble(recipeInput);

            if (!ItemStackUtil.isSameInCrafting(result, output.getFirst()))
                return Result.FAIL;

            if (inv.insert(ItemResource.of(result), result.getCount(), transaction) != result.getCount())
                return Result.FAIL;
            transaction.commit();
        }

        // 成功，播放音效
        level.levelEvent(1044, craftGuideStepData.storage.pos, 0);
        return Result.SUCCESS;
    }

    @Override
    public void stop() {
    }
}