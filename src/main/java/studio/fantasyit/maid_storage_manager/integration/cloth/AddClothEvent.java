package studio.fantasyit.maid_storage_manager.integration.cloth;

import com.github.tartaricacid.touhoulittlemaid.api.event.client.AddClothConfigEvent;

public class AddClothEvent {
    public static void init(AddClothConfigEvent event) {
        ClothEntry.createEntry(event.getRoot(), event.getEntryBuilder());
    }
}
