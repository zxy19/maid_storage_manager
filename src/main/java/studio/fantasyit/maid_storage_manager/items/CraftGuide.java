package studio.fantasyit.maid_storage_manager.items;

import net.minecraft.ChatFormatting;
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
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideRenderData;
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
    public static final String TAG_OP_EXTRA = "extra";


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
            if (!CraftGuideData.fromItemStack(player.getItemInHand(p_41434_)).getSteps().isEmpty()) {
                NetworkHooks.openScreen(serverPlayer, this, (buffer) -> {
                });
            } else {
                player.sendSystemMessage(Component.translatable("interaction.no_step"));
            }
            return InteractionResultHolder.consume(player.getItemInHand(p_41434_));
        } else {
            return InteractionResultHolder.pass(player.getItemInHand(p_41434_));
        }
    }

    public static void rollMode(ItemStack itemInHand, ServerPlayer serverPlayer, int value) {
        CraftGuideData craftGuideData = CraftGuideData.fromItemStack(itemInHand);
        int selectId = getSelectId(itemInHand);
        selectId = (selectId + value + craftGuideData.getSteps().size() + 1) % (craftGuideData.getSteps().size() + 1);
        setSelectId(itemInHand, selectId);
        if (selectId == craftGuideData.getSteps().size())
            serverPlayer.sendSystemMessage(Component.translatable("interaction.select_step_new"));
        else
            serverPlayer.sendSystemMessage(Component.translatable("interaction.select_step_index", selectId + 1));
        CraftGuideRenderData.recalculateItemStack(itemInHand);
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
            if (craftGuideData.getSteps().size() == 1 && craftGuideData.selecting == 0) {
                if (!craftGuideData.getSteps().get(0).storage.getPos().equals(context.getClickedPos())) {
                    craftGuideData.getSteps().remove(0);
                    craftGuideData.type = CommonType.TYPE;
                }
            }

            if (craftGuideData.selecting == 0 && craftGuideData.getSteps().size() == 0) {
                if (craftGuideData.getType().equals(CommonType.TYPE))
                    specialType = CraftManager.getInstance().getTargetType((ServerLevel) context.getLevel(),
                            context.getClickedPos(),
                            context.getClickedFace());
            }
            if (specialType == null) specialType = CommonType.TYPE;
            Target target = MaidStorage.getInstance().isValidTarget((ServerLevel) context.getLevel(), context.getPlayer(), context.getClickedPos(), context.getClickedFace());
            if (target == null) {
                target = Target.virtual(context.getClickedPos(), context.getClickedFace());
            }
            if (specialType == CommonType.TYPE) {
                if (craftGuideData.getSteps().size() <= selecting) {
                    if (!craftGuideData.getSteps().isEmpty() && !craftGuideData.getSteps().get(0).actionType.canBeCommon()) {
                        craftGuideData.getSteps().get(0).actionType = CraftManager.getInstance().getDefaultAction();
                        craftGuideData.getSteps().get(0).action = craftGuideData.getSteps().get(0).actionType.type();
                    }
                    craftGuideData.getSteps().add(CraftGuideStepData.createFromTypeStorage(target.withoutSide(), CraftManager.getInstance().getDefaultAction().type()));
                } else {
                    CraftGuideStepData craftGuideStepData = craftGuideData.getSteps().get(selecting);
                    if (!craftGuideStepData.actionType.canBeCommon()) {
                        craftGuideStepData.actionType = CraftManager.getInstance().getDefaultAction();
                        craftGuideStepData.action = craftGuideStepData.actionType.type();

                        craftGuideStepData.storage = target.withoutSide();
                    } else {
                        Target existingTarget = craftGuideStepData.getStorage();
                        if (existingTarget.equals(target)) {
                            craftGuideData.getSteps().remove(selecting);
                        } else if (existingTarget.withoutSide().equals(target.withoutSide())) {
                            craftGuideStepData.storage = target;
                        } else {
                            craftGuideStepData.storage = target.withoutSide();
                        }
                    }
                }
                craftGuideData.type = specialType;
            } else {
                List<CraftGuideStepData> steps = craftGuideData.getSteps();
                steps.clear();
                steps.add(CraftGuideStepData.createFromTypeStorage(target.withoutSide(), specialType));
                craftGuideData.type = specialType;
            }
            if (craftGuideData.type != CommonType.TYPE) {
                CraftManager.getInstance()
                        .getType(craftGuideData.type)
                        .onTypeUsing(serverPlayer, itemInHand, craftGuideData);
            }
            craftGuideData.saveToItemStack(itemInHand);
            CraftGuideRenderData.recalculateItemStack(itemInHand);
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

        CraftGuideRenderData data = CraftGuideRenderData.fromItemStack(itemStack);
        toolTip.add(Component.translatable("tooltip.maid_storage_manager.craft_guide.input.title", data.inputs.size()).withStyle(ChatFormatting.GRAY));
        for (ItemStack input : data.inputs) {
            toolTip.add(Component.translatable("tooltip.maid_storage_manager.craft_guide.input.item", input.getHoverName(), input.getCount()).withStyle(ChatFormatting.GRAY));
        }
        toolTip.add(Component.translatable("tooltip.maid_storage_manager.craft_guide.output.title", data.outputs.size()).withStyle(ChatFormatting.GRAY));
        for (ItemStack output : data.outputs) {
            toolTip.add(Component.translatable("tooltip.maid_storage_manager.craft_guide.output.item", output.getHoverName(), output.getCount()).withStyle(ChatFormatting.GRAY));
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
        return type.createGui(p_39954_, p_39956_.level(), p_39956_, craftGuideData);
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
