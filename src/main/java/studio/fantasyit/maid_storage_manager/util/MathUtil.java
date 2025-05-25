package studio.fantasyit.maid_storage_manager.util;

import net.minecraft.world.phys.Vec3;

public class MathUtil {
    public static int biMaxStepCalc(int current) {
        int m = (int) Math.floor(log2(current));
        return (int) ((m + 1) * (current) - Math.pow(2, (m + 1)) + 1);
    }

    public static double log2(double x) {
        return Math.log(x) / Math.log(2);
    }

    public static Vec3 getFromToWithFriction(Vec3 from, Vec3 to) {
        return getFromToWithFriction(from, to, 0.6);
    }

    public static Vec3 getFromToWithFriction(Vec3 from, Vec3 to, double friction) {
        return new Vec3(
                Math.sqrt((to.x - from.x) * 2 * friction),
                Math.sqrt((to.y - from.y) * 2 * friction),
                Math.sqrt((to.z - from.z) * 2 * friction)
        );
    }
}