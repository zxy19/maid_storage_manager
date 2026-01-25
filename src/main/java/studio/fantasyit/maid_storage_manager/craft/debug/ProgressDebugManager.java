package studio.fantasyit.maid_storage_manager.craft.debug;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class ProgressDebugManager {
    public static Map<UUID, ProgressDebugContext> debugContexts = new HashMap<>();
    public static Map<UUID, String> preparedPlayer = new HashMap<>();

    public static ProgressDebugContext createForMaid(EntityMaid maid, String control) {
        ProgressDebugContext context = new ProgressDebugContext(false);
        if (!control.isBlank()) {
            String[] split = control.split(",");
            for (String s : split) {
                char ctr = s.charAt(0);
                if (ctr == '+')
                    context.enable(ProgressDebugContext.TYPE.valueOf(s.substring(1)));
                else if (ctr == '-')
                    context.disable(ProgressDebugContext.TYPE.valueOf(s.substring(1)));
            }
        }
        debugContexts.put(maid.getUUID(), context);
        return context;
    }

    public static void preparePlayer(Player player, String path) {
        preparedPlayer.put(player.getUUID(), path);
    }

    public static @Nullable String getPreparedPlayer(Player player) {
        return preparedPlayer.get(player.getUUID());
    }

    public static void removePreparedPlayer(Player player) {
        preparedPlayer.remove(player.getUUID());
    }


    public static void remove(EntityMaid maid) {
        getDebugContext(maid).ifPresent(ProgressDebugContext::stop);
        debugContexts.remove(maid.getUUID());
    }

    public static Optional<ProgressDebugContext> getDebugContext(EntityMaid maid) {
        return Optional.ofNullable(debugContexts.get(maid.getUUID()));
    }

    public static ProgressDebugContext getDebugContextOrDummy(EntityMaid maid) {
        return getDebugContext(maid).orElse(ProgressDebugContext.Dummy.INSTANCE);
    }
}
