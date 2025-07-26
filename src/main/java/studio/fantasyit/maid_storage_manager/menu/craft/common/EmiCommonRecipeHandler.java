package studio.fantasyit.maid_storage_manager.menu.craft.common;

import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import studio.fantasyit.maid_storage_manager.network.CraftGuideGuiPacket;
import studio.fantasyit.maid_storage_manager.util.InventoryListUtil;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EmiCommonRecipeHandler implements EmiRecipeHandler<CommonCraftMenu> {
    @Override
    public EmiPlayerInventory getInventory(AbstractContainerScreen<CommonCraftMenu> screen) {
        return new EmiPlayerInventory(List.of());
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return true;
    }

    @Override
    public boolean alwaysDisplaySupport(EmiRecipe recipe) {
        return true;
    }

    protected boolean work(EmiRecipe recipe, EmiCraftContext<CommonCraftMenu> context, boolean doTransfer) {
        CommonCraftMenu container = context.getScreenHandler();
        List<ItemStack> outputs = new ArrayList<>();
        recipe.getOutputs()
                .stream()
                .map(e -> e.getEmiStacks().stream().map(EmiStack::getItemStack).findFirst())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(i -> ItemStackUtil.addToList(outputs, i.copy(), true));

        List<ItemStack> inputs = new ArrayList<>();
        recipe.getInputs()
                .stream()
                .map(e -> InventoryListUtil.getMatchingForPlayer(e.getEmiStacks().stream().map(EmiStack::getItemStack).toList()))
                .filter(i -> !i.isEmpty())
                .forEach(i -> ItemStackUtil.addToList(inputs, i.copy(), true));

        int inputId = 0;
        int outputId = 0;
        CompoundTag data = new CompoundTag();
        ListTag inputTag = new ListTag();
        ListTag outputTag = new ListTag();
        for (CommonStepDataContainer step : container.steps) {
            for (int i = 0; i < step.step.actionType.inputCount(); i++) {
                if (inputId < inputs.size()) {
                    if (doTransfer)
                        inputTag.add(ItemStackUtil.saveStack(context.getScreenHandler().player.registryAccess(), inputs.get(inputId)));
                    inputId++;
                }
            }
            for (int i = 0; i < step.step.actionType.outputCount(); i++) {
                if (outputId < outputs.size()) {
                    if (doTransfer)
                        outputTag.add(ItemStackUtil.saveStack(context.getScreenHandler().player.registryAccess(), outputs.get(outputId)));
                    outputId++;
                }
            }
        }
        if (doTransfer) {
            data.put("inputs", inputTag);
            data.put("outputs", outputTag);
            PacketDistributor.sendToServer(new CraftGuideGuiPacket(
                    CraftGuideGuiPacket.Type.SET_ALL_INPUT,
                    0,
                    0,
                    data
            ));
        }
        if (inputId != inputs.size() || outputId != outputs.size()) {
            return false;
        }
        return true;
    }

    @Override
    public boolean canCraft(EmiRecipe recipe, EmiCraftContext<CommonCraftMenu> context) {
        return work(recipe, context, false);
    }

    @Override
    public boolean craft(EmiRecipe recipe, EmiCraftContext<CommonCraftMenu> context) {
        return work(recipe, context, true);
    }
}
