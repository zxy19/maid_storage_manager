package studio.fantasyit.maid_storage_manager.integration.jade;

import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import studio.fantasyit.maid_storage_manager.entity.VirtualDisplayEntity;

@WailaPlugin
public class JadePlugin implements IWailaPlugin {
    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerEntityComponent(new VirtualFrameDataProvider(), VirtualDisplayEntity.class);
    }
}
