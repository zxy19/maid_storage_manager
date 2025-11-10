package studio.fantasyit.maid_storage_manager.items;

import com.github.tartaricacid.touhoulittlemaid.api.bauble.IMaidBauble;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.items.data.ItemStackData;
import studio.fantasyit.maid_storage_manager.menu.logistics.LogisticsGuideMenu;
import studio.fantasyit.maid_storage_manager.registry.DataComponentRegistry;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;
import studio.fantasyit.maid_storage_manager.storage.Target;

import java.util.List;
import java.util.Objects;

public class LogisticsGuide extends MaidInteractItem implements MenuProvider, IMaidBauble {
    public final static String TAG_ITEM = "item";
    public final static String TAG_INPUT = "input";
    public final static String TAG_OUTPUT = "output";
    public final static String TAG_SELECTING = "selecting";
    public final static String TAG_SINGLE_MODE = "single_mode";

    public LogisticsGuide() {
        super(new Properties().stacksTo(1));
    }

    public static Component getTip(ItemStack itemInHand) {
        int selectId = getSelectId(itemInHand);
        if (selectId == 0)
            return Component.translatable("interaction.select_extract");
        else
            return Component.translatable("interaction.select_store");
    }

    public static int getSelectId(ItemStack itemInHand) {
        return itemInHand.getOrDefault(DataComponentRegistry.SELECTING, 0);
    }

    public static void setSelectId(ItemStack itemInHand, int value) {
        itemInHand.set(DataComponentRegistry.SELECTING, value);
    }


    public static void rollMode(ItemStack itemInHand, ServerPlayer serverPlayer, int value) {
        int selectId = getSelectId(itemInHand);
        selectId = selectId == 0 ? 1 : 0;
        setSelectId(itemInHand, selectId);
    }

    public static void rollMode(ItemStack itemInHand, ServerPlayer serverPlayer) {
        rollMode(itemInHand, serverPlayer, 1);
    }


    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, @NotNull Player player, @NotNull InteractionHand p_41434_) {
        if (player.isShiftKeyDown()) return InteractionResultHolder.pass(player.getItemInHand(p_41434_));
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(this, (buffer) -> {
            });
            return InteractionResultHolder.consume(player.getItemInHand(p_41434_));
        }
        return InteractionResultHolder.pass(player.getItemInHand(p_41434_));
    }


    public static CraftGuideData getCraftGuideData(ItemStack itemInHand, HolderLookup.Provider provider) {
        ItemStack craftGuideItemStack = getCraftGuideItemStack(itemInHand, provider);
        if (craftGuideItemStack.isEmpty()) return null;
        return craftGuideItemStack.getOrDefault(DataComponentRegistry.CRAFT_GUIDE_DATA, CraftGuide.empty());
    }

    public static ItemStack getCraftGuideItemStack(ItemStack itemInHand, HolderLookup.Provider provider) {
        ItemStack item = getItemStack(itemInHand, provider);
        return item.is(ItemRegistry.CRAFT_GUIDE.get()) ? item : ItemStack.EMPTY;
    }

    public static ItemStack getFilterItemStack(ItemStack itemInHand, HolderLookup.Provider provider) {
        ItemStack item = getItemStack(itemInHand, provider);
        return item.is(ItemRegistry.FILTER_LIST.get()) ? item : ItemStack.EMPTY;
    }

    public static ItemStack getItemStack(ItemStack itemInHand, HolderLookup.Provider provider) {
        return itemInHand.getOrDefault(DataComponentRegistry.CONTAIN_ITEM, ItemStackData.EMPTY).itemStack(provider);
    }

    public static @Nullable Target getInput(ItemStack itemInHand) {
        return itemInHand.get(DataComponentRegistry.LOGISTICS_INPUT);
    }

    public static @Nullable Target getOutput(ItemStack itemInHand) {
        return itemInHand.get(DataComponentRegistry.LOGISTICS_OUTPUT);
    }

    public static int getWorkCount(ItemStack itemInHand) {
        return itemInHand.getOrDefault(DataComponentRegistry.LOGISTICS_SINGLE, false) ? 1 : 64;
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
        if (!context.getLevel().isClientSide && context.getPlayer() instanceof ServerPlayer serverPlayer) {
            if (!serverPlayer.isShiftKeyDown()) return InteractionResult.PASS;
            DeferredHolder<DataComponentType<?>, DataComponentType<Target>> selecting = getSelectId(context.getItemInHand()) == 0 ? DataComponentRegistry.LOGISTICS_INPUT : DataComponentRegistry.LOGISTICS_OUTPUT;
            BlockPos clickedPos = context.getClickedPos();
            Target validTarget = MaidStorage.getInstance().isValidTarget((ServerLevel) context.getLevel(), serverPlayer, clickedPos);
            if (validTarget != null) {
                ItemStack item = serverPlayer.getMainHandItem();
                if (item.has(selecting)) {
                    Target storage = item.get(selecting);
                    if (storage.getPos().equals(clickedPos) && storage.getSide().isPresent() && storage.getSide().get() == context.getClickedFace()) {
                        item.remove(selecting);
                        serverPlayer.sendSystemMessage(Component.translatable("interaction.clear_storage"));
                    } else {
                        if (storage.pos.equals(clickedPos)) {
                            storage = storage.sameType(clickedPos, context.getClickedFace());
                        } else {
                            storage = storage.sameType(clickedPos, null);
                        }
                        serverPlayer.sendSystemMessage(Component.translatable("interaction.bind_storage", clickedPos.getX(), clickedPos.getY(), clickedPos.getZ()));
                        item.set(selecting, storage);
                    }
                } else {
                    item.set(selecting, validTarget);
                    serverPlayer.sendSystemMessage(Component.translatable("interaction.bind_storage", clickedPos.getX(), clickedPos.getY(), clickedPos.getZ()));
                }
            }
            return InteractionResult.CONSUME;
        } else {
            if (Objects.requireNonNull(context.getPlayer()).isShiftKeyDown()) return InteractionResult.CONSUME;
            return InteractionResult.PASS;
        }
    }


    @Override
    public Component getDisplayName() {
        return Component.literal("");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int p_39954_, Inventory p_39955_, Player p_39956_) {
        return new LogisticsGuideMenu(p_39954_, p_39956_);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext p_339594_, List<Component> toolTip, TooltipFlag p_41424_) {
        super.appendHoverText(itemStack, p_339594_, toolTip, p_41424_);
        toolTip.add(Component.translatable("tooltip.maid_storage_manager.logistics_guide.desc").withStyle(ChatFormatting.GRAY));
        @Nullable Target input = getInput(itemStack);
        if (input != null) {
            toolTip.add(Component.translatable("tooltip.maid_storage_manager.logistics_guide.input",
                    input.pos.getX(),
                    input.pos.getY(),
                    input.pos.getZ()
            ));
        }

        @Nullable Target output = getOutput(itemStack);
        if (output != null) {
            toolTip.add(Component.translatable("tooltip.maid_storage_manager.logistics_guide.output",
                    output.pos.getX(),
                    output.pos.getY(),
                    output.pos.getZ()
            ));
        }

        ItemStack itemStack1 = getItemStack(itemStack, p_339594_.registries());
        if (itemStack1.is(ItemRegistry.CRAFT_GUIDE.get())) {
            toolTip.add(Component.translatable("tooltip.maid_storage_manager.logistics_guide.craft_guide").withStyle(ChatFormatting.YELLOW));
        } else if (itemStack1.is(ItemRegistry.FILTER_LIST.get())) {
            toolTip.add(Component.translatable("tooltip.maid_storage_manager.logistics_guide.filter").withStyle(ChatFormatting.YELLOW));
        }
    }
}
