package studio.fantasyit.maid_storage_manager.event;


import com.google.common.collect.Multimap;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.items.WrittenInvListItem;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;

import java.util.Map;

@Mod.EventBusSubscriber(modid = MaidStorageManager.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class AttributeModifierFixer {
    @SubscribeEvent
    public static void fixAttributeModifiers(ItemAttributeModifierEvent event) {
        if (event.getSlotType() == EquipmentSlot.MAINHAND && event.getItemStack().is(ItemRegistry.WRITTEN_INVENTORY_LIST.get())) {
            WrittenInvListItem writtenInvListItem = (WrittenInvListItem) ItemRegistry.WRITTEN_INVENTORY_LIST.get();
            Multimap<Attribute, AttributeModifier> modifiers = event.getModifiers();
            event.clearModifiers();
            for (Map.Entry<Attribute, AttributeModifier> modifierP : modifiers.entries()) {
                AttributeModifier modifier = modifierP.getValue();
                if (modifier.getName().equals(WrittenInvListItem.ATN_DAMAGE)) {
                    modifier = writtenInvListItem.getAttackDamageModifier(modifier.getAmount());
                } else if (modifier.getName().equals(WrittenInvListItem.ATN_SPEED)) {
                    modifier = writtenInvListItem.getAttackSpeedModifier(modifier.getAmount());
                }
                event.addModifier(modifierP.getKey(), modifier);
            }
        }
    }
}
