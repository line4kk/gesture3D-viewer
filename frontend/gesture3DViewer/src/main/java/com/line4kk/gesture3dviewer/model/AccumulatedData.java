package com.line4kk.gesture3dviewer.model;

public class AccumulatedData {
    private final double degreesX;
    private final double degreesY;
    private final double degreesZ;
    private final double dxCameraPan;
    private final double dyCameraPan;
    private final double deltaScale;
    private final boolean resetView;

    public AccumulatedData(double degreesX, double degreesY, double degreesZ, double dxCameraPan, double dyCameraPan, double deltaScale, boolean resetView) {
        this.degreesX = degreesX;
        this.degreesY = degreesY;
        this.degreesZ = degreesZ;
        this.dxCameraPan = dxCameraPan;
        this.dyCameraPan = dyCameraPan;
        this.deltaScale = deltaScale;
        this.resetView = resetView;
    }

    public double getDegreesY() {
        return degreesY;
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

    public double getDxCameraPan() {
        return dxCameraPan;
    }

    public boolean getResetView() {
        return resetView;
    }

}
