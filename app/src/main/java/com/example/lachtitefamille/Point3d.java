package com.example.lachtitefamille;

public class Point3d {

    /* These are the three coordinates. */
    private double x; // x coordinate; no restrictions
    private double y; // y coordinate; no restrictions
    private double z; // z coordinate; no restrictions

    /**
     * Constructor:  a point at the origin
     * Precondition: None
     */
    public Point3d() {
        x = 0.0;
        y = 0.0;
        z = 0.0;
    }

    /**
     * Constructor: a point with given coordinates (x0,y0,z0)
     * Precondition: None
     */
    public Point3d(double x0, double y0, double z0) {
        x = x0;
        y = y0;
        z = z0;
    }

    /**
     * Yields: this Point's x coordinate
     */
    public double getX() {
        return x;
    }

    /**
     * Assigns value to x coordinate
     */
    public void setX(double value) {
        x = value;
    }

    /**
     * Yields: this Point's y coordinate
     */
    public double getY() {
        return y;
    }

    /**
     * Assigns value to y coordinate
     */
    public void setY(double value) {
        y = value;
    }

    /**
     * Yields: this Point's z coordinate
     */
    public double getZ() {
        return z;
    }

    /**
     * Assigns value to z coordinate
     */
    public void setZ(double value) {
        z = value;
    }

    /**
     * Yields: "at least one of the coordinates
     * of this point is 0"
     */
    public boolean hasAZero() {
        return x == 0.0 || y == 0.0 || z == 0.0;
    }

    public void normalize() {
        double dist = x*x + y*y +z*z;
        x/= Math.sqrt(dist);
        y/= Math.sqrt(dist);
        z/= Math.sqrt(dist);
    }

    /**
     * Yields: "at least one of the coordinates
     * of the point q is 0"
     */
    public static boolean hasAZero(Point3d q) {
        return q.x == 0.0 || q.y == 0.0 || q.z == 0.0;
    }

}
