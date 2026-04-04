package com.line4kk.gesture3dviewer;

import com.line4kk.gesture3dviewer.model.AccumulatedData;
import com.line4kk.gesture3dviewer.model.GestureMessage;
import com.line4kk.gesture3dviewer.model.SensitivitySettings;

public class Accumulator {
    private double degreesX = 0.0;
    private double degreesY = 0.0;
    private double degreesZ = 0.0;
    private double deltaScale = 0.0;

    public synchronized void accumulate(GestureMessage message) {
        switch (message.type) {
            case "rotate":
                degreesX += message.dy * SensitivitySettings.rotateSensitivity;
                degreesY += message.dx * SensitivitySettings.rotateSensitivity;
                break;
        }
    }

    public synchronized AccumulatedData consume() {
        AccumulatedData data = new AccumulatedData(degreesX, degreesY, degreesZ, deltaScale);
        degreesX = 0.0;
        degreesY = 0.0;
        degreesZ = 0.0;
        deltaScale = 0.0;
        return data;
    }

}
