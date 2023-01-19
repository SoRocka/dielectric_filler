package filler.utils;

public class GCodeUtils {

    public static final GCodeUtils G_CODE_UTILS = new GCodeUtils();

    private final int movementSpeedFast;
    private final int movementSpeedSlow;
    private final double matrialCount;
    private double gcodeAValue;

    private GCodeUtils(){
        movementSpeedFast = 1500;
        movementSpeedSlow = 4600;
        matrialCount = 0.0328;
        gcodeAValue = 0.01;
    }

    public int getMovement_speed(int z) {
        return ((z > 2) ? movementSpeedFast : movementSpeedSlow);
    }

    public double getMaterialCount() {
        return matrialCount;
    }

    public double getAAxisValue() {
        return gcodeAValue;
    }

    public double getAAxisValue(double add) {
        gcodeAValue += add;
        return gcodeAValue;
    }

    public void setAAxisValue(double gcodeAValue) {
        this.gcodeAValue = gcodeAValue;
    }

    public void setDefaultAValue() {
        gcodeAValue = 0.01;
    }
}
