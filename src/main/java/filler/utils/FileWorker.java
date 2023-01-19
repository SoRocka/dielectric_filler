package filler.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import static filler.Application.gui;
import static filler.utils.FillerUtils.EXTRUDER_TEMP_MARK;
import static filler.utils.FillerUtils.GCODE_FILE_EXTENTION;
import static filler.utils.FillerUtils.TABLE_TEMP_MARK;
import static filler.utils.FillerUtils.TEMP_ERROR_FORMAT;

public class FileWorker {

    public static void write(String fileName, String gcodeString) {
        try {
            if (!fileName.isEmpty()) {
                if (!fileName.endsWith(GCODE_FILE_EXTENTION)) {
                    fileName += GCODE_FILE_EXTENTION;
                }
            }
        } catch (Exception e) {
            gui.statusBar.setText("Error. File no found.");
            throw new RuntimeException(e);
        }
        File file = new File(fileName);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            PrintWriter output;
            if (gcodeString.contains("M136"))
                output = new PrintWriter(file.getAbsoluteFile());
            else
                output = new PrintWriter(new FileOutputStream(file.getAbsoluteFile(), true));

            try {
                output.print(gcodeString);
            } finally {
                output.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String read(String fileName) throws FileNotFoundException {
        StringBuilder stringBuilder = new StringBuilder();
        exists(fileName);
        File headerFile = new File(fileName);
        try {
            // Object for reading file to buffer
            BufferedReader input = new BufferedReader(new FileReader(headerFile.getAbsoluteFile()));
            try {
                // Reading file by string
                String string;
                while ((string = input.readLine()) != null) {
                    stringBuilder.append(string);
                    stringBuilder.append("\n");
                }
            } finally {
                input.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // Replace in header #<PARAMETER> by value of parameter
        if (fileName.contains("header")) {
            int index = stringBuilder.indexOf(TABLE_TEMP_MARK);
            if (index > -1)
                stringBuilder.replace(index, index + 6,
                        String.valueOf(ModelParameters.MODEL_SETTINGS.getTableTemp()));
            else
                gui.statusBar.setText(String.format(TEMP_ERROR_FORMAT, TABLE_TEMP_MARK));
            //
            index = stringBuilder.indexOf(EXTRUDER_TEMP_MARK);
            if (index > -1)
                stringBuilder.replace(index, index + 6,
                        String.valueOf(ModelParameters.MODEL_SETTINGS.getExtruderTemp()));
            else
                gui.statusBar.setText(String.format(TEMP_ERROR_FORMAT, EXTRUDER_TEMP_MARK));
        }
        // Return gotten text of file
        return stringBuilder.toString();
    }

    private static void exists(String fileName) throws FileNotFoundException {
        File file = new File(fileName);
        if (!file.exists()) {
            gui.statusBar.setText("Cannot find file: " + fileName);
            throw new FileNotFoundException(file.getName());
        }
    }

}
