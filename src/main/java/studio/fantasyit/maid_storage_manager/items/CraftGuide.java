package studio.fantasyit.maid_storage_manager.items;

import net.minecraft.ChatFormatting;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.craft.CraftManager;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideRenderData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.type.CommonType;
import studio.fantasyit.maid_storage_manager.craft.type.ICraftType;
import studio.fantasyit.maid_storage_manager.registry.DataComponentRegistry;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;
import studio.fantasyit.maid_storage_manager.storage.Target;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CraftGuide extends Item implements MenuProvider {
    private static CraftGuideData EMPTY = null;

    public static CraftGuideData empty() {
        if (EMPTY == null)
            EMPTY = new CraftGuideData(new ArrayList<>(), CommonType.TYPE);
        return EMPTY;
    }

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
    public static final String TAG_SPECIAL_OP = "special";
    public static final String TAG_MARK_MERGEABLE = "mergeable";
    public static final String TAG_MARK_NO_OCCUPY = "no_occupy";

    public static Component getStatusMessage(ItemStack stack) {
        int selectId = getSelectId(stack);
        SpecialOP specialOP = getSpecialOP(stack);
        CraftGuideRenderData data = stack.getOrDefault(DataComponentRegistry.CRAFT_GUIDE_RENDER, CraftGuideRenderData.EMPTY);
        Component result;
        if (selectId == data.stepBindings.size())
            result = Component.translatable("interaction.select_step_new." + specialOP.name().toLowerCase());
        else
            result = Component.translatable("interaction.select_step_index." + specialOP.name().toLowerCase(), selectId + 1);

        return switch (specialOP) {
            case NONE -> result;
            case COPY -> result.copy().withStyle(ChatFormatting.YELLOW);
            case REPLACE -> result.copy().withStyle(ChatFormatting.RED);
        };
    }

    public enum SpecialOP {
        NONE,
        COPY,
        REPLACE
    }


    public CraftGuide() {
        super(
                new Properties()
                        .stacksTo(1)
        );
    }

    public static int getSelectId(ItemStack itemInHand) {
        return itemInHand.getOrDefault(DataComponentRegistry.SELECTING, 0);
    }

    public static void setSelectId(ItemStack itemInHand, int value) {
        itemInHand.set(DataComponentRegistry.SELECTING, value);
    }

    public static void setSpecialOP(ItemStack itemInHand, SpecialOP value) {
        itemInHand.set(DataComponentRegistry.CRAFT_GUIDE_SPECIAL, value.name());
    }

    public static SpecialOP getSpecialOP(ItemStack itemInHand) {
        return SpecialOP.valueOf(itemInHand.getOrDefault(DataComponentRegistry.CRAFT_GUIDE_SPECIAL, SpecialOP.NONE.name()));
    }

    public static CraftGuideData getCraftGuide(ItemStack itemInHand) {
        return itemInHand.getOrDefault(DataComponentRegistry.CRAFT_GUIDE_DATA, CraftGuide.empty()).copy();
    }

    public static CraftGuideData getCraftGuideReadOnly(ItemStack itemInHand) {
        return itemInHand.getOrDefault(DataComponentRegistry.CRAFT_GUIDE_DATA, CraftGuide.empty());
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, @NotNull Player player, @NotNull InteractionHand p_41434_) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            CraftGuideData cgd = getCraftGuideReadOnly(player.getItemInHand(p_41434_));
            if (!cgd.getSteps().isEmpty()) {
                serverPlayer.openMenu(this, (buffer) -> {
                });
            } else {
                player.sendSystemMessage(Component.translatable("interaction.no_step"));
            }
            return InteractionResultHolder.consume(player.getItemInHand(p_41434_));
        } else {
            return InteractionResultHolder.pass(player.getItemInHand(p_41434_));
        }
    }

    public static void rollSpecial(ItemStack itemInHand, ServerPlayer serverPlayer, int value) {
        SpecialOP specialOP = getSpecialOP(itemInHand);
        specialOP = SpecialOP.values()[(specialOP.ordinal() + value + SpecialOP.values().length) % SpecialOP.values().length];
        setSpecialOP(itemInHand, specialOP);
    }

    public static void rollMode(ItemStack itemInHand, ServerPlayer serverPlayer, int value) {
        CraftGuideData craftGuideData = getCraftGuide(itemInHand);
        int selectId = getSelectId(itemInHand);
        selectId = (selectId + value + craftGuideData.getSteps().size() + 1) % (craftGuideData.getSteps().size() + 1);
        setSelectId(itemInHand, selectId);
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
            CraftGuideData craftGuideData = getCraftGuide(itemInHand);
            craftGuideData.selecting = getSelectId(itemInHand);
            SpecialOP specialOP = getSpecialOP(itemInHand);
            @NotNull InteractionResult result = switch (specialOP) {
                case NONE -> operateNormal(context, serverPlayer, craftGuideData, itemInHand);
                case COPY -> operateCopy(context, serverPlayer, craftGuideData, itemInHand);
                case REPLACE -> operateReplace(context, serverPlayer, craftGuideData, itemInHand);
            };

            itemInHand.set(DataComponentRegistry.CRAFT_GUIDE_DATA, craftGuideData);
            CraftGuideRenderData.recalculateItemStack(itemInHand);
            return result;
        } else {
            if (Objects.requireNonNull(context.getPlayer()).isShiftKeyDown())
                return InteractionResult.CONSUME;
            return InteractionResult.PASS;
        }
    }

    private @NotNull InteractionResult operateReplace(@NotNull UseOnContext context, ServerPlayer serverPlayer, CraftGuideData craftGuideData, ItemStack itemInHand) {
        if (craftGuideData.getSteps().isEmpty()) return InteractionResult.PASS;
        if (craftGuideData.getSteps().size() == craftGuideData.selecting) {
            craftGuideData.getSteps()
                    .forEach(t -> {
                        t.storage = new Target(t.storage.getType(), context.getClickedPos(), t.storage.getSide());
                    });
        } else {
            CraftGuideStepData step = craftGuideData.getStepByIdx(craftGuideData.selecting);
            step.storage = new Target(step.storage.getType(), context.getClickedPos(), step.storage.getSide());
        }
        return InteractionResult.SUCCESS;
    }

    private @NotNull InteractionResult operateCopy(@NotNull UseOnContext context, ServerPlayer serverPlayer, CraftGuideData craftGuideData, ItemStack itemInHand) {
        if (craftGuideData.getSteps().isEmpty()) return InteractionResult.PASS;
        //复制全部的step
        if (craftGuideData.getSteps().size() == craftGuideData.selecting) {
            new ArrayList<>(craftGuideData.getSteps())
                    .stream()
                    .map(t -> CraftGuideStepData.fromCompound(serverPlayer.registryAccess(), t.toCompound(serverPlayer.registryAccess())))
                    .forEach(t -> {
                        t.storage = new Target(t.storage.getType(), context.getClickedPos(), t.storage.getSide());
                        craftGuideData.steps.add(t);
                    });
        } else {
            CraftGuideStepData newStep = CraftGuideStepData.fromCompound(serverPlayer.registryAccess(), craftGuideData.getSteps().get(craftGuideData.selecting).toCompound(serverPlayer.registryAccess()));
            newStep.storage = new Target(newStep.storage.getType(), context.getClickedPos(), newStep.storage.getSide());
            craftGuideData.steps.add(newStep);
        }
        craftGuideData.selecting = craftGuideData.steps.size() - 1;

        return InteractionResult.SUCCESS;
    }

    private @NotNull InteractionResult operateNormal(@NotNull UseOnContext context, ServerPlayer serverPlayer, CraftGuideData craftGuideData, ItemStack itemInHand) {
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
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext p_339594_, List<Component> toolTip, TooltipFlag p_41424_) {
        super.appendHoverText(itemStack, p_339594_, toolTip, p_41424_);
        CraftGuideRenderData data = itemStack.getOrDefault(DataComponentRegistry.CRAFT_GUIDE_RENDER, CraftGuideRenderData.EMPTY);
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
        CraftGuideData craftGuideData = getCraftGuideReadOnly(p_39956_.getMainHandItem());
        if (craftGuideData.getType() == null) return null;
        ICraftType type = CraftManager.getInstance().getType(craftGuideData.getType());
        if (type == null) return null;
        return type.createGui(p_39954_, p_39956_.level(), p_39956_, craftGuideData);
    }



}
