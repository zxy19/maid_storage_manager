package studio.fantasyit.maid_storage_manager.craft;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.Event;
import studio.fantasyit.maid_storage_manager.craft.action.CraftAction;
import studio.fantasyit.maid_storage_manager.craft.generator.IAutoCraftGuideGenerator;
import studio.fantasyit.maid_storage_manager.craft.type.ICraftType;

import java.util.ArrayList;
import java.util.List;

public class CollectCraftEvent extends Event {
    private final List<ICraftType> craftTypes;
    private final List<CraftAction> actions;
    private final ArrayList<IAutoCraftGuideGenerator> autoCraftGuideGenerators;

    public CollectCraftEvent(List<ICraftType> craftTypes, List<CraftAction> actions, ArrayList<IAutoCraftGuideGenerator> autoCraftGuideGenerators) {
        this.craftTypes = craftTypes;
        this.actions = actions;
        this.autoCraftGuideGenerators = autoCraftGuideGenerators;
    }

    public List<ICraftType> getCraftTypes() {
        return craftTypes;
    }

    public void addCraftType(ICraftType craftType) {
        craftTypes.add(craftType);
    }

    public void addAction(ResourceLocation type, CraftAction.CraftActionProvider craftActionProvider, CraftAction.CraftActionPathFindingTargetProvider craftActionPathFindingTargetProvider, double closeEnoughThreshold, boolean isCommon, int hasInput, int hasOutput) {
        this.actions.add(new CraftAction(type, craftActionProvider, craftActionPathFindingTargetProvider, closeEnoughThreshold, isCommon, hasInput, hasOutput));
    }

    public void addAutoCraftGuideGenerator(IAutoCraftGuideGenerator autoCraftGuideGenerator) {
        this.autoCraftGuideGenerators.add(autoCraftGuideGenerator);
    }
}
