package filler.entities;

import java.util.ArrayList;
import java.util.List;

import static filler.entities.Point.newCoord;
import static filler.utils.GCodeUtils.G_CODE_UTILS;
import static filler.utils.ModelParameters.MODEL_SETTINGS;
import static java.lang.Math.pow;
import static java.lang.Math.round;
import static java.lang.String.valueOf;

public class Cell {

    public static final String NUMBER_FORMAT = "%.4f";

    private Point coord = new Point();
    private Point sizes = new Point();
    private Point layer_size = new Point();

    public void setCoordX(double coordX) {
        coord.setX(coordX);
    }

    public void setCoordY(double coordY) {
        coord.setY(coordY);
    }

    public void setSizesX(double sizesX) {
        sizes.setX(sizesX);
    }

    public void setSizesY(double sizesY) {
        sizes.setY(sizesY);
    }

    private int layerNumber;
    private List<Point> points;
    private List<Double> branch = new ArrayList<>();
    private int maxBranchCount;
    private double epsilonCoef;
    private double extruderSize;
    private double fixPadding;

    public Cell() {
        extruderSize = MODEL_SETTINGS.getExtruderSize();
        fixPadding = extruderSize / 2.0;
        points = new ArrayList<>();
        maxBranchCount = 15;
    }

    public void setLayer_size(Point sizes) {
        this.layer_size.setX(sizes.getX());
        this.layer_size.setY(sizes.getY());
    }

    // Calculating the dielectric constant for current cell like relative filling area.
    public void calculateCoef(int z) {
        layerNumber = z;
        double h_2 = pow(MODEL_SETTINGS.getWidthZ() - (double) layerNumber * 0.2, 2);
        double x_2 = pow(layer_size.getX() / 2.0 - coord.getX() - sizes.getX() / 2.0, 2);
        double y_2 = pow(layer_size.getY() / 2.0 - coord.getY() - sizes.getY() / 2.0, 2);
        epsilonCoef = MODEL_SETTINGS.getEpsilonRate() *
                (MODEL_SETTINGS.getEpsilonZero() * h_2 / (h_2 + x_2 + y_2)) * 0.714 - 0.658;
        if (epsilonCoef < 0) {
            System.out.println("Epsilon < 0");
        }
    }

    // Calculating points of trace
    public void calc_points(boolean reflected, boolean rotated) {
        int leg_max = maxBranchCount;
        double horPadding = 0.0;
        double verPadding = 0.0;

        double verPaddingStep = 0.01;
        double horPaddingStep = 0.01;
        if (rotated) {
            // Replace x and y
            sizes.replace();
        }
        boolean horOrVerPadding = sizes.getX() >= sizes.getY();
        // Search horizontal and vertical padding satisfying 'epsilonCoef' (relative filling area).
        while (epsilonCoef <= calc_square(horPadding, verPadding)) {
            if (leg_max != maxBranchCount) {
                // if maxBranchCount is changed:
                horOrVerPadding = !horOrVerPadding;
                leg_max = maxBranchCount;
            }
            if (horOrVerPadding)
                horPadding += horPaddingStep;
            else
                verPadding += verPaddingStep;
        }

        List<Point> listFwd = new ArrayList<Point>();
        List<Point> listBck = new ArrayList<Point>();

        listFwd.add(newCoord(extruderSize / 2, extruderSize / 2));
        listBck.add(newCoord(sizes.getX() - extruderSize / 2,
                sizes.getY() - extruderSize / 2));

        // Set cell orientation and calculating points.
        int orient = 0;
        for (int i = 1; i < branch.size(); i++) {
            if (branch.get(i) != 0) {
                switch (orient) {
                    case 0:
                        listFwd.add(newCoord(listFwd.get(listFwd.size() - 1).getX() + branch.get(i - 1),
                                listFwd.get(listFwd.size() - 1).getY()));
                        listBck.add(0, newCoord(listBck.get(0).getX() - branch.get(i - 1),
                                listBck.get(0).getY()));
                        orient = 1;
                        break;
                    case 1:
                        listFwd.add(newCoord(listFwd.get(listFwd.size() - 1).getX(),
                                listFwd.get(listFwd.size() - 1).getY() + branch.get(i - 1)));
                        listBck.add(0, newCoord(listBck.get(0).getX(),
                                listBck.get(0).getY() - branch.get(i - 1)));
                        orient = 2;
                        break;
                    case 2:
                        listFwd.add(newCoord(listFwd.get(listFwd.size() - 1).getX() - branch.get(i - 1),
                                listFwd.get(i - 1).getY()));
                        listBck.add(0, newCoord(listBck.get(0).getX() + branch.get(i - 1),
                                listBck.get(0).getY()));
                        orient = 3;
                        break;
                    case 3:
                        listFwd.add(newCoord(listFwd.get(i - 1).getX(),
                                listFwd.get(i - 1).getY() - branch.get(i - 1)));
                        listBck.add(0, newCoord(listBck.get(0).getX(),
                                listBck.get(0).getY() + branch.get(i - 1)));
                        orient = 0;
                        break;
                    default:
                        System.out.println("ERROR: variable: 'orient' is overstate.T");
                }
            }
        }
        points.addAll(listFwd);
        points.addAll(listBck);
        if (reflected) {
            reflectX();
//            reflectY();
        }
        if (rotated) {
            rotate90(reflected);
        }
    }

    private double calc_square(double horPadding, double verPadding) {
        List<Double> semiBranch = new ArrayList<Double>();

        // Calculating length semi-branch from cell size and padding between branch.
        semiBranch.add(sizes.getX() / 2.0 - fixPadding);
        semiBranch.add(sizes.getX() / 2.0 - fixPadding - horPadding);
        semiBranch.add(sizes.getY() / 2.0 - fixPadding);
        semiBranch.add(sizes.getY() / 2.0 - fixPadding - verPadding - extruderSize);
        for (int i = 4; i < 20; i++) {
            if (i % 2 == 0) {
                semiBranch.add(sizes.getX() / 2 - fixPadding - i / 2.0 * horPadding - (i / 2.0 - 1) * extruderSize);
            } else {
                semiBranch.add(sizes.getY() / 2 - fixPadding - (i - 1) / 2.0 * verPadding - (i - 1) / 2.0 * extruderSize);
            }
        }

        // Zeroing semi-branch with negative values.
        for (int i = 2; i < semiBranch.size(); i++) {
            if (semiBranch.get(i) < 0) {
                for (int j = i; j < semiBranch.size(); j++) {
                    semiBranch.set(j, 0.0);
                    maxBranchCount = i - 2;
                }
                break;
            }
        }

        branch.clear();
        // Calculating branches length.
        branch.add(semiBranch.get(0) + semiBranch.get(1));
        branch.add(semiBranch.get(2) + semiBranch.get(3));
        branch.add(semiBranch.get(1) + semiBranch.get(4));
        for (int i = 5; i < semiBranch.size(); i++) {
            if (semiBranch.get(i) < 0) {
                break;
            } else {
                branch.add(semiBranch.get(i - 2) + semiBranch.get(i));
            }
        }
        // Calculating filling coefficient.
        double square = pow(extruderSize, 2);
        for (int i = 0; i < branch.size(); i++) {
            if (branch.get(i) > 0) {
                double ex_2 = pow(extruderSize / 2, 2);
                square += 2 * (branch.get(i) * extruderSize - ex_2 + 3.1415 * ex_2 / 8);
            }
        }
        double s_s0 = square / sizes.getX() / sizes.getY();
        s_s0 = (double) round(100 * s_s0) / 100;
        return s_s0;
    }

    public String createLayerTrace(List<Point> pointList) {
        points.clear();
        points = pointList;
        return createLayerTrace(true);
    }

    // Generate G1-commands for all points of current cell.
    public String createLayerTrace(boolean last) {
        double z1 = (double) (layerNumber + 1) * 0.2D;
        StringBuilder gString = new StringBuilder();

        gString.append("\nG1 X").append(String.format(NUMBER_FORMAT, points.get(0).getX()));
        gString.append(" Y").append(String.format(NUMBER_FORMAT, points.get(0).getY()));
        gString.append(" Z").append(String.format(NUMBER_FORMAT, z1));
        gString.append(" F").append(G_CODE_UTILS.getMovement_speed(layerNumber));
        gString.append(" A").append(G_CODE_UTILS.getAAxisValue(MODEL_SETTINGS.getFwd()));

        for (int i = 1; i < points.size(); ++i) {

            gString.append("\nG1");
            gString.append(" X").append(String.format(NUMBER_FORMAT, points.get(i).getX()));
            gString.append(" Y").append(String.format(NUMBER_FORMAT, points.get(i).getY()));
            gString.append(" Z").append(String.format(NUMBER_FORMAT, z1));
            gString.append(" F").append(G_CODE_UTILS.getMovement_speed(layerNumber));
            double aValueMed;

            aValueMed = Math.pow(points.get(i - 1).getX() - points.get(i).getX(), 2.0D) + Math.pow(points.get(i - 1).getY() - points.get(i).getY(), 2.0D);

            G_CODE_UTILS.setAAxisValue(G_CODE_UTILS.getAAxisValue() + Math.sqrt(aValueMed) * G_CODE_UTILS.getMaterialCount());

            String aValueStr = valueOf(G_CODE_UTILS.getAAxisValue());
            gString.append(" A").append(aValueStr);
        }
        gString.append("\n;");
        if (last) {
            gString.append("\nG1");
            gString.append(" X").append(String.format(NUMBER_FORMAT, points.get(points.size() - 1).getX()));
            gString.append(" Y").append(String.format(NUMBER_FORMAT, points.get(points.size() - 1).getY()));
            gString.append(" Z").append(String.format(NUMBER_FORMAT, z1 + MODEL_SETTINGS.getLayerHeight()));
            gString.append(" F4000");
            gString.append(" A").append(valueOf(G_CODE_UTILS.getAAxisValue() - MODEL_SETTINGS.getBck())).append("\n; lineEnd");

        }
        return gString.toString();
    }

    private void rotate90() {
        points.forEach(point -> point.set(Point.newCoord(-point.getY() + sizes.getY(), point.getX())));
//            points.forEach(point -> point.set(newCoord(point.getY(), point.getX() * -1 + sizes.getX())));

    }

    private void rotate90(boolean ref) {
        if (ref)
            // -90
            points.forEach(point -> point.set(Point.newCoord(-point.getY() + sizes.getY(), point.getX())));
        else
            // 90
            points.forEach(point -> point.set(newCoord(point.getY(), point.getX() * -1 + sizes.getX())));

    }

    // Translate trace for place in the right location
    public void translate(double additionX, double additionY) {
        points.forEach(point -> point.translate(additionX, additionY));

//        for (int i = 0; i < points.size(); i++)
//            points.set(i, newCoord(points.get(i).getX() + coord.getX() + additionX, points.get(i).getY() + coord.getY() + additionY));
    }

    public List<Point> getPoints() {
        return points;
    }

    // Reflected trace in X-axis (reflectX and reflectY are identical).
    private void reflectX() {
        points.forEach(point -> point.setY(-point.getY() + sizes.getY()));
    }

    // Reflected trace in X-axis (reflectX and reflectY are identical).
    private void reflectY() {
        points.forEach(point -> point.setX(-point.getX() + sizes.getX()));
    }

}
