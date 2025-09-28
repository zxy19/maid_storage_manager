package studio.fantasyit.maid_storage_manager.mixin;

import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.chatbubble.EntityGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import studio.fantasyit.maid_storage_manager.api.mixin.IEntityGraphicsBufferSourceGetter;

@Mixin(EntityGraphics.class)
public abstract class EntityGraphicsBufferSourceGetter implements IEntityGraphicsBufferSourceGetter {

    @Accessor(value = "bufferSource", remap = false)
    public abstract MultiBufferSource getBufferSource();
}
