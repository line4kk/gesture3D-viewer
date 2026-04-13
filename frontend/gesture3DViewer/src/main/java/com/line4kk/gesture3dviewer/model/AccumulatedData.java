package com.line4kk.gesture3dviewer.model;

public class AccumulatedData {
    private double degreesX;
    private double degreesY;
    private double degreesZ;
    private double dxCameraPan;
    private double dyCameraPan;
    private double deltaScale;

    public AccumulatedData(double degreesX, double degreesY, double degreesZ, double dxCameraPan, double dyCameraPan, double deltaScale) {
        this.degreesX = degreesX;
        this.degreesY = degreesY;
        this.degreesZ = degreesZ;
        this.dxCameraPan = dxCameraPan;
        this.dyCameraPan = dyCameraPan;
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

    public double getDyCameraPan() {
        return dyCameraPan;
    }

    public void setDyCameraPan(double dyCameraPan) {
        this.dyCameraPan = dyCameraPan;
    }

    public double getDxCameraPan() {
        return dxCameraPan;
    }

    public void setDxCameraPan(double dxCameraPan) {
        this.dxCameraPan = dxCameraPan;
    }
}
