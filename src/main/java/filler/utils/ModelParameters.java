package filler.utils;

public class ModelParameters {

    public static final ModelParameters MODEL_SETTINGS = new ModelParameters();

    private double widthX;
    private double widthY;
    private double widthZ;
    private double defaultCellSize;
    private double layerHeight;
    private double epsilonZero;
    private double epsilonMinimum;
    private double extruderSize;
    private int extruderTemp;
    private int tableTemp;
    private double epsilonRate = 1;
    private double fwd;
    private double bck;

    private ModelParameters() {

    }

    public double getFwd() {
        return fwd;
    }

    public void setFwd(double fwd) {
        this.fwd = fwd;
    }

    public double getBck() {
        return bck;
    }

    public void setBck(double bck) {
        this.bck = bck;
    }

    public double getWidthX() {
        return widthX;
    }

    public void setWidthX(double widthX) {
        this.widthX = widthX;
    }

    public double getWidthY() {
        return widthY;
    }

    public void setWidthY(double widthY) {
        this.widthY = widthY;
    }

    public double getWidthZ() {
        return widthZ;
    }

    public void setWidthZ(double widthZ) {
        this.widthZ = widthZ;
    }

    public double getDefaultCellSize() {
        return defaultCellSize;
    }

    public void setDefaultCellSize(double defaultCellSize) {
        this.defaultCellSize = defaultCellSize;
    }

    public double getLayerHeight() {
        return layerHeight;
    }

    public void setLayerHeight(double layerHeight) {
        this.layerHeight = layerHeight;
    }

    public double getEpsilonZero() {
        return epsilonZero;
    }

    public void setEpsilonZero(double epsilonZero) {
        this.epsilonZero = epsilonZero;
    }

    public double getEpsilonMinimum() {
        return epsilonMinimum;
    }

    public void setEpsilonMinimum(double epsilonMinimum) {
        this.epsilonMinimum = epsilonMinimum;
    }

    public double getExtruderSize() {
        return extruderSize;
    }

    public void setExtruderSize(double extruderSize) {
        this.extruderSize = extruderSize;
    }

    public int getExtruderTemp() {
        return extruderTemp;
    }

    public void setExtruderTemp(int extruderTemp) {
        this.extruderTemp = extruderTemp;
    }

    public int getTableTemp() {
        return tableTemp;
    }

    public void setTableTemp(int tableTemp) {
        this.tableTemp = tableTemp;
    }

    public double getEpsilonRate() {
        return epsilonRate;
    }

    public void setEpsilonRate(double epsilonRate) {
        this.epsilonRate = epsilonRate;
    }
}


