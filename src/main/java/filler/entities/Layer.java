package filler.entities;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

import static filler.utils.ModelParameters.MODEL_SETTINGS;

public class Layer {
    // Layer coordinates
    private double coord_x;
    private double coord_y;

    // Layer widths
    private double widthX;
    private double widthY;

    // Counts of default cells
    private int counterX;
    private int counterY;

    // Layer border sizes
    private double borderX;
    private double borderY;

    // Layer default cell sizes
    private double cellSizeX;
    private double cellSizeY;

    private boolean rotated;
    private boolean reflected;

    private final int layerNumber;

    public Layer(int z, double stepX, double stepY) {
        layerNumber = z;

        coord_x = z * stepX / 2;
        coord_y = z * stepY / 2;

        widthX = MODEL_SETTINGS.getWidthX() - z * stepX;
        widthY = MODEL_SETTINGS.getWidthY() - z * stepY;

        cellSizeX = MODEL_SETTINGS.getDefaultCellSize();
        cellSizeY = MODEL_SETTINGS.getDefaultCellSize();

        counterX = (int) (widthX / cellSizeX);
        counterY = (int) (widthY / cellSizeY);

        borderX = (widthX - counterX * cellSizeX) / 2;
        borderY = (widthY - counterY * cellSizeY) / 2;
    }

    // Calculate Layer parameters (width, count of cells,
    // border size, cell default size) and save their.
    //
    // True - x-axis; False - y-axis;
    public void calculate(boolean axis) {
        double minimumCellSize = 2.8;
        double w;
        int count;
        double border;
        double cellSize;
        if (axis) {
            w = widthX;
            count = counterX;
            border = borderX;
            cellSize = cellSizeX;
        } else {
            w = widthY;
            count = counterY;
            border = borderY;
            cellSize = cellSizeY;
        }
        // NOT EVEN LAYER
        if (layerNumber % 2 == 1) {
            //- NOT EVEN CELLS COUNT
            if (count % 2 == 1) {
                if (count == 1) {
                    if (border < 1) {
                        // One cell without borders
                        border = w;
                    } else {
                        // Two cells (include borders)
                        count = 2;
                        border += cellSize / 2;
                    }
                } else {
                    if (count == 3 && border < 1 && border >= 0.2) {
                        cellSize = minimumCellSize;
                        count = 4;
                        border = (w - count * cellSize) / 2;
                        if (border != 0)
                            border += cellSize;
                    } else {
                        if (border < 1.8 && border >= 1) {
                            count += 1;
                            border += 0.5 * cellSize;
                        } else {
                            count += 1;
                            border += 0.5 * cellSize;
                        }
                    }
                }
            }
            //+ EVEN COUNT
            else {
                // no cells (width < default cell size)
                if (count == 0) {
                    count = 1;
                    border = 3.6;
                } else {
                    if (count == 2)
                        border = w / 2;
                    else {
                        if (border < 1.8 && border >= 1)
                            border += cellSize;
                        else {
                            count -= 1;
                            border += 1.5 * cellSize;
                        }
                    }
                }
            }
        }
        // EVEN LAYER
        else {
            //+ NO EVEN CELLS COUNT
            if (count % 2 == 1) {
                if (count == 1) {
                    if (border > 1) {
                        count = 2;
                        border += cellSize / 2;
                    } else {
                        border = w;
                    }
                } else {
                    if (count > 2) {
                        if (border != 0)
                            border += cellSize;
                    }
                }
            }
            //- EVEN CELLS COUNT
            else {
                if (count == 0) {
                    count = 1;
                    border = 3.6;
                } else {
                    if (count == 2) {
                        if (border != 0) {
                            if (border < 1.8 && border > 1) {
                                count += 1;
                                border += cellSize / 2;
                            } else { // border < 1
                                if (border >= 0.6) {
                                    cellSize = minimumCellSize;
                                    count = 3;
                                    border = (w - cellSize) / 2;
                                } else {
                                    border = w / 2;
                                }
                            }
                        }// else {without changes}
                    } else {
                        if (count > 3) {
                            if (border != 0) {
                                if (border < 1.8 && border > 1) {
                                    count += 1;
                                    border += 0.5 * cellSize;
                                } else {
                                    count -= 1;
                                    border += 1.5 * cellSize;
                                }
                            } else {
                                count -= 1;
                                border += 1.5 * cellSize;
                            }
                        }
                    }
                }
            }
        }
        if (axis) {
            counterX = count;
            borderX = border;
            cellSizeX = cellSize;
        } else {
            counterY = count;
            borderY = border;
            cellSizeY = cellSize;
        }
    }

    // Generate gcode string for layer
    public String getLayerGCode() {
        StringBuilder stringBuffer = new StringBuilder();
        if (borderX == 0) {
            borderX = cellSizeX;
        }
        if (borderY == 0) {
            borderY = cellSizeY;
        }
        boolean rotated_;
        // Replacing counterX & counterY
        for (int y = 0; y < counterY; y++) {
            rotated_ = (y % 2 == 0) == rotated;
            List<Point> pointList = new ArrayList<>();
            Cell currentCell = null;
            for (int x = 0; x < counterX; x++) {
                currentCell = calcCell(x, y);
                currentCell.setLayer_size(Point.newCoord(widthX, widthY));
                currentCell.calculateCoef(layerNumber);
                currentCell.calc_points(reflected, rotated_);
                currentCell.translate(- MODEL_SETTINGS.getWidthX() / 2,  - MODEL_SETTINGS.getWidthY() / 2);
                pointList.addAll(currentCell.getPoints());
                rotated_ = !rotated_;

            }
            if (y % 2 == 0) {
                stringBuffer.append(currentCell.createLayerTrace(pointList));
            } else {
                stringBuffer.append(currentCell.createLayerTrace(Lists.reverse(pointList)));
            }
        }
        return stringBuffer.toString();
    }

    private Cell calcCell(double x, double y) {
        Cell cell = new Cell();
        if (x == 0) {
            cell.setCoordX(0.0);
            cell.setSizesX(borderX);
        } else {
            if (x == counterX - 1 && x != 0) {
                cell.setCoordX(borderX + (x - 1) * cellSizeX);
                cell.setSizesX(borderX);
            } else {
                cell.setCoordX(borderX + (x - 1) * cellSizeX);
                cell.setSizesX(cellSizeX);
            }
        }
        if (y == 0) {
            cell.setCoordY(0.0);
            cell.setSizesY(borderY);
        } else {
            if (y == counterY - 1 && y != 0) {
                cell.setCoordY(borderY + (y - 1) * cellSizeY);
                cell.setSizesY(borderY);
            } else {
                cell.setCoordY(borderY + (y - 1) * cellSizeY);
                cell.setSizesY(cellSizeY);
            }
        }
        if (widthX < 3.6) {
            widthX = 3.6;
            coord_x = MODEL_SETTINGS.getWidthX() / 2 - 1.8;
        }
        if (widthY < 3.6) {
            widthY = 3.6;
            coord_y = MODEL_SETTINGS.getWidthY() / 2 - 1.8;
        }
        return cell;
    }

    public void setRotatedAndReflected(boolean reflected, boolean rotated) {
        this.reflected = reflected;
        this.rotated = rotated;
    }
}
