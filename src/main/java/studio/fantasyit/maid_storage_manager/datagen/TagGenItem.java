package studio.fantasyit.maid_storage_manager.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;

import java.util.concurrent.CompletableFuture;

public class TagGenItem extends TagsProvider<Item> {

    protected TagGenItem(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, Registries.ITEM, lookupProvider, MaidStorageManager.MODID);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        getOrCreateRawBuilder(TagKey.create(Registries.ITEM,
                Identifier.fromNamespaceAndPath(MaidStorageManager.MODID, "no_components")))
                .addElement(Identifier.fromNamespaceAndPath("botania", "twig_wand"));

        getOrCreateRawBuilder(TagKey.create(Registries.ITEM,
                Identifier.fromNamespaceAndPath(MaidStorageManager.MODID, "use_components")));
    }
}
