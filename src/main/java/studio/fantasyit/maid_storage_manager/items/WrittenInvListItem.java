package studio.fantasyit.maid_storage_manager.items;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.network.Network;
import studio.fantasyit.maid_storage_manager.registry.DataComponentRegistry;

import java.util.function.Consumer;

//import studio.fantasyit.maid_storage_manager.menu.InventoryListScreen;

public class WrittenInvListItem extends Item {
    public static final String TAG_UUID = "uuid";
    public static final String TAG_AUTHOR = "author";
    public static final String TAG_TIME = "time";

    public WrittenInvListItem(Identifier id) {
        super(
                new Properties()
                        .stacksTo(1)
                        .setId(ResourceKey.create(Registries.ITEM, id))
        );
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public @NotNull InteractionResult use(Level level, @NotNull Player player, @NotNull InteractionHand p_41434_) {
        if (level.isClientSide()) {
            ItemStack stack = player.getMainHandItem();
            if (stack.has(DataComponentRegistry.INVENTORY_UUID)) {
                Network.sendRequestListPacket(stack.get(DataComponentRegistry.INVENTORY_UUID));
                // InventoryListScreen disabled
                // Minecraft.getInstance().setScreen(new InventoryListScreen(stack.get(DataComponentRegistry.INVENTORY_UUID)));
            }
            return InteractionResult.SUCCESS;
        } else {
            return InteractionResult.PASS;
        }
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext p_339594_, TooltipDisplay tooltipDisplay, Consumer<Component> tooltip, TooltipFlag p_41424_) {
        super.appendHoverText(itemStack, p_339594_, tooltipDisplay, tooltip, p_41424_);
        tooltip.accept(Component.translatable("tooltip.maid_storage_manager.written_request_list.desc").withStyle(ChatFormatting.GRAY));
        if (itemStack.has(DataComponentRegistry.INVENTORY_AUTHOR))
            tooltip.accept(Component.translatable(
                    "tooltip.maid_storage_manager.request_list.author",
                    itemStack.get(DataComponentRegistry.INVENTORY_AUTHOR)
            ));
        if (itemStack.has(DataComponentRegistry.INVENTORY_TIME))
            tooltip.accept(Component.translatable(
                    "tooltip.maid_storage_manager.request_list.time",
                    getTimeStr(itemStack.get(DataComponentRegistry.INVENTORY_TIME))
            ));
    }

    private String getTimeStr(long aLong) {
        long day = aLong / (24000);
        long hour = (aLong - day * 24000) / 1000;
        long minute = (aLong - day * 24000 - hour * 1000) * 60 / 1000;
        return Component
                .translatable("tooltip.maid_storage_manager.request_list.time.str",
                        String.valueOf(day),
                        String.format("%02d", hour),
                        String.format("%02d", minute))
                .getString();
    }

    public void setAttributes(ItemStack stack, double attackDamage, double attackSpeed) {
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.builder()
                .add(
                        Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(BASE_ATTACK_DAMAGE_ID, attackDamage, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.bySlot(EquipmentSlot.MAINHAND)
                )
                .add(
                        Attributes.ATTACK_SPEED,
                        new AttributeModifier(BASE_ATTACK_SPEED_ID, attackSpeed, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.bySlot(EquipmentSlot.MAINHAND)
                )
                .build()
        );
    }
}