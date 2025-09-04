package studio.fantasyit.maid_storage_manager.menu.craft.common;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.menu.base.ImageAsset;

import java.util.HashMap;
import java.util.Map;

public class CommonCraftAssets {
    public static final ResourceLocation BACKGROUND = new ResourceLocation(MaidStorageManager.MODID, "textures/gui/craft/type/common.png");

    public static final ResourceLocation BACKGROUND_OPT = new ResourceLocation(MaidStorageManager.MODID, "textures/gui/craft/type/common_opt.png");


    public static ImageAsset ROW_HIGHLIGHT = ImageAsset.from4Point(
            BACKGROUND,
            176, 0, 249, 17
    );
    public static ImageAsset ROW = ImageAsset.from4Point(
            BACKGROUND,
            176, 18, 247, 33
    );
    public static ImageAsset ROW_HOVER = ImageAsset.from4Point(
            BACKGROUND,
            176, 34, 247, 49
    );
    public static ImageAsset SLOT_NORMAL = ImageAsset.from4Point(
            BACKGROUND,
            176, 50, 193, 67
    );
    public static ImageAsset SLOT_HAND = ImageAsset.from4Point(
            BACKGROUND,
            176, 86, 193, 103
    );
    public static ImageAsset DELETE_GRAY = ImageAsset.from4Point(
            BACKGROUND,
            219, 50, 221, 52
    );
    public static ImageAsset DELETE = ImageAsset.from4Point(
            BACKGROUND,
            219, 53, 221, 55
    );

    public static ImageAsset BTN_DOWN = ImageAsset.from4Point(
            BACKGROUND,
            209, 60, 216, 64
    );
    public static ImageAsset BTN_UP = ImageAsset.from4Point(
            BACKGROUND,
            209, 65, 216, 69
    );
    public static ImageAsset BTN_ACTION = ImageAsset.from4Point(
            BACKGROUND,
            194, 50, 208, 64
    );
    public static ImageAsset BTN_ACTION_HOVER = ImageAsset.from4Point(
            BACKGROUND,
            194, 65, 208, 79
    );
    public static ImageAsset BTN_OPTION = ImageAsset.from4Point(
            BACKGROUND,
            194, 80, 204, 90
    );
    public static ImageAsset BTN_OPTION_HOVER = ImageAsset.from4Point(
            BACKGROUND,
            194, 91, 204, 101
    );
    public static ImageAsset OPTION_UNDERLINE = ImageAsset.from4Point(
            BACKGROUND,
            205, 80, 225, 80
    );
    public static ImageAsset ARROW_DOWN = ImageAsset.from4Point(
            BACKGROUND,
            209, 70, 213, 75
    );
    public static ImageAsset ARROW_UP = ImageAsset.from4Point(
            BACKGROUND,
            214, 70, 218, 75
    );
    public static ImageAsset SCROLL_BASE = ImageAsset.from4Point(
            BACKGROUND,
            209, 50, 213, 54
    );
    public static ImageAsset SCROLL_DECO = ImageAsset.from4Point(
            BACKGROUND,
            209, 55, 213, 59
    );
    public static ImageAsset SCROLL_BASE_HOVER = ImageAsset.from4Point(
            BACKGROUND,
            214, 50, 218, 54
    );
    public static ImageAsset SCROLL_DECO_HOVER = ImageAsset.from4Point(
            BACKGROUND,
            214, 55, 218, 59
    );

    public static ImageAsset BTN_NO_OCCUPY = new ImageAsset(
            BACKGROUND_OPT,
            0, 0, 15, 15
    );
    public static ImageAsset BTN_NOT_MERGEABLE = new ImageAsset(
            BACKGROUND_OPT,
            15, 0, 15, 15
    );
    public static ImageAsset BTN_OCCUPY = new ImageAsset(
            BACKGROUND_OPT,
            30, 0, 15, 15
    );
    public static ImageAsset BTN_MERGEABLE = new ImageAsset(
            BACKGROUND_OPT,
            45, 0, 15, 15
    );

    public static ImageAsset BTN_NO_OCCUPY_HOVER = new ImageAsset(
            BACKGROUND_OPT,
            0, 15, 15, 15
    );
    public static ImageAsset BTN_NOT_MERGEABLE_HOVER = new ImageAsset(
            BACKGROUND_OPT,
            15, 15, 15, 15
    );
    public static ImageAsset BTN_OCCUPY_HOVER = new ImageAsset(
            BACKGROUND_OPT,
            30, 15, 15, 15
    );
    public static ImageAsset BTN_MERGEABLE_HOVER = new ImageAsset(
            BACKGROUND_OPT,
            45, 15, 15, 15
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
