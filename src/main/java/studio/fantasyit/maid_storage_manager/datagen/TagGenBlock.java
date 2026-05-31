package studio.fantasyit.maid_storage_manager.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;

import java.util.concurrent.CompletableFuture;

public class TagGenBlock extends TagsProvider<Block> {

    protected TagGenBlock(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, Registries.BLOCK, lookupProvider, MaidStorageManager.MODID);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        getOrCreateRawBuilder(TagKey.create(Registries.BLOCK,
                Identifier.fromNamespaceAndPath(MaidStorageManager.MODID, "working_block/furnace")))
                .addElement(BuiltInRegistries.BLOCK.getKey(Blocks.FURNACE))
                .addElement(BuiltInRegistries.BLOCK.getKey(Blocks.BLAST_FURNACE))
                .addElement(BuiltInRegistries.BLOCK.getKey(Blocks.SMOKER));

        getOrCreateRawBuilder(TagKey.create(Registries.BLOCK,
                Identifier.fromNamespaceAndPath(MaidStorageManager.MODID, "working_block/stone_cutter")))
                .addElement(BuiltInRegistries.BLOCK.getKey(Blocks.STONECUTTER));

        getOrCreateRawBuilder(TagKey.create(Registries.BLOCK,
                Identifier.fromNamespaceAndPath(MaidStorageManager.MODID, "working_block/brewing_stand")))
                .addElement(BuiltInRegistries.BLOCK.getKey(Blocks.BREWING_STAND));

        getOrCreateRawBuilder(TagKey.create(Registries.BLOCK,
                Identifier.fromNamespaceAndPath(MaidStorageManager.MODID, "working_block/crafting_table")))
                .addElement(BuiltInRegistries.BLOCK.getKey(Blocks.CRAFTING_TABLE));

        getOrCreateRawBuilder(TagKey.create(Registries.BLOCK,
                Identifier.fromNamespaceAndPath(MaidStorageManager.MODID, "working_block/smithing_table")))
                .addElement(BuiltInRegistries.BLOCK.getKey(Blocks.SMITHING_TABLE));

        getOrCreateRawBuilder(TagKey.create(Registries.BLOCK,
                Identifier.fromNamespaceAndPath(MaidStorageManager.MODID, "mb_chests")));

        getOrCreateRawBuilder(TagKey.create(Registries.BLOCK,
                Identifier.fromNamespaceAndPath(MaidStorageManager.MODID, "sortable_chests")));

        getOrCreateRawBuilder(TagKey.create(Registries.BLOCK,
                Identifier.fromNamespaceAndPath(MaidStorageManager.MODID, "default_storage_blocks")));

        getOrCreateRawBuilder(TagKey.create(Registries.BLOCK,
                Identifier.fromNamespaceAndPath(MaidStorageManager.MODID, "create_package_container")));
    }
}
