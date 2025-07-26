package studio.fantasyit.maid_storage_manager.integration.create;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessingType;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessingTypeRegistry;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.jetbrains.annotations.UnmodifiableView;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.integration.Integrations;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class CreateIntegration {
    public static void init() {
        if (Integrations.createStockManager())
            NeoForge.EVENT_BUS.addListener(AddCreateStockButtonForMaid::addStockButton);
    }

    public enum AddressType {
        REQUEST,
        PLACED
    }

    public static String getAddress(EntityMaid maid, AddressType type) {
        String pattern = Config.createAddress;
        String uuidStr = maid.getUUID().toString();

        return pattern
                .replace("<UUID>", uuidStr)
                .replace("<UUID4>", uuidStr.substring(uuidStr.length() - 4))
                .replace("<UUID8>", uuidStr.substring(uuidStr.length() - 8))
                .replace("<TYPE>", type.toString().toLowerCase())
                .replace("<TYPE1>", type.toString().substring(0, 1));
    }

    public static @UnmodifiableView List<FanProcessingType> getFanProcessingTypes() {
        if (ModList.get().getMods().stream().anyMatch(modInfo -> modInfo.getModId().equals("create")
                && modInfo.getVersion().compareTo(new DefaultArtifactVersion("6.0.0")) >= 0))
            return FanProcessingTypeRegistry.SORTED_TYPES_VIEW;
        try {
            Method getSortedTypesView = FanProcessingTypeRegistry.class.getMethod("getSortedTypesView", (Class<?>[]) null);
            if (getSortedTypesView != null) {
                return (List<FanProcessingType>) getSortedTypesView.invoke(null, (Object[]) null);
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            return List.of();
        }
        return List.of();
    }
}