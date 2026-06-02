package studio.fantasyit.maid_storage_manager.datagen;

import com.github.tartaricacid.touhoulittlemaid.datagen.builder.AltarRecipeBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.items.data.ItemStackData;
import studio.fantasyit.maid_storage_manager.recipe.CopyConfigRecipe;
import studio.fantasyit.maid_storage_manager.recipe.ListClearRecipe;
import studio.fantasyit.maid_storage_manager.registry.DataComponentRegistry;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class RecipeDataGen extends RecipeProvider {

    public RecipeDataGen(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        TagKey<Item> dyes = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "dyes"));
        TagKey<Item> dyeRed = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "dyes/red"));
        TagKey<Item> dyeGreen = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "dyes/green"));
        TagKey<Item> dyeBlue = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "dyes/blue"));
        TagKey<Item> strings = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "strings"));
        TagKey<Item> chests = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "chests"));
        TagKey<Item> barrels = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "barrels"));
        TagKey<Item> signs = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("minecraft", "signs"));
        TagKey<Item> buttons = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("minecraft", "buttons"));

        TagKey<Item> sbDiamond = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "storage_blocks/diamond"));
        TagKey<Item> sbLapis = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "storage_blocks/lapis"));
        TagKey<Item> sbGold = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "storage_blocks/gold"));
        TagKey<Item> sbRedstone = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "storage_blocks/redstone"));
        TagKey<Item> sbIron = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "storage_blocks/iron"));
        TagKey<Item> sbCoal = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "storage_blocks/coal"));

        buildAltarRecipes(dyes, dyeRed, dyeGreen, dyeBlue, strings, chests, barrels, signs, buttons,
                sbDiamond, sbLapis, sbGold, sbRedstone, sbIron, sbCoal);
        buildCopyRecipes();
        buildListClearRecipes();
        buildVanillaShapelessRecipes();
    }

    private void buildAltarRecipes(TagKey<Item> dyes, TagKey<Item> dyeRed, TagKey<Item> dyeGreen, TagKey<Item> dyeBlue,
                                   TagKey<Item> strings, TagKey<Item> chests, TagKey<Item> barrels,
                                   TagKey<Item> signs, TagKey<Item> buttons,
                                   TagKey<Item> sbDiamond, TagKey<Item> sbLapis, TagKey<Item> sbGold,
                                   TagKey<Item> sbRedstone, TagKey<Item> sbIron, TagKey<Item> sbCoal) {
        altarFilterList(dyes);
        altarRequestList();
        altarProgressPad();
        altarNoAccess(dyeRed, signs);
        altarWorkCard(dyeBlue, strings);
        altarChangeFlag();
        altarCraftGuide();
        altarAllowAccess(dyeGreen, signs);
        altarInventoryList();
        altarLogisticsGuide(chests, barrels);
        altarStorageDefineBauble(dyeRed, dyeGreen);
        altarLogisticsGuideRabbit();
        altarProgressPadFilledMap();
        altarPortableCraftCalculator(sbDiamond, sbLapis, sbGold, sbRedstone, sbIron, sbCoal);
        altarConfigurableCommunicateTerminal(buttons);
    }

    private void altarFilterList(TagKey<Item> dyes) {
        AltarRecipeBuilder.shapeless(this.items, makeComponentResult(ItemRegistry.FILTER_LIST.get(), List.of(
                itemData(ItemRegistry.FILTER_LIST.get()),
                itemData(ItemRegistry.FILTER_LIST.get()),
                itemData(ItemRegistry.FILTER_LIST.get())
        )))
                .requires(dyes)
                .requires(3, Items.PAPER)
                .save(this.output, recipeKey("filter_list"));
    }

    private void altarRequestList() {
        AltarRecipeBuilder.shapeless(this.items, ItemRegistry.REQUEST_LIST_ITEM.get())
                .requires(Items.BOOK)
                .requires(Items.SUGAR)
                .save(this.output, recipeKey("request_list"));
    }

    private void altarProgressPad() {
        AltarRecipeBuilder.shapeless(this.items, ItemRegistry.PROGRESS_PAD.get())
                .requires(ItemRegistry.WORK_CARD.get())
                .requires(Items.MAP)
                .requires(Items.ENDER_PEARL)
                .save(this.output, recipeKey("progress_pad"));
    }

    private void altarNoAccess(TagKey<Item> dyeRed, TagKey<Item> signs) {
        AltarRecipeBuilder.shapeless(this.items, ItemRegistry.NO_ACCESS.get(), 3)
                .requires(dyeRed)
                .requires(3, signs)
                .save(this.output, recipeKey("no_access"));
    }

    private void altarWorkCard(TagKey<Item> dyeBlue, TagKey<Item> strings) {
        AltarRecipeBuilder.shapeless(this.items, ItemRegistry.WORK_CARD.get(), 2)
                .requires(dyeBlue)
                .requires(strings)
                .requires(Items.PAPER)
                .save(this.output, recipeKey("work_card"));
    }

    private void altarChangeFlag() {
        AltarRecipeBuilder.shapeless(this.items, ItemRegistry.CHANGE_FLAG.get())
                .requires(Items.ORANGE_BANNER)
                .requires(Items.STICK)
                .save(this.output, recipeKey("change_flag"));
    }

    private void altarCraftGuide() {
        AltarRecipeBuilder.shapeless(this.items, makeComponentResult(ItemRegistry.CRAFT_GUIDE.get(), List.of(
                itemData(ItemRegistry.CRAFT_GUIDE.get()),
                itemData(ItemRegistry.CRAFT_GUIDE.get()),
                itemData(ItemRegistry.CRAFT_GUIDE.get())
        )))
                .requires(Items.CRAFTING_TABLE)
                .requires(Items.FURNACE)
                .requires(Items.PAINTING)
                .save(this.output, recipeKey("craft_guide"));
    }

    private void altarAllowAccess(TagKey<Item> dyeGreen, TagKey<Item> signs) {
        AltarRecipeBuilder.shapeless(this.items, ItemRegistry.ALLOW_ACCESS.get(), 3)
                .requires(dyeGreen)
                .requires(3, signs)
                .save(this.output, recipeKey("allow_access"));
    }

    private void altarInventoryList() {
        AltarRecipeBuilder.shapeless(this.items, ItemRegistry.INVENTORY_LIST.get())
                .requires(Items.WRITABLE_BOOK)
                .requires(Items.CAKE)
                .save(this.output, recipeKey("inventory_list"));
    }

    private void altarLogisticsGuide(TagKey<Item> chests, TagKey<Item> barrels) {
        AltarRecipeBuilder.shapeless(this.items, makeComponentResult(ItemRegistry.LOGISTICS_GUIDE.get(), List.of(
                itemData(ItemRegistry.LOGISTICS_GUIDE.get()),
                itemData(ItemRegistry.LOGISTICS_GUIDE.get()),
                itemData(ItemRegistry.LOGISTICS_GUIDE.get())
        )))
                .requires(chests)
                .requires(barrels)
                .requires(Items.PAINTING)
                .save(this.output, recipeKey("logistics_guide"));
    }

    private void altarStorageDefineBauble(TagKey<Item> dyeRed, TagKey<Item> dyeGreen) {
        AltarRecipeBuilder.shapeless(this.items, ItemRegistry.STORAGE_DEFINE_BAUBLE.get())
                .requires(dyeRed)
                .requires(dyeGreen)
                .requires(Items.NAME_TAG)
                .save(this.output, recipeKey("storage_define_bauble"));
    }

    private void altarLogisticsGuideRabbit() {
        AltarRecipeBuilder.shapeless(this.items, makeComponentResult(ItemRegistry.LOGISTICS_GUIDE.get(), List.of(
                itemData(ItemRegistry.LOGISTICS_GUIDE.get()),
                itemData(ItemRegistry.LOGISTICS_GUIDE.get()),
                itemData(ItemRegistry.LOGISTICS_GUIDE.get())
        )))
                .requires(Items.RABBIT_FOOT)
                .requires(Items.RABBIT)
                .requires(Items.RABBIT_HIDE)
                .save(this.output, recipeKey("logistics_guide_rabbit"));
    }

    private void altarProgressPadFilledMap() {
        AltarRecipeBuilder.shapeless(this.items, ItemRegistry.PROGRESS_PAD.get())
                .requires(ItemRegistry.WORK_CARD.get())
                .requires(Items.FILLED_MAP)
                .requires(Items.ENDER_PEARL)
                .save(this.output, recipeKey("progress_pad_filled_map"));
    }

    private void altarPortableCraftCalculator(TagKey<Item> sbDiamond, TagKey<Item> sbLapis, TagKey<Item> sbGold,
                                              TagKey<Item> sbRedstone, TagKey<Item> sbIron, TagKey<Item> sbCoal) {
        AltarRecipeBuilder.shapeless(this.items, ItemRegistry.PORTABLE_CRAFT_CALCULATOR_BAUBLE.get())
                .requires(sbDiamond)
                .requires(sbLapis)
                .requires(sbGold)
                .requires(sbRedstone)
                .requires(sbIron)
                .requires(sbCoal)
                .power(0.9F)
                .save(this.output, recipeKey("portable_craft_calculator_bauble"));
    }

    private void altarConfigurableCommunicateTerminal(TagKey<Item> buttons) {
        AltarRecipeBuilder.shapeless(this.items, makeComponentResult(ItemRegistry.CONFIGURABLE_COMMUNICATE_MARK.get(), List.of(
                itemData(ItemRegistry.CONFIGURABLE_COMMUNICATE_MARK.get()),
                itemData(ItemRegistry.CONFIGURABLE_COMMUNICATE_MARK.get())
        )))
                .requires(Items.LIGHTNING_ROD)
                .requires(Items.REDSTONE)
                .requires(Items.ITEM_FRAME)
                .requires(buttons)
                .save(this.output, recipeKey("configurable_communicate_terminal"));
    }

    private void buildCopyRecipes() {
        copyRecipe(ItemRegistry.CHANGE_FLAG.get(), "change_flag_copy");
        copyRecipe(ItemRegistry.CRAFT_GUIDE.get(), "craft_guide_copy");
        copyRecipe(ItemRegistry.FILTER_LIST.get(), "filter_list_copy");
        copyRecipe(ItemRegistry.REQUEST_LIST_ITEM.get(), "request_list_copy");
    }

    private void copyRecipe(Item item, String name) {
        ShapelessRecipe inner = new ShapelessRecipe(
                new Recipe.CommonInfo(true),
                new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, ""),
                new ItemStackTemplate(item),
                List.of(Ingredient.of(item), Ingredient.of(item)));
        this.output.accept(recipeKey(name), new CopyConfigRecipe(inner), null);
    }

    private void buildListClearRecipes() {
        ShapelessRecipe inner1 = new ShapelessRecipe(
                new Recipe.CommonInfo(true),
                new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, ""),
                new ItemStackTemplate(ItemRegistry.PROGRESS_PAD.get()),
                List.of(Ingredient.of(ItemRegistry.PROGRESS_PAD.get())));
        this.output.accept(recipeKey("progress_pad_reuse"), new ListClearRecipe(inner1), null);

        ShapelessRecipe inner2 = new ShapelessRecipe(
                new Recipe.CommonInfo(true),
                new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, ""),
                new ItemStackTemplate(ItemRegistry.REQUEST_LIST_ITEM.get()),
                List.of(Ingredient.of(ItemRegistry.REQUEST_LIST_ITEM.get())));
        this.output.accept(recipeKey("request_list_reset"), new ListClearRecipe(inner2), null);

        ShapelessRecipe inner3 = new ShapelessRecipe(
                new Recipe.CommonInfo(true),
                new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, ""),
                new ItemStackTemplate(ItemRegistry.INVENTORY_LIST.get()),
                List.of(Ingredient.of(ItemRegistry.WRITTEN_INVENTORY_LIST.get())));
        this.output.accept(recipeKey("inventory_list_reuse"), new ListClearRecipe(inner3), null);
    }

    private void buildVanillaShapelessRecipes() {
        this.oneToOneConversionRecipe(ItemRegistry.ALLOW_ACCESS.get(), ItemRegistry.NO_ACCESS.get(), "allow_access_to_no_access");
        this.oneToOneConversionRecipe(ItemRegistry.NO_ACCESS.get(), ItemRegistry.ALLOW_ACCESS.get(), "no_access_to_allow_access");
        this.oneToOneConversionRecipe(ItemRegistry.STORAGE_DEFINE_BAUBLE.get(), ItemRegistry.STORAGE_DEFINE_BAUBLE.get(), "storage_define_bauble_reuse");
    }

    private static ItemStackTemplate makeComponentResult(Item item, List<ItemStackData> spawnItems) {
        DataComponentPatch patch = DataComponentPatch.builder()
                .set(DataComponentRegistry.TO_SPAWN_ITEMS.get(), spawnItems)
                .build();
        return new ItemStackTemplate(item, patch);
    }

    private static ItemStackData itemData(Item item) {
        return new ItemStackData(Optional.of(new ItemStackTemplate(item)));
    }

    private static ResourceKey<Recipe<?>> recipeKey(String name) {
        return ResourceKey.create(Registries.RECIPE,
                Identifier.fromNamespaceAndPath(MaidStorageManager.MODID, name));
    }

    public static class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            super(output, registries);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
            return new RecipeDataGen(registries, output);
        }

        @Override
        public String getName() {
            return "Recipes";
        }
    }
}
