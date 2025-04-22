package studio.fantasyit.maid_storage_manager.menu.craft.common;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.menu.base.ImageAsset;

import java.util.HashMap;
import java.util.Map;

public class CommonCraftAssets {
    public static ImageAsset SLOT_L = new ImageAsset(
            new ResourceLocation(MaidStorageManager.MODID, "textures/gui/craft/type/common.png"),
            176, 32, 18, 18
    );
    public static ImageAsset SLOT_M = new ImageAsset(
            new ResourceLocation(MaidStorageManager.MODID, "textures/gui/craft/type/common.png"),
            194, 32, 18, 18
    );
    public static ImageAsset SLOT_R = new ImageAsset(
            new ResourceLocation(MaidStorageManager.MODID, "textures/gui/craft/type/common.png"),
            212, 32, 18, 18
    );

    public static ImageAsset BUTTON_UP = new ImageAsset(
            new ResourceLocation(MaidStorageManager.MODID, "textures/gui/craft/type/common.png"),
            179, 51, 10, 6
    );
    public static ImageAsset BUTTON_REMOVE = new ImageAsset(
            new ResourceLocation(MaidStorageManager.MODID, "textures/gui/craft/type/common.png"),
            179, 60, 10, 10
    );
    public static ImageAsset BUTTON_DOWN = new ImageAsset(
            new ResourceLocation(MaidStorageManager.MODID, "textures/gui/craft/type/common.png"),
            179, 73, 10, 6
    );
    public static ImageAsset BUTTON_UP_HOVER = new ImageAsset(
            new ResourceLocation(MaidStorageManager.MODID, "textures/gui/craft/type/common.png"),
            195, 51, 10, 6
    );
    public static ImageAsset BUTTON_REMOVE_HOVER = new ImageAsset(
            new ResourceLocation(MaidStorageManager.MODID, "textures/gui/craft/type/common.png"),
            195, 60, 10, 10
    );
    public static ImageAsset BUTTON_DOWN_HOVER = new ImageAsset(
            new ResourceLocation(MaidStorageManager.MODID, "textures/gui/craft/type/common.png"),
            195, 73, 10, 6
    );

    public static ImageAsset BUTTON_NBT_POSI = new ImageAsset(
            new ResourceLocation(MaidStorageManager.MODID, "textures/gui/craft/type/common.png"),
            176, 0, 16, 16
    );
    public static ImageAsset BUTTON_NBT_POSI_HOVER = new ImageAsset(
            new ResourceLocation(MaidStorageManager.MODID, "textures/gui/craft/type/common.png"),
            176, 16, 16, 16
    );
    public static ImageAsset BUTTON_NBT_NEGI = new ImageAsset(
            new ResourceLocation(MaidStorageManager.MODID, "textures/gui/craft/type/common.png"),
            192, 0, 16, 16
    );
    public static ImageAsset BUTTON_NBT_NEGI_HOVER = new ImageAsset(
            new ResourceLocation(MaidStorageManager.MODID, "textures/gui/craft/type/common.png"),
            192, 16, 16, 16
    );
    public static ImageAsset BUTTON_OPTIONAL_POSI = new ImageAsset(
            new ResourceLocation(MaidStorageManager.MODID, "textures/gui/craft/type/common.png"),
            208, 0, 16, 16
    );
    public static ImageAsset BUTTON_OPTIONAL_POSI_HOVER = new ImageAsset(
            new ResourceLocation(MaidStorageManager.MODID, "textures/gui/craft/type/common.png"),
            208, 16, 16, 16
    );
    public static ImageAsset BUTTON_OPTIONAL_NEGI = new ImageAsset(
            new ResourceLocation(MaidStorageManager.MODID, "textures/gui/craft/type/common.png"),
            224, 0, 16, 16
    );
    public static ImageAsset BUTTON_OPTIONAL_NEGI_HOVER = new ImageAsset(
            new ResourceLocation(MaidStorageManager.MODID, "textures/gui/craft/type/common.png"),
            224, 16, 16, 16
    );

    public static ImageAsset BUTTON_ACTION = new ImageAsset(
            new ResourceLocation(MaidStorageManager.MODID, "textures/gui/craft/type/common.png"),
            176, 81, 15, 15
    );
    public static ImageAsset BUTTON_ACTION_HOVER = new ImageAsset(
            new ResourceLocation(MaidStorageManager.MODID, "textures/gui/craft/type/common.png"),
            176, 97, 15, 15
    );

    public static ImageAsset BUTTON_PREV_PAGE = new ImageAsset(
            new ResourceLocation(MaidStorageManager.MODID, "textures/gui/craft/type/common.png"),
            176, 112, 16, 8
    );
    public static ImageAsset BUTTON_NEXT_PAGE = new ImageAsset(
            new ResourceLocation(MaidStorageManager.MODID, "textures/gui/craft/type/common.png"),
            198, 112, 16, 8
    );
    public static ImageAsset BUTTON_PREV_PAGE_HOVER = new ImageAsset(
            new ResourceLocation(MaidStorageManager.MODID, "textures/gui/craft/type/common.png"),
            176, 122, 16, 8
    );
    public static ImageAsset BUTTON_NEXT_PAGE_HOVER = new ImageAsset(
            new ResourceLocation(MaidStorageManager.MODID, "textures/gui/craft/type/common.png"),
            198, 122, 16, 8
    );
    public static ImageAsset SEPARATOR = new ImageAsset(
            new ResourceLocation(MaidStorageManager.MODID, "textures/gui/craft/type/common.png"),
            1, 246, 129, 2
    );
    public static ImageAsset SMALL_BUTTON = new ImageAsset(
            new ResourceLocation(MaidStorageManager.MODID, "textures/gui/craft/type/common.png"),
            192, 86, 10, 10
    );
    public static ImageAsset SMALL_BUTTON_HOVER = new ImageAsset(
            new ResourceLocation(MaidStorageManager.MODID, "textures/gui/craft/type/common.png"),
            192, 102, 10, 10
    );

    public static Map<ResourceLocation, ImageAsset> ACTION_IMAGE_MAP = new HashMap<>();

    public static ImageAsset imageForAction(ResourceLocation location) {
        return ACTION_IMAGE_MAP.computeIfAbsent(location, (key) ->
                new ImageAsset(
                        new ResourceLocation(location.getNamespace(), "textures/gui/craft/action/" + location.getPath() + ".png"),
                        0, 0, 11, 11, 11, 11
                )
        );
    }

    public static Component translationForAction(ResourceLocation location) {
        return Component.translatable("craft_guide.action." + location.getNamespace() + "." + location.getPath());
    }
}
