package studio.fantasyit.maid_storage_manager.datagen;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.items.render.CraftGuideSMR;
import studio.fantasyit.maid_storage_manager.items.render.FilterListSMR;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;

import java.util.Optional;

public class ModelGen extends ModelProvider {
    private final ResourceManager rm;

    public ModelGen(PackOutput output, ResourceManager rm) {
        super(output, MaidStorageManager.MODID);
        this.rm = rm;
    }

    public static final ModelTemplate ITEM_SIMPLE = new ModelTemplate(
            Optional.of(Identifier.withDefaultNamespace("item/generated")),
            Optional.empty(),
            TextureSlot.LAYER0
    );

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        Identifier requestListModel = itemModels.createFlatItemModel(ItemRegistry.REQUEST_LIST_ITEM.get(), ITEM_SIMPLE);
        Identifier virtualRequestListModel = ITEM_SIMPLE.create(
                Identifier.fromNamespaceAndPath(MaidStorageManager.MODID, "virtual_request_list").withPrefix("item/"),
                new TextureMapping().put(TextureSlot.LAYER0, new Material(Identifier.fromNamespaceAndPath(MaidStorageManager.MODID, "item/request_list"))),
                itemModels.modelOutput);
        itemModels.itemModelOutput.accept(ItemRegistry.VIRTUAL_REQUEST_LIST_ITEM.get(), ItemModelUtils.plainModel(virtualRequestListModel));
        itemModels.generateFlatItem(ItemRegistry.INVENTORY_LIST.get(), ITEM_SIMPLE);
        itemModels.generateFlatItem(ItemRegistry.WRITTEN_INVENTORY_LIST.get(), ITEM_SIMPLE);

        Identifier filterListModel = itemModels.createFlatItemModel(ItemRegistry.FILTER_LIST.get(), ITEM_SIMPLE);
        itemModels.itemModelOutput.accept(ItemRegistry.FILTER_LIST.get(),
                ItemModelUtils.specialModel(filterListModel, new FilterListSMR.Unbaked(filterListModel)));

        itemModels.generateFlatItem(ItemRegistry.STORAGE_DEFINE_BAUBLE.get(), ITEM_SIMPLE);
        itemModels.generateFlatItem(ItemRegistry.NO_ACCESS.get(), ITEM_SIMPLE);
        itemModels.generateFlatItem(ItemRegistry.ALLOW_ACCESS.get(), ITEM_SIMPLE);

        // CraftGuide的双层模型
        Identifier craftGuideModel = itemModels.createFlatItemModel(ItemRegistry.CRAFT_GUIDE.get(), ITEM_SIMPLE);
        Identifier id = Identifier.fromNamespaceAndPath(MaidStorageManager.MODID, "craft_guide_blank");
        Identifier identifier = ITEM_SIMPLE.create(id.withPrefix("item/"), new TextureMapping().put(TextureSlot.LAYER0, new Material(id.withPrefix("item/"))), itemModels.modelOutput);
        itemModels.itemModelOutput.accept(ItemRegistry.CRAFT_GUIDE.get(),
                ItemModelUtils.specialModel(craftGuideModel, new CraftGuideSMR.Unbaked(craftGuideModel, identifier)));

        Identifier portableCraftCalcModel = ITEM_SIMPLE.create(
                Identifier.fromNamespaceAndPath(MaidStorageManager.MODID, "portable_craft_calculator_bauble").withPrefix("item/"),
                new TextureMapping().put(TextureSlot.LAYER0, new Material(Identifier.fromNamespaceAndPath(MaidStorageManager.MODID, "item/portable_craft_calculator"))),
                itemModels.modelOutput);
        itemModels.itemModelOutput.accept(ItemRegistry.PORTABLE_CRAFT_CALCULATOR_BAUBLE.get(), ItemModelUtils.plainModel(portableCraftCalcModel));
        itemModels.generateFlatItem(ItemRegistry.WORK_CARD.get(), ITEM_SIMPLE);

        itemModels.generateFlatItem(ItemRegistry.LOGISTICS_GUIDE.get(), ITEM_SIMPLE);

        itemModels.generateFlatItem(ItemRegistry.CHANGE_FLAG.get(), ITEM_SIMPLE);
        itemModels.generateFlatItem(ItemRegistry.PROGRESS_PAD.get(), ITEM_SIMPLE);
        itemModels.generateFlatItem(ItemRegistry.CONFIGURABLE_COMMUNICATE_MARK.get(), ITEM_SIMPLE);
    }
}
