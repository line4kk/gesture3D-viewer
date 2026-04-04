package com.line4kk.gesture3dviewer.model;

public class AccumulatedData {
    private double degreesX;
    private double degreesY;
    private double degreesZ;
    private double deltaScale;

    public AccumulatedData(double degreesX, double degreesY, double degreesZ, double deltaScale) {
        this.degreesX = degreesX;
        this.degreesY = degreesY;
        this.degreesZ = degreesZ;
        this.deltaScale = deltaScale;
    }

    public double getDegreesY() {
        return degreesY;
    }

    public void setDegreesX(double degreesX) {
        this.degreesX = degreesX;
    }

    public void setDegreesY(double degreesY) {
        this.degreesY = degreesY;
    }

    public void setDegreesZ(double degreesZ) {
        this.degreesZ = degreesZ;
    }

    public void setDeltaScale(double deltaScale) {
        this.deltaScale = deltaScale;
    }

    public double getDegreesX() {
        return degreesX;
    }

    public double getDegreesZ() {
        return degreesZ;
    }

    public double getDeltaScale() {
        return deltaScale;
    }
}
