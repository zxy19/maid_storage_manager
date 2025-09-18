package studio.fantasyit.tour_guide.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import studio.fantasyit.tour_guide.client.event.ClientInputEvent;

public class ClientMessages {
    public static void sendOp(boolean allowSkip) {

        MutableComponent t = Component.translatable("message.tour_guide.next_step_tip", ClientInputEvent.KEY_CHECK_STEP.get().getKey().getDisplayName()).withStyle(ChatFormatting.GREEN);
        if (allowSkip)
            t = t.append(Component.translatable("message.tour_guide.skip_step_tip", ClientInputEvent.KEY_SKIP.get().getKey().getDisplayName()).withStyle(ChatFormatting.GREEN));
        Minecraft.getInstance().player.sendSystemMessage(t);
    }
}
