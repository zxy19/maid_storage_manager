package studio.fantasyit.maid_storage_manager.items;

import com.github.tartaricacid.touhoulittlemaid.api.bauble.IMaidBauble;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.items.data.TargetList;
import studio.fantasyit.maid_storage_manager.registry.DataComponentRegistry;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;
import studio.fantasyit.maid_storage_manager.storage.Target;

import java.util.List;
import java.util.Optional;

public class StorageDefineBauble extends MaidInteractItem implements IMaidBauble {

    public static String TAG_STORAGES = "storages";
    public static String TAG_MODE = "mode";

    public static final String TAG_STORAGE_DEFINE = "storage_define";


    public enum Mode {
        APPEND,
        REMOVE,
        REPLACE,
        REPLACE_SPEC
    }

    public StorageDefineBauble() {
        super(
                new Properties().stacksTo(1)
                        .component(DataComponentRegistry.TARGETS, new TargetList().toImmutable())
                        .component(DataComponentRegistry.STORAGE_DEFINE_MODE, Mode.APPEND.name())
        );
    }

    public static Mode rollMode(ItemStack stack) {
        Mode currentMode = Mode.valueOf(stack.getOrDefault(DataComponentRegistry.STORAGE_DEFINE_MODE, Mode.APPEND.name()));
        Mode newMode = Mode.values()[(currentMode.ordinal() + 1) % Mode.values().length];
        stack.set(DataComponentRegistry.STORAGE_DEFINE_MODE, newMode.name());
        return newMode;
    }

    public static void rollMode(ItemStack stack, ServerPlayer sender, int value) {
        Mode currentMode = Mode.valueOf(stack.getOrDefault(DataComponentRegistry.STORAGE_DEFINE_MODE, Mode.APPEND.name()));
        int dv = value > 0 ? 1 : Mode.values().length - 1;
        Mode newMode = Mode.values()[(currentMode.ordinal() + dv) % Mode.values().length];
        sender.sendSystemMessage(Component.translatable("interaction.mode_" + switch (newMode) {
            case APPEND -> "append";
            case REMOVE -> "remove";
            case REPLACE -> "replace";
            case REPLACE_SPEC -> "replace_spec";
        }));
        stack.set(DataComponentRegistry.STORAGE_DEFINE_MODE, newMode.name());
    }

    public static Mode getMode(ItemStack stack) {
        return Mode.valueOf(stack.getOrDefault(DataComponentRegistry.STORAGE_DEFINE_MODE, Mode.APPEND.name()));
    }

    public static List<Target> getStorages(ItemStack stack) {
        return Optional.ofNullable(stack.get(DataComponentRegistry.TARGETS)).map(TargetList.Immutable::targets).orElse(List.of());
    }


    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
        if (!context.getLevel().isClientSide && context.getPlayer() instanceof ServerPlayer serverPlayer && serverPlayer.isShiftKeyDown()) {
            BlockPos clickedPos = context.getClickedPos();
            Direction side = context.getClickedFace();
            Target validTarget = MaidStorage.getInstance().isValidTarget((ServerLevel) context.getLevel(), serverPlayer, clickedPos, side);
            if (validTarget != null) {
                ItemStack item = serverPlayer.getMainHandItem();
                TargetList targetList = Optional.ofNullable(item.get(DataComponentRegistry.TARGETS))
                        .map(TargetList.Immutable::toMutable).orElse(new TargetList());
                List<Target> list = targetList.targets();
                boolean found = false;
                for (int i = 0; i < list.size(); i++) {
                    Target storage = list.get(i);
                    if (storage.equals(validTarget)) {
                        list.remove(i);
                        found = true;
                        break;
                    } else if (storage.pos.equals(validTarget.pos)) {
                        list.set(i, validTarget);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    list.add(validTarget.withoutSide());
                }
                item.set(DataComponentRegistry.TARGETS, targetList.toImmutable());
            }
            return InteractionResult.CONSUME;
        } else {
            return InteractionResult.CONSUME;
        }
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        return this.useOn(context);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext p_339594_, List<Component> toolTip, TooltipFlag p_41424_) {
        super.appendHoverText(itemStack, p_339594_, toolTip, p_41424_);
        Mode mode = getMode(itemStack);
        int count = getStorages(itemStack).size();
        toolTip.add(Component.translatable("interaction.mode_" + switch (mode) {
            case APPEND -> "append";
            case REMOVE -> "remove";
            case REPLACE -> "replace";
            case REPLACE_SPEC -> "replace_spec";
        }));
        toolTip.add(Component.translatable("tooltip.maid_storage_manager.storage_define_bauble.count", count));
    }
}
