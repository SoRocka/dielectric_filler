package filler.entities;

import com.google.common.collect.ImmutableList;
import filler.utils.FileWorker;

import javax.swing.*;
import java.io.FileNotFoundException;
import java.util.List;

import static filler.entities.Cell.NUMBER_FORMAT;
import static filler.entities.Point.newCoord;
import static filler.utils.GCodeUtils.G_CODE_UTILS;
import static filler.utils.ModelParameters.MODEL_SETTINGS;
import static java.lang.String.format;

public class Figure {
    private final double stepX;
    private final double stepY;
    private String pathToFile;
    private byte layerType = 0;

    public Figure() {
        // Step size by layer. Layer1_x (_y) = Layer0_x (_y) - 2*stepX (_y)
        stepX = MODEL_SETTINGS.getWidthX() * MODEL_SETTINGS.getLayerHeight() / (MODEL_SETTINGS.getWidthZ());
        stepY = MODEL_SETTINGS.getWidthY() * MODEL_SETTINGS.getLayerHeight() / (MODEL_SETTINGS.getWidthZ());
        // Dialog window to choose file
        JFileChooser fileOpen = new JFileChooser();
        int ret = fileOpen.showDialog(null, "Select file to save");
        if (ret == JFileChooser.APPROVE_OPTION) {
            pathToFile = fileOpen.getSelectedFile().getAbsolutePath();
        }
        addHeader();
        addRoundBase();

    }

    // Method for making pyramid structure.
    public void makePyramid() {
        int layersCount = (int) (MODEL_SETTINGS.getWidthZ()
                / MODEL_SETTINGS.getLayerHeight());
        // z < layer_count
        for(int z = 0; z < layersCount; z++) {
            Layer layers = new Layer(z, stepX, stepY);
            switch (layerType) {
                case 0:
                    layers.setRotatedAndReflected(false, false);
                    if (z % 2 == 1)
                        layerType++;
                    break;
                case 1:
                    layers.setRotatedAndReflected(true, false);
                    if (z % 2 == 1)
                        layerType++;
                    break;
                case 2:
                    layers.setRotatedAndReflected(false, true);
                    if (z % 2 == 1)
                        layerType++;
                    break;
                case 3:
                    layers.setRotatedAndReflected(true, true);
                    if (z % 2 == 1)
                        layerType = 0;
                    break;
                default:
                    break;
            }
            // Calculate cells count, border size, default cell size.
            // X-axis
            layers.calculate(true);
            // Y-axis
            layers.calculate(false);
            String layerGCode = layers.getLayerGCode();
            FileWorker.write(pathToFile, layerGCode);
        }
        addTail();
    }

    // Read header.gcode from ../templates/ and write to file.gcode.
    private void addHeader() {
        try {
            FileWorker.write(pathToFile, FileWorker.read("templates/header.gcode"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void addRoundBase() {
        double x = MODEL_SETTINGS.getWidthX();
        double y = MODEL_SETTINGS.getWidthY();
        double extruderSize = MODEL_SETTINGS.getExtruderSize();

        String baseGLine = addGLine(
                ImmutableList.of(
                        newCoord(-2 * extruderSize, -2 * extruderSize),
                        newCoord(-2 * extruderSize, y + 2 * extruderSize),
                        newCoord(x + 2 * extruderSize, y + 2 * extruderSize),
                        newCoord(x + 2 * extruderSize, -2 * extruderSize),
                        newCoord(-extruderSize, -2 * extruderSize),
                        newCoord(-extruderSize, y + extruderSize),
                        newCoord(x + extruderSize, y + extruderSize),
                        newCoord(x + extruderSize, -extruderSize),
                        newCoord(0, -extruderSize)
                ));
        FileWorker.write(pathToFile, baseGLine);
    }

    private String addGLine(List<Point> pointList) {
        int z = 0;
        StringBuilder str;
        str = new StringBuilder();
        String G1Format = "\nG1 X%s Y%s Z%s F%s A%s";
        pointList.forEach(point -> point.translate(Point.newCoord(-MODEL_SETTINGS.getWidthX() / 2, -MODEL_SETTINGS.getWidthY() / 2)));
        str.append(String.format(G1Format,
                format(NUMBER_FORMAT, pointList.get(0).getX()),             // X
                format(NUMBER_FORMAT, pointList.get(0).getY()),             // Y
                format(NUMBER_FORMAT, 0.2),                                 // Z
                G_CODE_UTILS.getMovement_speed(z),                    // F
                G_CODE_UTILS.getAAxisValue()                          // A
        ));

        for (int i = 1; i < pointList.size(); i++) {
            double aValueMed = Math.pow(pointList.get(i - 1).getX() - pointList.get(i).getX(), 2.0D) +
                    Math.pow(pointList.get(i - 1).getY() - pointList.get(i).getY(), 2.0D);
            double aValue = G_CODE_UTILS.getAAxisValue()
                    + Math.sqrt(aValueMed) * G_CODE_UTILS.getMaterialCount();

            str.append(format(G1Format,
                    format(NUMBER_FORMAT, pointList.get(i).getX()), // X
                    format(NUMBER_FORMAT, pointList.get(i).getY()), // Y
                    format(NUMBER_FORMAT, 0.2),                     // Z
                    G_CODE_UTILS.getMovement_speed(z),        // F
                    aValue                                          // A
            ));
            G_CODE_UTILS.setAAxisValue(aValue);
        }

        return str.append("\n").toString();
    }

    // Read addTail.gcode from ../templates/ and write to file.gcode.
    private void addTail() {
        try {
            FileWorker.write(pathToFile, FileWorker.read("templates/tail.gcode"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
