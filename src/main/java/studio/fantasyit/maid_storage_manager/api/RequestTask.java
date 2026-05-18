package studio.fantasyit.maid_storage_manager.api;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public record RequestTask(
        ItemStack item,
        int requested,
        int collected,
        int stored,
        boolean done,
        List<ItemStack> missing,
        String failAddition
) {}
