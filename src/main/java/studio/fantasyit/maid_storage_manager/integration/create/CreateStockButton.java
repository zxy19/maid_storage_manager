package studio.fantasyit.maid_storage_manager.integration.create;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.client.gui.widget.button.MaidSideTabButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.PacketDistributor;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.network.CreateStockManagerPacket;
import studio.fantasyit.maid_storage_manager.network.Network;

import java.util.List;

public class CreateStockButton extends MaidSideTabButton {
    private static final ResourceLocation SIDE = ResourceLocation.fromNamespaceAndPath(TouhouLittleMaid.MOD_ID, "textures/gui/maid_gui_side.png");
    private static final ResourceLocation ICON = ResourceLocation.fromNamespaceAndPath(MaidStorageManager.MODID, "textures/gui/create_stock_keeper.png");
    BlockPos ticker;
    int maidId;

    public CreateStockButton(int x, int y, BlockPos ticker, int maidId) {
        super(x, y, 2 * 25, b -> {
            if (b instanceof CreateStockButton csb) csb.click();
        }, List.of(Component.translatable("gui.maid_storage_manager.create_stock_button")));
        this.ticker = ticker;
        this.maidId = maidId;
    }

    public void click() {
        StockManagerInteract.setInteractedMaidId(maidId);
        Network.INSTANCE.send(PacketDistributor.SERVER.noArg(),
                new CreateStockManagerPacket(CreateStockManagerPacket.Type.OPEN_SCREEN,
                        ticker,
                        maidId
                )
        );
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        graphics.blit(SIDE, this.getX() + 3, this.getY(), 235.0F, 157F, this.width, this.height, 256, 256);
        graphics.blit(ICON, this.getX() + 4, this.getY() + 2, 0, 0, 16, 16, 16, 16);
    }
}
