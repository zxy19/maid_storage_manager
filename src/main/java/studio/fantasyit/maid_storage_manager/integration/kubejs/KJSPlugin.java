package studio.fantasyit.maid_storage_manager.integration.kubejs;

import dev.latvian.mods.kubejs.event.EventGroupRegistry;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingRegistry;
import dev.latvian.mods.kubejs.script.TypeWrapperRegistry;
import studio.fantasyit.maid_storage_manager.integration.kubejs.binding.KJSMSMBinding;
import studio.fantasyit.maid_storage_manager.integration.kubejs.binding.KJSMSMCompacted;
import studio.fantasyit.maid_storage_manager.integration.kubejs.binding.KJSMSMTypeCasting;
import studio.fantasyit.maid_storage_manager.integration.kubejs.binding.KJSMSMUtilities;
import studio.fantasyit.maid_storage_manager.integration.kubejs.wrapped.base.BaseSupplierWrapper;
import studio.fantasyit.maid_storage_manager.integration.kubejs.wrapped.base.BaseWrappedWrapper;
import studio.fantasyit.maid_storage_manager.integration.kubejs.wrapped.craft.context.IKJSCraftContext;
import studio.fantasyit.maid_storage_manager.integration.kubejs.wrapped.craft.context.KJSCraftContext;
import studio.fantasyit.maid_storage_manager.integration.kubejs.wrapped.craft.contextSupplier.IKJSCraftContextSupplier;
import studio.fantasyit.maid_storage_manager.integration.kubejs.wrapped.craft.contextSupplier.KJSCraftContextSupplier;
import studio.fantasyit.maid_storage_manager.integration.kubejs.wrapped.craft.generator.IKJSAutoCraftGuideGenerator;
import studio.fantasyit.maid_storage_manager.integration.kubejs.wrapped.craft.generator.KJSAutoCraftGuideGenerator;
import studio.fantasyit.maid_storage_manager.integration.kubejs.wrapped.craft.type.IKJSCraftType;
import studio.fantasyit.maid_storage_manager.integration.kubejs.wrapped.craft.type.KJSCraftType;
import studio.fantasyit.maid_storage_manager.integration.kubejs.wrapped.item.KJSItemPair;
import studio.fantasyit.maid_storage_manager.integration.kubejs.wrapped.item.KJSItemPairWrapper;

public class KJSPlugin implements KubeJSPlugin {
    @Override
    public void registerTypeWrappers(TypeWrapperRegistry typeWrappers) {
        typeWrappers.register(IKJSAutoCraftGuideGenerator.class, new BaseWrappedWrapper<>(KJSAutoCraftGuideGenerator::new));
        typeWrappers.register(IKJSAutoCraftGuideGenerator.Full.class, new BaseWrappedWrapper<>(KJSAutoCraftGuideGenerator::new));
        typeWrappers.register(IKJSCraftType.class, new BaseWrappedWrapper<>(KJSCraftType::new));
        typeWrappers.register(IKJSCraftType.Full.class, new BaseWrappedWrapper<>(KJSCraftType::new));
        typeWrappers.register(IKJSCraftContext.class, new BaseWrappedWrapper<>(KJSCraftContext::new));
        typeWrappers.register(IKJSCraftContextSupplier.class, new BaseSupplierWrapper<>(KJSCraftContextSupplier::new));
        typeWrappers.register(KJSItemPair.class, new KJSItemPairWrapper());
    }

    @Override
    public void registerEvents(EventGroupRegistry registry) {
        registry.register(KJSRegEvent.group);
    }

    @Override
    public void registerBindings(BindingRegistry bindings) {
        KJSMSMBinding kjsmsmBinding = new KJSMSMBinding();
        bindings.add("MaidStorageManagerEnum", kjsmsmBinding);
        bindings.add("MSME", kjsmsmBinding);
        KJSMSMUtilities kjsmsmUtilities = new KJSMSMUtilities();
        bindings.add("MaidStorageManagerUtil", kjsmsmUtilities);
        bindings.add("MSMU", kjsmsmUtilities);
        KJSMSMTypeCasting kjsmsmTypeCasting = new KJSMSMTypeCasting();
        bindings.add("MaidStorageManagerTypeCasting", kjsmsmTypeCasting);
        bindings.add("MSMTC", kjsmsmTypeCasting);
        KJSMSMCompacted kjsmsmCompacted = new KJSMSMCompacted(kjsmsmBinding, kjsmsmUtilities, kjsmsmTypeCasting);
        bindings.add("MaidStorageManager", kjsmsmCompacted);
        bindings.add("MSM", kjsmsmCompacted);
    }
}
