package studio.fantasyit.maid_storage_manager.items;

import com.github.tartaricacid.touhoulittlemaid.api.bauble.IMaidBauble;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideRenderData;
import studio.fantasyit.maid_storage_manager.menu.LogisticsGuideMenu;
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

    public static int getSelectId(ItemStack itemInHand) {
        if (!itemInHand.hasTag())
            return 0;
        CompoundTag tag = Objects.requireNonNull(itemInHand.getTag());
        if (!tag.contains(TAG_SELECTING))
            return 0;
        return tag.getInt(TAG_SELECTING);
    }

    public static void setSelectId(ItemStack itemInHand, int value) {
        CompoundTag tag = itemInHand.getOrCreateTag();
        tag.putInt(TAG_SELECTING, value);
        itemInHand.setTag(tag);
    }


    public static void rollMode(ItemStack itemInHand, ServerPlayer serverPlayer, int value) {
        int selectId = getSelectId(itemInHand);
        selectId = selectId == 0 ? 1 : 0;
        setSelectId(itemInHand, selectId);
        if (selectId == 0)
            serverPlayer.sendSystemMessage(Component.translatable("interaction.select_extract"));
        else
            serverPlayer.sendSystemMessage(Component.translatable("interaction.select_store"));
        CraftGuideRenderData.recalculateItemStack(itemInHand);
    }

    public static void rollMode(ItemStack itemInHand, ServerPlayer serverPlayer) {
        rollMode(itemInHand, serverPlayer, 1);
    }


    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, @NotNull Player player, @NotNull InteractionHand p_41434_) {
        if (player.isShiftKeyDown()) return InteractionResultHolder.pass(player.getItemInHand(p_41434_));
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, this, (buffer) -> {
            });
            return InteractionResultHolder.consume(player.getItemInHand(p_41434_));
        }
        return InteractionResultHolder.pass(player.getItemInHand(p_41434_));
    }


    public static CraftGuideData getCraftGuideData(ItemStack itemInHand) {
        ItemStack craftGuideItemStack = getCraftGuideItemStack(itemInHand);
        if (craftGuideItemStack.isEmpty()) return null;
        return CraftGuideData.fromItemStack(craftGuideItemStack);
    }

    public static ItemStack getCraftGuideItemStack(ItemStack itemInHand) {
        ItemStack item = getItemStack(itemInHand);
        return item.is(ItemRegistry.CRAFT_GUIDE.get()) ? item : ItemStack.EMPTY;
    }

    public static ItemStack getFilterItemStack(ItemStack itemInHand) {
        ItemStack item = getItemStack(itemInHand);
        return item.is(ItemRegistry.FILTER_LIST.get()) ? item : ItemStack.EMPTY;
    }

    public static ItemStack getItemStack(ItemStack itemInHand) {
        if (!itemInHand.hasTag())
            return ItemStack.EMPTY;
        CompoundTag tag = Objects.requireNonNull(itemInHand.getTag());
        if (!tag.contains(TAG_ITEM))
            return ItemStack.EMPTY;
        return ItemStack.of(tag.getCompound(TAG_ITEM));
    }

    public static @Nullable Target getInput(ItemStack itemInHand) {
        if (!itemInHand.hasTag())
            return null;
        CompoundTag tag = Objects.requireNonNull(itemInHand.getTag());
        if (!tag.contains(TAG_INPUT))
            return null;
        return Target.fromNbt(tag.getCompound(TAG_INPUT));
    }

    public static @Nullable Target getOutput(ItemStack itemInHand) {
        if (!itemInHand.hasTag())
            return null;
        CompoundTag tag = Objects.requireNonNull(itemInHand.getTag());
        if (!tag.contains(TAG_OUTPUT))
            return null;
        return Target.fromNbt(tag.getCompound(TAG_OUTPUT));
    }

    public static int getWorkCount(ItemStack itemInHand) {
        if (!itemInHand.hasTag())
            return 0;
        CompoundTag tag = Objects.requireNonNull(itemInHand.getTag());
        return tag.getBoolean(TAG_SINGLE_MODE) ? 1 : 64;
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
        if (!context.getLevel().isClientSide && context.getPlayer() instanceof ServerPlayer serverPlayer) {
            if (!serverPlayer.isShiftKeyDown()) return InteractionResult.PASS;
            String selectingTag = getSelectId(context.getItemInHand()) == 0 ? TAG_INPUT : TAG_OUTPUT;
            BlockPos clickedPos = context.getClickedPos();
            Target validTarget = MaidStorage.getInstance().isValidTarget((ServerLevel) context.getLevel(), serverPlayer, clickedPos);
            if (validTarget != null) {
                ItemStack item = serverPlayer.getMainHandItem();
                CompoundTag tag = item.getOrCreateTag();
                if (tag.contains(selectingTag)) {
                    Target storage = Target.fromNbt(tag.getCompound(selectingTag));
                    if (storage.getPos().equals(clickedPos) && storage.getSide().isPresent() && storage.getSide().get() == context.getClickedFace()) {
                        tag.remove(selectingTag);
                        serverPlayer.sendSystemMessage(Component.translatable("interaction.clear_storage"));
                    } else {
                        if (storage.pos.equals(clickedPos)) {
                            storage.side = context.getClickedFace();
                        } else {
                            storage.pos = clickedPos;
                            storage.side = null;
                        }
                        serverPlayer.sendSystemMessage(Component.translatable("interaction.bind_storage", clickedPos.getX(), clickedPos.getY(), clickedPos.getZ()));
                        tag.put(selectingTag, storage.toNbt());
                    }
                } else {
                    tag.put(selectingTag, validTarget.toNbt());
                    serverPlayer.sendSystemMessage(Component.translatable("interaction.bind_storage", clickedPos.getX(), clickedPos.getY(), clickedPos.getZ()));
                }
                item.setTag(tag);
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
    public void appendHoverText(@NotNull ItemStack itemStack,
                                @Nullable Level p_41422_,
                                @NotNull List<Component> toolTip,
                                @NotNull TooltipFlag p_41424_) {
        super.appendHoverText(itemStack, p_41422_, toolTip, p_41424_);
        toolTip.add(Component.translatable("tooltip.maid_storage_manager.logistics_guide.desc").withStyle(ChatFormatting.GRAY));
        if (!itemStack.hasTag()) {
            return;
        }
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

        ItemStack itemStack1 = getItemStack(itemStack);
        if (itemStack1.is(ItemRegistry.CRAFT_GUIDE.get())) {
            toolTip.add(Component.translatable("tooltip.maid_storage_manager.logistics_guide.craft_guide").withStyle(ChatFormatting.YELLOW));
        } else if (itemStack1.is(ItemRegistry.FILTER_LIST.get())) {
            toolTip.add(Component.translatable("tooltip.maid_storage_manager.logistics_guide.filter").withStyle(ChatFormatting.YELLOW));
        }
    }
}
