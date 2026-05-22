package com.line4kk.gesture3dviewer.model;

import javafx.scene.paint.Color;

public class ViewerSettings {
    public static double rotateSensitivity = 300;
    public static double cameraPanSensitivity = 500;
    public static double cameraScaleSensitivity = 700;
    public static double initBoundingBox = 200;
    public static double initCameraZ = -450;
    public static double lightingRangeCoefficient = 8;  // 2-10
    public static String backgroundColor = "#78BFDE";

    public static Color getBackgroundColor() {
        return Color.web(backgroundColor);
    }
}
