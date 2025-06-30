package studio.fantasyit.maid_storage_manager.craft.generator.type.ars;

import com.hollingsworth.arsnouveau.api.enchanting_apparatus.EnchantingApparatusRecipe;
import com.hollingsworth.arsnouveau.setup.registry.RecipeRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.RegistryObject;

public class GeneratorArsNouveauApparatus extends GeneratorArsNouveauEnchantApp<EnchantingApparatusRecipe> {
    @Override
    protected RegistryObject<RecipeType<EnchantingApparatusRecipe>> getRecipeType() {
        return RecipeRegistry.APPARATUS_TYPE;
    }

    @Override
    public Component getConfigName() {
        return Component.translatable("config.maid_storage_manager.crafting.generating.ars.apparatus");
    }
}
