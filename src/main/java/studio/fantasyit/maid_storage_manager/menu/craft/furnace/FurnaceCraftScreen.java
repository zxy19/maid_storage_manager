package studio.fantasyit.maid_storage_manager.menu.craft.furnace;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.anti_ad.mc.ipn.api.IPNIgnore;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.menu.craft.base.AbstractCraftScreen;
import yalter.mousetweaks.api.MouseTweaksDisableWheelTweak;

@MouseTweaksDisableWheelTweak
@IPNIgnore
public class FurnaceCraftScreen extends AbstractCraftScreen<FurnaceCraftMenu> {
    private static final ResourceLocation background = ResourceLocation.fromNamespaceAndPath(MaidStorageManager.MODID, "textures/gui/craft/type/furnace.png");

    public FurnaceCraftScreen(FurnaceCraftMenu p_97741_, Inventory p_97742_, Component p_97743_) {
        super(p_97741_, p_97742_, p_97743_,background,true);
    }
}