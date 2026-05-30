package com.line4kk.gesture3dviewer.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserSettings {
    public static final double DEFAULT_ROTATE_SENSITIVITY = 300;
    public static final double DEFAULT_CAMERA_PAN_SENSITIVITY = 500;
    public static final double DEFAULT_CAMERA_SCALE_SENSITIVITY = 700;
    public static final double DEFAULT_INIT_BOUNDING_BOX = 200;
    public static final double DEFAULT_INIT_CAMERA_Z = -450;
    public static final double DEFAULT_LIGHTING_RANGE_COEFFICIENT = 8;
    public static final String DEFAULT_BACKGROUND_COLOR = "#78BFDE";
    public static final int DEFAULT_CHOSEN_CAMERA_IND = 0;

    public double rotateSensitivity = ViewerSettings.rotateSensitivity;
    public double cameraPanSensitivity = ViewerSettings.cameraPanSensitivity;
    public double cameraScaleSensitivity = ViewerSettings.cameraScaleSensitivity;
    public double initBoundingBox = ViewerSettings.initBoundingBox;
    public double initCameraZ = ViewerSettings.initCameraZ;
    public double lightingRangeCoefficient = ViewerSettings.lightingRangeCoefficient;
    public String backgroundColor = ViewerSettings.backgroundColor;
    public int chosenCameraInd = ViewerSettings.chosenCameraInd;

    public static UserSettings defaults() {
        UserSettings settings = new UserSettings();
        settings.rotateSensitivity = DEFAULT_ROTATE_SENSITIVITY;
        settings.cameraPanSensitivity = DEFAULT_CAMERA_PAN_SENSITIVITY;
        settings.cameraScaleSensitivity = DEFAULT_CAMERA_SCALE_SENSITIVITY;
        settings.initBoundingBox = DEFAULT_INIT_BOUNDING_BOX;
        settings.initCameraZ = DEFAULT_INIT_CAMERA_Z;
        settings.lightingRangeCoefficient = DEFAULT_LIGHTING_RANGE_COEFFICIENT;
        settings.backgroundColor = DEFAULT_BACKGROUND_COLOR;
        settings.chosenCameraInd = DEFAULT_CHOSEN_CAMERA_IND;
        return settings;
    }
}
