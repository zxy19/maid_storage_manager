package studio.fantasyit.maid_storage_manager.craft.debug;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class CraftingDebugManager {
    public static Map<UUID, CraftingDebugContext> debugContexts = new HashMap<>();

    public static CraftingDebugContext prepareForPlayer(UUID uuid, String control) {
        CraftingDebugContext context = new CraftingDebugContext(false);
        if (!control.isBlank()) {
            String[] split = control.split(",");
            for (String s : split) {
                char ctr = s.charAt(0);
                if (ctr == '+')
                    context.enable(CraftingDebugContext.TYPE.valueOf(s.substring(1)));
                else if (ctr == '-')
                    context.disable(CraftingDebugContext.TYPE.valueOf(s.substring(1)));
            }
        }
        debugContexts.put(uuid, context);
        return context;
    }

    public static Optional<CraftingDebugContext> getDebugContext(UUID uuid) {
        return Optional.ofNullable(debugContexts.get(uuid));
    }

}
