package studio.fantasyit.tour_guide.step;

import net.minecraft.resources.ResourceLocation;

public record TourStepId<T>(ResourceLocation id, Class<T> caster) {
    public T cast(Object obj) {
        return caster.cast(obj);
    }
}
