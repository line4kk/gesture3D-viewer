package com.line4kk.gesture3dviewer;

import javafx.geometry.Bounds;
import javafx.scene.Group;

public class BoundingBox {
    private final double minX;
    private final double maxX;
    private final double minY;
    private final double maxY;
    private final double minZ;
    private final double maxZ;
    private double[] center;

    public BoundingBox(double minX, double maxX, double minY, double maxY, double minZ, double maxZ) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        this.minZ = minZ;
        this.maxZ = maxZ;
    }

    public BoundingBox(Group modelScene) {
        Bounds bound = modelScene.getBoundsInLocal();
        minX = bound.getMinX();
        maxX = bound.getMaxX();
        minY = bound.getMinY();
        maxY = bound.getMaxY();
        minZ = bound.getMinZ();
        maxZ = bound.getMaxZ();
    }

    public double[] getCenter() {
        if (center == null) {
            double[] centerPoint = new double[3];
            centerPoint[0] = (maxX + minX) / 2;
            centerPoint[1] = (maxY + minY) / 2;
            centerPoint[2] = (maxZ + minZ) / 2;
            center = centerPoint;
        }
        return center;
    }

    public double sizeX() {return maxX - minX;}
    public double sizeY() {return maxY - minY;}
    public double sizeZ() {return maxZ - minZ;}

    public double maxSize() {return Math.max(sizeX(), Math.max(sizeY(), sizeZ()));}

}
