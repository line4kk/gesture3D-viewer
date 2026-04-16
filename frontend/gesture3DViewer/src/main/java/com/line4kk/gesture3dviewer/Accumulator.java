package com.line4kk.gesture3dviewer;

import com.line4kk.gesture3dviewer.model.AccumulatedData;
import com.line4kk.gesture3dviewer.model.GestureMessage;
import com.line4kk.gesture3dviewer.model.SensitivitySettings;

public class Accumulator {
    private double degreesX = 0.0;
    private double degreesY = 0.0;
    private double degreesZ = 0.0;
    private double dxCameraPan = 0.0;
    private double dyCameraPan = 0.0;
    private double deltaScale = 0.0;
    private boolean resetView = false;
    private boolean screenshot = false;

    public synchronized void accumulate(GestureMessage message) {
        switch (message.type) {
            case "rotate":
                degreesX += message.dy * SensitivitySettings.rotateSensitivity;
                degreesY += message.dx * SensitivitySettings.rotateSensitivity;
                break;
            case "rotate_z":
                degreesZ += message.dz * SensitivitySettings.rotateSensitivity;
                break;
            case "camera_pan":
                dxCameraPan += message.dx * SensitivitySettings.cameraPanSensitivity;
                dyCameraPan += message.dy * -SensitivitySettings.cameraPanSensitivity;
                break;
            case "camera_scale":
                deltaScale += message.dr * SensitivitySettings.cameraScaleSensitivity;
                break;
            case "reset_view":
                resetView = true;
                break;
            case "screenshot":
                screenshot = true;
                break;
        }
    }

    public synchronized AccumulatedData consume() {
        AccumulatedData data = new AccumulatedData(degreesX, degreesY, degreesZ, dxCameraPan, dyCameraPan, deltaScale, resetView, screenshot);
        degreesX = 0.0;
        degreesY = 0.0;
        degreesZ = 0.0;
        dxCameraPan = 0.0;
        dyCameraPan = 0.0;
        deltaScale = 0.0;
        resetView = false;
        screenshot = false;
        return data;
    }

}
