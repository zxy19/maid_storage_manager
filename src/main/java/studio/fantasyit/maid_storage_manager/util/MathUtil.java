package studio.fantasyit.maid_storage_manager.util;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import studio.fantasyit.maid_storage_manager.Config;

public class MathUtil {
    public static int biMaxStepCalc(int current) {
        int m = (int) Math.floor(log2(current));
        return (int) ((m + 1) * (current) - Math.pow(2, (m + 1)) + 1);
    }

    public static double log2(double x) {
        return Math.log(x) / Math.log(2);
    }

    public static Vec3 getFromToWithFriction(Entity entity, Vec3 to) {
        return getFromToWithFriction(entity.blockPosition().getCenter(), to);
    }

    public static Vec3 getFromToWithFriction(Vec3 from, Vec3 to) {
        return getFromToWithFriction(from, to, 0.6, 0.98);
    }

    public static Vec3 getFromToWithFriction(Vec3 from, Vec3 to, double groundFriction, double airFriction) {
//        return to.subtract(from).normalize().scale(0.02);
        return switch (Config.throwItemVector) {
            case FINALLY_POS -> {
                double deltaH = to.y - from.y;
                double vY0 = deltaH <= 0 ? 0 : Math.sqrt(0.08 * deltaH);
                double t = vY0 == 0 ? Math.sqrt(deltaH / -0.02) : (vY0 / 0.04);
                double friction = vY0 == 0 ? groundFriction : airFriction;
                double vX0 = (to.x - from.x) * (1 - 0.98 * friction);
                double vZ0 = (to.z - from.z) * (1 - 0.98 * friction);
                yield new Vec3(vX0, vY0, vZ0);
            }
            case GO_THROUGH -> {
                double deltaH = to.y - from.y;
                double vY0 = deltaH <= 0 ? 0 : Math.sqrt(0.08 * deltaH);
                double t = vY0 == 0 ? Math.sqrt(deltaH / -0.02) : (vY0 / 0.04);
                double friction = vY0 == 0 ? groundFriction : airFriction;
                double vX0 = (to.x - from.x) * (friction - 1) / (Math.pow(friction, ((int) (t / 4)) + 1) - 1);
                double vZ0 = (to.z - from.z) * (friction - 1) / (Math.pow(friction, ((int) (t / 4)) + 1) - 1);
                yield new Vec3(vX0, vY0, vZ0);
            }
            case FIXED -> to.subtract(from).normalize().scale(0.6);
        };
    }


    public static float vec2RotX(Vec3 vec) {
        return Mth.wrapDegrees((float) Math.toDegrees(Math.asin(-vec.normalize().y)));
    }

    public static float vec2RotY(Vec3 vec) {
        float yRot = (float) Math.toDegrees(Math.atan2(-vec.normalize().x, vec.normalize().z));
        yRot = Mth.wrapDegrees(yRot);
        if (yRot < 0) yRot += 360;
        return yRot;
    }

    public static int gcd(int a, int b)
    {
        while (b > 0)
        {
            int temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }
    public static int lcm(int a, int b)
    {
        return a * (b / gcd(a, b));
    }
}