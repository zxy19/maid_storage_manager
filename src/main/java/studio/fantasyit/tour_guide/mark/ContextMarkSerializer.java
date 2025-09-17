package studio.fantasyit.tour_guide.mark;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import studio.fantasyit.tour_guide.mark.gui.GuiMainTipMark;
import studio.fantasyit.tour_guide.mark.gui.GuiRectMark;
import studio.fantasyit.tour_guide.mark.gui.GuiSlotMark;
import studio.fantasyit.tour_guide.mark.gui.GuiTextMark;
import studio.fantasyit.tour_guide.mark.world.BlockMark;
import studio.fantasyit.tour_guide.mark.world.EntityMark;
import studio.fantasyit.tour_guide.mark.world.TextMark;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ContextMarkSerializer {
    public static <T> NetworkSerializer<T> of(Function<FriendlyByteBuf, T> reader, BiConsumer<T, FriendlyByteBuf> writer) {
        return new NetworkSerializer<T>() {
            @Override
            public T read(FriendlyByteBuf buf) {
                return reader.apply(buf);
            }

            @Override
            public void write(T obj, FriendlyByteBuf buf) {
                writer.accept(obj, buf);
            }
        };
    }

    public interface NetworkSerializer<T> {
        T read(FriendlyByteBuf buf);

        void write(T obj, FriendlyByteBuf buf);
    }

    public static final Map<ResourceLocation, NetworkSerializer<?>> SERIALIZER_MAP = new HashMap<>();

    public static <T> void register(ResourceLocation id, NetworkSerializer<T> serializer) {
        SERIALIZER_MAP.put(id, serializer);
    }

    @SuppressWarnings("unchecked")
    public static <T extends IMark> void serialize(FriendlyByteBuf buf, T obj) {
        buf.writeResourceLocation(obj.getId());
        ((NetworkSerializer<T>) (SERIALIZER_MAP.get(obj.getId()))).write(obj, buf);
    }

    @SuppressWarnings("unchecked")
    public static <T> T deserialize(FriendlyByteBuf buf) {
        ResourceLocation id = buf.readResourceLocation();
        return (T) SERIALIZER_MAP.get(id).read(buf);
    }

    static {
        register(GuiTextMark.ID, of(GuiTextMark::fromNetwork, GuiTextMark::toNetwork));
        register(GuiSlotMark.ID, of(GuiSlotMark::fromNetwork, GuiSlotMark::toNetwork));
        register(GuiRectMark.ID, of(GuiRectMark::fromNetwork, GuiRectMark::toNetwork));
        register(GuiMainTipMark.ID, of(GuiMainTipMark::fromNetwork, GuiMainTipMark::toNetwork));
        register(TextMark.ID, of(TextMark::fromNetwork, TextMark::toNetwork));
        register(BlockMark.ID, of(BlockMark::fromNetwork, BlockMark::toNetwork));
        register(EntityMark.ID, of(EntityMark::fromNetwork, EntityMark::toNetwork));
    }
}
