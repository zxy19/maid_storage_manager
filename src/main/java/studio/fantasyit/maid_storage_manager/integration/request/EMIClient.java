package studio.fantasyit.maid_storage_manager.integration.request;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;

import java.util.List;

public class EMIClient {
    public static void processRequestNearByClient(EmiRecipe recipe) {
        List<EmiIngredient> recipeSlotsView = recipe.getInputs();
        IngredientRequestClient.processRequestNearByClient(
                recipeSlotsView
                        .stream()
                        .map(e -> e
                                .getEmiStacks()
                                .stream()
                                .map(EmiStack::getItemStack)
                                .filter(i -> !i.isEmpty())
                                .toList())
                        .toList()
        );
    }
}
