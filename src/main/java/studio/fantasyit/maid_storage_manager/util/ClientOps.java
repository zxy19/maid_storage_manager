package studio.fantasyit.maid_storage_manager.util;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class ClientOps {
    public static Player getPlayer() {
        return Minecraft.getInstance().player;
    }
    public static Level getLevel() {
        return Minecraft.getInstance().level;
    }
}
