package studio.fantasyit.maid_storage_manager.event;

import com.github.tartaricacid.touhoulittlemaid.api.event.InteractMaidEvent;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.integration.Integrations;
import studio.fantasyit.maid_storage_manager.integration.create.StockManagerInteract;
import studio.fantasyit.maid_storage_manager.items.HangUpItem;
import studio.fantasyit.maid_storage_manager.menu.FilterMenu;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.registry.MemoryModuleRegistry;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;
import studio.fantasyit.maid_storage_manager.storage.Target;

import java.util.List;

@Mod.EventBusSubscriber(modid = MaidStorageManager.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerInteract {
    public static void onPlayerInteract(ServerLevel level, ServerPlayer player, BlockPos pos, Direction clickedFace) {
        @Nullable Target validStorage = MaidStorage.getInstance().isValidTarget(level, player, pos, clickedFace);
        if (validStorage == null) return;
        List<Entity> maidList = level.getEntities(player, player.getBoundingBox()
                .inflate(10, 10, 10), entity -> entity instanceof EntityMaid && ((EntityMaid) entity).isOwnedBy(player));
        for (Entity eMaid : maidList) {
            EntityMaid maid = (EntityMaid) eMaid;
            maid.getBrain().setMemory(
                    MemoryModuleRegistry.CO_WORK_TARGET_STORAGE.get(),
                    validStorage
            );
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerInteractMaid(InteractMaidEvent event) {
        EntityMaid maid = event.getMaid();
        Player player = event.getPlayer();
        if (Integrations.createStockManager())
            if (StockManagerInteract.onPlayerInteract(player, maid)) {
                event.setCanceled(true);
                return;
            }
        if (player instanceof ServerPlayer sp) {
            if (sp.getMainHandItem().is(Items.EXPERIENCE_BOTTLE)) {
                ItemStack mainHandItem = sp.getMainHandItem();
                int count = sp.isShiftKeyDown() ? mainHandItem.getCount() : 1;
                int amount = (3 + sp.level().random.nextInt(5) + sp.level().random.nextInt(5)) * count;
                maid.setExperience(maid.getExperience() + amount);
                mainHandItem.shrink(count);
                event.setCanceled(true);
            }
        }

    }

    @SubscribeEvent
    public static void onPlayerInteractRc(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide || event.getFace() == null) return;
        onPlayerInteract(
                (ServerLevel) event.getLevel(),
                (ServerPlayer) event.getEntity(),
                event.getPos(),
                event.getFace()
        );
    }

    @SubscribeEvent
    public static void onPlayerInteractLc(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getLevel().isClientSide || event.getFace() == null) return;
        onPlayerInteract(
                (ServerLevel) event.getLevel(),
                (ServerPlayer) event.getEntity(),
                event.getPos(),
                event.getFace()
        );
    }

    @SubscribeEvent
    public static void onPlayerInteractEntity(PlayerInteractEvent.EntityInteract event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            if (event.getTarget() instanceof ItemFrame ifr) {
                if (ifr.getItem().is(ItemRegistry.FILTER_LIST)) {
                    if (event.getEntity().isShiftKeyDown()) {
                        sp.openMenu(new MenuProvider() {
                            @Override
                            public Component getDisplayName() {
                                return Component.empty();
                            }

                            @Override
                            public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
                                return new FilterMenu(i, player, ifr.getId());
                            }
                        }, (buffer) -> {
                            buffer.writeInt(ifr.getId());
                        });
                    }

                    event.setCanceled(true);
                } else if (ifr.getItem().getItem() instanceof HangUpItem hi && hi.allowClickThrough()) {
                    event.setCanceled(true);
                }
            }
        }
    }
}
