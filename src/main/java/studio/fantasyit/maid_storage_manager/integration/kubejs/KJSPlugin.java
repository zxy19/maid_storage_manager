package studio.fantasyit.maid_storage_manager.integration.kubejs;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.rhino.util.wrap.TypeWrappers;
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

public class KJSPlugin extends KubeJSPlugin {
    @Override
    public void registerTypeWrappers(ScriptType type, TypeWrappers typeWrappers) {
        typeWrappers.register(IKJSAutoCraftGuideGenerator.class, new BaseWrappedWrapper<>(KJSAutoCraftGuideGenerator::new));
        typeWrappers.register(IKJSAutoCraftGuideGenerator.Full.class, new BaseWrappedWrapper<>(KJSAutoCraftGuideGenerator::new));
        typeWrappers.register(IKJSCraftType.class, new BaseWrappedWrapper<>(KJSCraftType::new));
        typeWrappers.register(IKJSCraftType.Full.class, new BaseWrappedWrapper<>(KJSCraftType::new));
        typeWrappers.register(IKJSCraftContext.class, new BaseWrappedWrapper<>(KJSCraftContext::new));
        typeWrappers.register(IKJSCraftContextSupplier.class, new BaseSupplierWrapper<>(KJSCraftContextSupplier::new));
        typeWrappers.register(KJSItemPair.class, new KJSItemPairWrapper());
    }

    @Override
    public void registerEvents() {
        KJSRegEvent.group.register();
    }

    @Override
    public void registerBindings(BindingsEvent event) {
        KJSMSMBinding kjsmsmBinding = new KJSMSMBinding();
        event.add("MaidStorageManagerEnum", kjsmsmBinding);
        event.add("MSME", kjsmsmBinding);
        KJSMSMUtilities kjsmsmUtilities = new KJSMSMUtilities();
        event.add("MaidStorageManagerUtil", kjsmsmUtilities);
        event.add("MSMU", kjsmsmUtilities);
        KJSMSMTypeCasting kjsmsmTypeCasting = new KJSMSMTypeCasting();
        event.add("MaidStorageManagerTypeCasting", kjsmsmTypeCasting);
        event.add("MSMTC", kjsmsmTypeCasting);
        KJSMSMCompacted kjsmsmCompacted = new KJSMSMCompacted(kjsmsmBinding, kjsmsmUtilities, kjsmsmTypeCasting);
        event.add("MaidStorageManager", kjsmsmCompacted);
        event.add("MSM", kjsmsmCompacted);
    }
}
