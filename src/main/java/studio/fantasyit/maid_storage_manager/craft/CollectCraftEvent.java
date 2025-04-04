package studio.fantasyit.maid_storage_manager.craft;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.Event;
import studio.fantasyit.maid_storage_manager.craft.type.ICraftType;

import java.util.List;

public class CollectCraftEvent extends Event {
    private final List<ICraftType> craftTypes;
    private final List<CraftManager.CraftAction> actions;

    public CollectCraftEvent(List<ICraftType> craftTypes, List<CraftManager.CraftAction> actions) {
        this.craftTypes = craftTypes;
        this.actions = actions;
    }

    public List<ICraftType> getCraftTypes() {
        return craftTypes;
    }

    public void addCraftType(ICraftType craftType) {
        craftTypes.add(craftType);
    }

    public void addAction(ResourceLocation type, CraftManager.CraftActionProvider craftActionProvider, boolean isCommon) {
        this.actions.add(new CraftManager.CraftAction(type, craftActionProvider, isCommon));
    }
}
