package studio.fantasyit.maid_storage_manager.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagEntry;
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
                Identifier.fromNamespaceAndPath(MaidStorageManager.MODID, "no_nbt")))
                .addOptionalElement(Identifier.fromNamespaceAndPath("botania", "twig_wand"));

        getOrCreateRawBuilder(TagKey.create(Registries.ITEM,
                Identifier.fromNamespaceAndPath(MaidStorageManager.MODID, "use_nbt")))
                .addElement(Identifier.fromNamespaceAndPath("minecraft", "tipped_arrow"))
                .addElement(Identifier.fromNamespaceAndPath("minecraft", "firework_rocket"))
                .addElement(Identifier.fromNamespaceAndPath("minecraft", "firework_star"))
                .addElement(Identifier.fromNamespaceAndPath("minecraft", "potion"))
                .addElement(Identifier.fromNamespaceAndPath("minecraft", "splash_potion"))
                .addElement(Identifier.fromNamespaceAndPath("minecraft", "lingering_potion"))
                .addElement(Identifier.fromNamespaceAndPath("minecraft", "enchanted_book"))
                .add(TagEntry.tag(Identifier.fromNamespaceAndPath("forge", "tools")))
                .add(TagEntry.tag(Identifier.fromNamespaceAndPath("forge", "armors")))
                .addElement(Identifier.fromNamespaceAndPath(MaidStorageManager.MODID, "work_card"))
                .addOptionalElement(Identifier.fromNamespaceAndPath("tacz", "ammo"))
                .addOptionalElement(Identifier.fromNamespaceAndPath("tacz", "attachment"))
                .addOptionalElement(Identifier.fromNamespaceAndPath("tacz", "modern_kinetic_gun"))
                .add(TagEntry.optionalTag(Identifier.fromNamespaceAndPath("modulargolems", "parts")))
                .add(TagEntry.optionalTag(Identifier.fromNamespaceAndPath("modulargolems", "holders")))
                .addOptionalElement(Identifier.fromNamespaceAndPath("modulargolems", "golem_facade"));
    }
}
