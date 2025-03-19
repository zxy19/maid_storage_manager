package studio.fantasyit.maid_storage_manager.mixin;

import com.github.tartaricacid.touhoulittlemaid.util.ParseI18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Mixin(ParseI18n.class)
public abstract class ParseI18nMixin {
    @Shadow(remap = false)
    @Final
    private static String I18N_END_CHAR;

    @Shadow(remap = false)
    @Final
    private static String I18N_START_CHAR;

    @Shadow
    public static List<Component> parse(List<String> strIn) {
        return null;
    }

    @Inject(method = "parse(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;", at = @At("HEAD"), cancellable = true, remap = false)
    private static void maid_storage_manager$parse(String strIn, CallbackInfoReturnable<MutableComponent> cir) {
        if (strIn.startsWith(I18N_START_CHAR) && strIn.endsWith(I18N_END_CHAR)) {
            String s1 = strIn.substring(1, strIn.length() - 1);
            String[] split = s1.split(",");
            List<String> list = Arrays.stream(split)
                    .skip(1)
                    .toList();
            List<Component> params = parse(list);
            cir.setReturnValue(Component.translatable(split[0], params.toArray()));
        }
    }
}
