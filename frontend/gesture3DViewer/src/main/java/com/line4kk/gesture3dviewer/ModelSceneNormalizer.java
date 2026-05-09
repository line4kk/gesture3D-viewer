package com.line4kk.gesture3dviewer;

import com.line4kk.gesture3dviewer.model.ViewerSettings;
import javafx.scene.Group;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;


public class ModelSceneNormalizer {
    public static void normalize(Group modelScene) {
        BoundingBox bb = new BoundingBox(modelScene);

        if (bb.maxSize() == 0) return;

        double scale = ViewerSettings.initBoundingBox / bb.maxSize();
        double[] c = bb.getCenter();
        modelScene.getTransforms().addAll(
                new Rotate(180, Rotate.X_AXIS),        // 3. flip модели
                new Scale(scale, scale, scale),       // 2. масштаб
                new Translate(-c[0], -c[1], -c[2])   // 1. центр в 0
        );
    }
}
