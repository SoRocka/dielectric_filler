package filler.entities;

public class Point {
    private double x;
    private double y;

    public static Point newCoord(double x, double y) {
        return new Point(x, y);
    }

    public Point() {
        this.x = 0;
        this.y = 0;
    }

    private Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void replace() {
        double buffer = x;
        this.x = this.y;
        this.y = buffer;
    }

    public void translate(Point point) {
        x += point.getX();
        y += point.getY();
    }

    public void translate(double x, double y) {
        this.x += x;
        this.y += y;
    }

    public void set(Point point) {
        x = point.getX();
        y = point.getY();
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }
}
