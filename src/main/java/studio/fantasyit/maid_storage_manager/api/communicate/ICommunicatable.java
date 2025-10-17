package studio.fantasyit.maid_storage_manager.api.communicate;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.api.communicate.context.ICommunicateContext;

public interface ICommunicatable {
    boolean willingCommunicate(EntityMaid wisher, @Nullable ItemStack wisherItemStack, EntityMaid handler);

    @Nullable ICommunicateContext startCommunicate(EntityMaid wisher, @Nullable ItemStack wisherItemStack, EntityMaid handler);
}
