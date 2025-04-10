package studio.fantasyit.maid_storage_manager.items;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.craft.CraftManager;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.type.CommonType;
import studio.fantasyit.maid_storage_manager.craft.type.ICraftType;
import studio.fantasyit.maid_storage_manager.items.render.CustomItemRenderer;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;
import studio.fantasyit.maid_storage_manager.storage.Target;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class CraftGuide extends Item implements MenuProvider {
    public static final String TAG_RESULT = "result";
    public static final String TAG_SELECTING = "selecting";
    public static final String TAG_STEPS = "steps";
    public static final String TAG_OP_STORAGE = "storage";
    public static final String TAG_OP_ACTION = "action";
    public static final String TAG_ITEMS_ITEM = "item";
    public static final String TAG_ITEMS_COUNT = "requested";
    public static final String TAG_OP_MATCH_TAG = "match_tag";
    public static final String TAG_TYPE = "type";
    public static final String TAG_OP_OPTIONAL = "optional";
    public static final String TAG_OP_INPUT = "input";
    public static final String TAG_OP_OUTPUT = "output";


    public CraftGuide() {
        super(
                new Properties()
                        .stacksTo(1)
        );
    }

    public static boolean matchNbt(ItemStack mainHandItem, String targ) {
        if (!mainHandItem.is(ItemRegistry.CRAFT_GUIDE.get()))
            return false;
        if (!mainHandItem.hasTag())
            return true;
        CompoundTag tag = Objects.requireNonNull(mainHandItem.getTag());
        if (!tag.contains(targ))
            return true;
        if (!tag.getCompound(targ).contains(TAG_OP_MATCH_TAG))
            return true;
        return tag.getCompound(targ).getBoolean(TAG_OP_MATCH_TAG);
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


    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, @NotNull Player player, @NotNull InteractionHand p_41434_) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            if (serverPlayer.isShiftKeyDown()) {
                ItemStack itemInHand = player.getItemInHand(p_41434_);
                rollMode(itemInHand, serverPlayer);
            } else
                NetworkHooks.openScreen(serverPlayer, this, (buffer) -> {
                });
            return InteractionResultHolder.consume(player.getItemInHand(p_41434_));
        } else {
            return InteractionResultHolder.pass(player.getItemInHand(p_41434_));
        }
    }

    public static void rollMode(ItemStack itemInHand, ServerPlayer serverPlayer, int value) {
        CraftGuideData craftGuideData = CraftGuideData.fromItemStack(itemInHand);
        int selectId = getSelectId(itemInHand);
        selectId = (selectId + 1) % (craftGuideData.getSteps().size() + 1);
        setSelectId(itemInHand, selectId);
        if (selectId == craftGuideData.getSteps().size())
            serverPlayer.sendSystemMessage(Component.translatable("interaction.select_step_new"));
        else
            serverPlayer.sendSystemMessage(Component.translatable("interaction.select_step_index", selectId + 1));
    }

    public static void rollMode(ItemStack itemInHand, ServerPlayer serverPlayer) {
        rollMode(itemInHand, serverPlayer, 1);
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
        if (!context.getLevel().isClientSide && context.getPlayer() instanceof ServerPlayer serverPlayer) {
            if (!serverPlayer.isShiftKeyDown())
                return InteractionResult.PASS;
            ItemStack itemInHand = context.getItemInHand();
            CraftGuideData craftGuideData = CraftGuideData.fromItemStack(itemInHand);
            int selecting = craftGuideData.selecting;

            ResourceLocation specialType = CommonType.TYPE;
            if (craftGuideData.selecting == 0 && craftGuideData.getSteps().size() <= 1) {
                if (craftGuideData.getType().equals(CommonType.TYPE))
                    specialType = CraftManager.getInstance().getTargetType((ServerLevel) context.getLevel(),
                            context.getClickedPos(),
                            context.getClickedFace());
            }
            Target target = MaidStorage.getInstance().isValidTarget((ServerLevel) context.getLevel(), context.getPlayer(), context.getClickedPos(), context.getClickedFace());
            if (target == null) {
                target = Target.virtual(context.getClickedPos(), context.getClickedFace());
            }
            if (specialType == CommonType.TYPE) {
                if (craftGuideData.getSteps().size() == selecting)
                    craftGuideData.getSteps().add(new CraftGuideStepData(target.withoutSide(),
                            List.of(),
                            List.of(),
                            CraftManager.getInstance().getDefaultAction().type(),
                            false,
                            false));
                else {
                    CraftGuideStepData craftGuideStepData = craftGuideData.getSteps().get(selecting);
                    Target existingTarget = craftGuideStepData.getStorage();
                    if (existingTarget.equals(target)) {
                        craftGuideData.getSteps().remove(selecting);
                    } else if (existingTarget.equals(target.withoutSide())) {
                        craftGuideStepData.storage = target;
                    } else {
                        craftGuideStepData.storage = target.withoutSide();
                    }
                }
            } else {
                List<CraftGuideStepData> steps = craftGuideData.getSteps();
                steps.clear();
                steps.add(new CraftGuideStepData(
                                target.withoutSide(),
                                List.of(),
                                List.of(),
                                CraftGuideStepData.SPECIAL_ACTION,
                                false,
                                false
                        )
                );
                craftGuideData.type = specialType;
            }
            craftGuideData.saveToItemStack(itemInHand);
            return InteractionResult.CONSUME;
        } else {
            if (Objects.requireNonNull(context.getPlayer()).isShiftKeyDown())
                return InteractionResult.CONSUME;
            return InteractionResult.PASS;
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack itemStack,
                                @Nullable Level p_41422_,
                                @NotNull List<Component> toolTip,
                                @NotNull TooltipFlag p_41424_) {
        super.appendHoverText(itemStack, p_41422_, toolTip, p_41424_);

        CraftGuideData craftGuideData = CraftGuideData.fromItemStack(itemStack);
        if (craftGuideData.getType() == null) return;
        ICraftType type = CraftManager.getInstance().getType(craftGuideData.getType());
        if (type == null) return;
        type.getTooltip(craftGuideData, toolTip);
    }

    protected void addTooltip(CraftGuideStepData data, List<Component> toolTip) {
        toolTip.add(Component.translatable("tooltip.maid_storage_manager.craft_guide.storage",
                data.getStorage().getPos().getX(),
                data.getStorage().getPos().getY(),
                data.getStorage().getPos().getZ()
        ));
        for (int i = 0; i < data.getItems().size(); i++) {
            if (data.getItems().get(i).isEmpty()) continue;
            toolTip.add(Component.translatable("tooltip.maid_storage_manager.craft_guide.item",
                    data.getItems().get(i).getHoverName().getString(),
                    data.getItems().get(i).getCount()
            ));
        }
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.literal("");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int p_39954_, Inventory p_39955_, Player p_39956_) {
        CraftGuideData craftGuideData = CraftGuideData.fromItemStack(p_39956_.getItemInHand(InteractionHand.MAIN_HAND));
        if (craftGuideData.getType() == null) return null;
        ICraftType type = CraftManager.getInstance().getType(craftGuideData.getType());
        if (type == null) return null;
        return type.createGui(p_39954_,p_39956_.level(), p_39956_, craftGuideData);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return CustomItemRenderer.getInstance();
            }
        });
    }
}
