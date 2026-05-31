package studio.fantasyit.maid_storage_manager.craft.generator.type.ars;

import com.hollingsworth.arsnouveau.common.crafting.recipes.EnchantingApparatusRecipe;
import com.hollingsworth.arsnouveau.setup.registry.RecipeRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;

public class GeneratorArsNouveauApparatus extends GeneratorArsNouveauEnchantApp<EnchantingApparatusRecipe> {
    @Override
    protected DeferredHolder<RecipeType<?>, RecipeRegistry.ModRecipeType<EnchantingApparatusRecipe>> getRecipeType() {
        return RecipeRegistry.APPARATUS_TYPE;
    }

    @Override
    public Component getConfigName() {
        return Component.translatable("config.maid_storage_manager.crafting.generating.ars.apparatus");
    }
}
