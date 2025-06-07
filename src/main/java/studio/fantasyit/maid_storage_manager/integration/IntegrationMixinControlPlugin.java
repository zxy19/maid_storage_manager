package studio.fantasyit.maid_storage_manager.integration;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class IntegrationMixinControlPlugin implements IMixinConfigPlugin {
    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    protected String mixinClass(String mixinClassName) {
        return "studio.fantasyit.maid_storage_manager.mixin." + mixinClassName;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClass("JeiGuiIconToggleButtonAccessor").equals(mixinClassName)) {
            return Integrations.JEIIngredientRequestLoading();
        } else if (mixinClass("JEIRecipeTransferHook").equals(mixinClassName)) {
            return Integrations.JEIIngredientRequestLoading();
        } else if (mixinClass("EMIRecipeTransferHook").equals(mixinClassName)) {
            return Integrations.EMIngredientRequestLoading();
        } else if (mixinClass("CreateStockKeeperScreenMixin").equals(mixinClassName)) {
            return Integrations.createLoading();
        } else if (mixinClass("CreateStockTickerBEMixin").equals(mixinClassName)) {
            return Integrations.createLoading();
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
