package studio.fantasyit.maid_storage_manager.datagen;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
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
        itemModels.generateFlatItem(ItemRegistry.REQUEST_LIST_ITEM.get(), ITEM_SIMPLE);
        itemModels.generateFlatItem(ItemRegistry.VIRTUAL_REQUEST_LIST_ITEM.get(), ITEM_SIMPLE);
        itemModels.generateFlatItem(ItemRegistry.INVENTORY_LIST.get(), ITEM_SIMPLE);
        itemModels.generateFlatItem(ItemRegistry.WRITTEN_INVENTORY_LIST.get(), ITEM_SIMPLE);
        itemModels.generateFlatItem(ItemRegistry.FILTER_LIST.get(), ITEM_SIMPLE);
        itemModels.generateFlatItem(ItemRegistry.STORAGE_DEFINE_BAUBLE.get(), ITEM_SIMPLE);
        itemModels.generateFlatItem(ItemRegistry.NO_ACCESS.get(), ITEM_SIMPLE);
        itemModels.generateFlatItem(ItemRegistry.ALLOW_ACCESS.get(), ITEM_SIMPLE);
        itemModels.generateFlatItem(ItemRegistry.CRAFT_GUIDE.get(), ITEM_SIMPLE);
        itemModels.generateFlatItem(ItemRegistry.PORTABLE_CRAFT_CALCULATOR_BAUBLE.get(), ITEM_SIMPLE);
        itemModels.generateFlatItem(ItemRegistry.WORK_CARD.get(), ITEM_SIMPLE);
        itemModels.generateFlatItem(ItemRegistry.LOGISTICS_GUIDE.get(), ITEM_SIMPLE);
        itemModels.generateFlatItem(ItemRegistry.CHANGE_FLAG.get(), ITEM_SIMPLE);
        itemModels.generateFlatItem(ItemRegistry.PROGRESS_PAD.get(), ITEM_SIMPLE);
        itemModels.generateFlatItem(ItemRegistry.CONFIGURABLE_COMMUNICATE_MARK.get(), ITEM_SIMPLE);
    }
}
