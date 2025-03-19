package studio.fantasyit.maid_storage_manager.util;

public class MathUtil {
    public static int biMaxStepCalc(int current) {
        int m = (int) Math.floor(log2(current));
        return (int) ((m + 1) * (current) - Math.pow(2, (m + 1)) + 1);
    }

    public static double log2(double x){
        return Math.log(x) / Math.log(2);
    }
}
