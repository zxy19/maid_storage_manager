package studio.fantasyit.maid_storage_manager.craft;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;

public class WorkBlockTags {
    public static TagKey<Block> CRAFTING_TABLE = TagKey.create(
            BuiltInRegistries.BLOCK.key(),
            ResourceLocation.fromNamespaceAndPath(MaidStorageManager.MODID, "working_block/crafting_table")
    );
    public static TagKey<Block> FURNACE = TagKey.create(
            BuiltInRegistries.BLOCK.key(),
            ResourceLocation.fromNamespaceAndPath(MaidStorageManager.MODID, "working_block/furnace")
    );
    public static TagKey<Block> BREWING_STAND = TagKey.create(
            BuiltInRegistries.BLOCK.key(),
            ResourceLocation.fromNamespaceAndPath(MaidStorageManager.MODID, "working_block/brewing_stand")
    );
    public static TagKey<Block> SMITHING_TABLE = TagKey.create(
            BuiltInRegistries.BLOCK.key(),
            ResourceLocation.fromNamespaceAndPath(MaidStorageManager.MODID, "working_block/smithing_table")
    );
    public static TagKey<Block> STONE_CUTTER = TagKey.create(
            BuiltInRegistries.BLOCK.key(),
            ResourceLocation.fromNamespaceAndPath(MaidStorageManager.MODID, "working_block/stone_cutter")
    );
}
