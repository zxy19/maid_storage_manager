package studio.fantasyit.maid_storage_manager.craft.generator.type.ars;

import com.hollingsworth.arsnouveau.common.crafting.recipes.EnchantmentRecipe;
import com.hollingsworth.arsnouveau.setup.registry.RecipeRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;

public class GeneratorArsNouveauEnchanting extends GeneratorArsNouveauEnchantApp<EnchantmentRecipe> {
    @Override
    protected DeferredHolder<RecipeType<?>, RecipeRegistry.ModRecipeType<EnchantmentRecipe>> getRecipeType() {
        return RecipeRegistry.ENCHANTMENT_TYPE;
    }

    @Override
    public Component getConfigName() {
        return Component.translatable("config.maid_storage_manager.crafting.generating.ars.enchanting");
    }
}
