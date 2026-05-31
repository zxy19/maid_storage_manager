package studio.fantasyit.maid_storage_manager.api.communicate.wish;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.api.communicate.data.CommunicateWish;
import studio.fantasyit.maid_storage_manager.api.communicate.step.IActionStep;

import java.util.List;

public interface IActionWish {
    Identifier getType();

    @Nullable List<IActionStep> getSteps(EntityMaid handler, CommunicateWish wish);
}
