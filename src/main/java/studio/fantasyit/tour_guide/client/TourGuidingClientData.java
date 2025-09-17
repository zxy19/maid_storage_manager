package studio.fantasyit.tour_guide.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import studio.fantasyit.tour_guide.mark.IMark;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class TourGuidingClientData {
    static List<IMark> marks = List.of();

    public static void setMarks(List<IMark> _marks) {
        marks = _marks;
    }

    public static List<IMark> getMarks() {
        return marks;
    }

    public static void clear() {
        marks.clear();
    }
}
