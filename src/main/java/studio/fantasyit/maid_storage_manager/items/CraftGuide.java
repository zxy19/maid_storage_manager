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
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.items.render.CustomItemRenderer;
import studio.fantasyit.maid_storage_manager.menu.CraftGuideMenu;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;
import studio.fantasyit.maid_storage_manager.storage.Target;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class CraftGuide extends Item implements MenuProvider {
    public static final String TAG_RESULT = "result";
    public static final String TAG_SELECTING = "selecting";
    public static final String TAG_INPUT = "input1";
    public static final String TAG_INPUT_2 = "input2";
    public static final String TAG_OUTPUT = "output";
    public static final String TAG_OP_STORAGE = "side";
    public static final String TAG_OP_ITEMS = "items";
    public static final String TAG_OP_ACTION = "item";
    public static final String TAG_ITEMS_ITEM = "item";
    public static final String TAG_ITEMS_COUNT = "requested";
    public static final String TAG_OP_MATCH_TAG = "match_tag";
    public static final String TAG_TYPE = "type";
    public static final String TAG_OP_OPTIONAL = "optional";


    public CraftGuide() {
        super(
                new Properties()
                        .stacksTo(1)
        );
    }

    public static boolean matchNbt(ItemStack mainHandItem, String targ) {
        if (!mainHandItem.is(ItemRegistry.REQUEST_LIST_ITEM.get()))
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

    private void rollMode(ItemStack itemInHand, ServerPlayer serverPlayer) {
        CompoundTag tag = itemInHand.getOrCreateTag();
        int selectId = tag.getInt(TAG_SELECTING);
        selectId = (selectId + 1) % TAG_TARG.length;
        tag.putInt(TAG_SELECTING, selectId);
        itemInHand.setTag(tag);
        serverPlayer.sendSystemMessage(Component.translatable("interaction.select_" + switch (TAG_TARG[selectId]) {
            case TAG_INPUT -> "input1";
            case TAG_INPUT_2 -> "input2";
            case TAG_OUTPUT -> "output";
            default -> "error";
        }));
    }

    public static void rollMode(ItemStack itemInHand, ServerPlayer serverPlayer, int value) {
        CompoundTag tag = itemInHand.getOrCreateTag();
        int selectId = tag.getInt(TAG_SELECTING);
        selectId = (selectId + (value > 0 ? 1 : (TAG_TARG.length - 1))) % TAG_TARG.length;
        tag.putInt(TAG_SELECTING, selectId);
        itemInHand.setTag(tag);
        serverPlayer.sendSystemMessage(Component.translatable("interaction.select_" + switch (TAG_TARG[selectId]) {
            case TAG_INPUT -> "input1";
            case TAG_INPUT_2 -> "input2";
            case TAG_OUTPUT -> "output";
            default -> "error";
        }));
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
        if (!context.getLevel().isClientSide && context.getPlayer() instanceof ServerPlayer serverPlayer) {
            if (!serverPlayer.isShiftKeyDown())
                return InteractionResult.PASS;
            ItemStack itemInHand = context.getItemInHand();
            CompoundTag tag = itemInHand.getOrCreateTag();

            //工作台特判
            if (context.getLevel().getBlockState(context.getClickedPos()).is(Blocks.CRAFTING_TABLE)) {
                CompoundTag tmp = new CompoundTag();
                tmp.put(TAG_OP_STORAGE, new Target(
                        new ResourceLocation(MaidStorageManager.MODID, "crafting"),
                        context.getClickedPos()
                ).toNbt());
                tag.put(TAG_INPUT, tmp);
                tag.put(TAG_INPUT_2, new CompoundTag());
                tag.put(TAG_OUTPUT, new CompoundTag());
            } else {
                Target validTarget = MaidStorage.getInstance().isValidTarget((ServerLevel) context.getLevel(), serverPlayer, context.getClickedPos(), context.getClickedFace());
                Target sidelessTarget = MaidStorage.getInstance().isValidTarget((ServerLevel) context.getLevel(), serverPlayer, context.getClickedPos());
                if (validTarget == null) return InteractionResult.CONSUME;
                if (tag.getCompound(TAG_INPUT).contains(TAG_OP_STORAGE)) {
                    //如果之前是工作台配方，那么应该清空所有的格子
                    CompoundTag tag1 = tag.getCompound(TAG_INPUT).getCompound(TAG_OP_STORAGE);
                    if (Target.fromNbt(tag1).getType().equals(new ResourceLocation(MaidStorageManager.MODID, "crafting"))) {
                        tag.put(TAG_INPUT, new CompoundTag());
                        tag.put(TAG_INPUT_2, new CompoundTag());
                        tag.put(TAG_OUTPUT, new CompoundTag());
                    }
                }
                String tagTarg = TAG_TARG[tag.getInt(TAG_SELECTING)];
                if (!tag.contains(tagTarg)) {
                    tag.put(tagTarg, new CompoundTag());
                }
                if (!tag.getCompound(tagTarg).contains(TAG_OP_STORAGE)) {
                    //不存在，设置
                    CompoundTag tmp = tag.getCompound(tagTarg);
                    tmp.put(TAG_OP_STORAGE, sidelessTarget.toNbt());
                    tag.put(tagTarg, tmp);
                }else{
                    Target storage = Target.fromNbt(tag.getCompound(tagTarg).getCompound(TAG_OP_STORAGE));
                    if (storage.getPos().equals(context.getClickedPos()) && storage.getSide().isPresent() && storage.getSide().get() == context.getClickedFace()) {
                        CompoundTag compound = tag.getCompound(tagTarg);
                        compound.remove(TAG_OP_STORAGE);
                        tag.put(tagTarg, compound);
                    } else {
                        if (storage.pos.equals(context.getClickedPos())) {
                            storage.side = context.getClickedFace();
                        } else {
                            storage.pos = context.getClickedPos();
                            storage.side = null;
                        }
                        CompoundTag tmp = tag.getCompound(tagTarg);
                        tmp.put(TAG_OP_STORAGE, storage.toNbt());
                        tag.put(tagTarg, tmp);
                    }
                }
            }
            itemInHand.setTag(tag);
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
        CraftGuideStepData input1 = craftGuideData.getInput1();
        if (input1.available()) {
            toolTip.add(Component.translatable("tooltip.maid_storage_manager.craft_guide.input1"));
            addTooltip(input1, toolTip);
        }
        CraftGuideStepData input2 = craftGuideData.getInput2();
        if (input2.available()) {
            toolTip.add(Component.translatable("tooltip.maid_storage_manager.craft_guide.input2"));
            addTooltip(input2, toolTip);
        }
        CraftGuideStepData output = craftGuideData.getOutput();
        if (output.available()) {
            toolTip.add(Component.translatable("tooltip.maid_storage_manager.craft_guide.output"));
            addTooltip(output, toolTip);
        }
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
        return new CraftGuideMenu(p_39954_, p_39956_);
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
