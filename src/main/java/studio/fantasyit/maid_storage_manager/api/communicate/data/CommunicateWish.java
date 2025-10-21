package studio.fantasyit.maid_storage_manager.api.communicate.data;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import studio.fantasyit.maid_storage_manager.api.communicate.wish.IActionWish;

import java.util.List;

public record CommunicateWish(EntityMaid wisher, List<IActionWish> wishes) {
}
