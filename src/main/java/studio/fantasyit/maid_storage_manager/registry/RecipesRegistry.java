package studio.fantasyit.maid_storage_manager.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.recipe.CopyConfigRecipe;
import studio.fantasyit.maid_storage_manager.recipe.ListClearRecipe;


public class RecipesRegistry {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, MaidStorageManager.MODID);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ListClearRecipe>> LIST_CLEAR_SERIALIZERS = RECIPE_SERIALIZERS.register("list_clear", ListClearRecipe.Serializer::new);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CopyConfigRecipe>> COPY_CONFIG_SERIALIZERS = RECIPE_SERIALIZERS.register("copy", CopyConfigRecipe.Serializer::new);

    public static void register(IEventBus modEventBus) {
        RECIPE_SERIALIZERS.register(modEventBus);
    }
}
