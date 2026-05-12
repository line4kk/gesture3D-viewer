package com.line4kk.gesture3dviewer;

import com.line4kk.gesture3dviewer.model.ViewerSettings;
import javafx.fxml.FXML;
import javafx.scene.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;

public class SceneController {

    @FXML
    private Pane mainScene;
    private Group world;
    private Group modelScene;
    private Camera camera;

    @FXML
    public void initialize() {
        // JavaFX вызовет автоматически
        world = new Group();  // "мир" на сцене - группа

        AmbientLight ambientLight = new AmbientLight(Color.color(0.5, 0.5, 0.5));
        PointLight pointLight = new PointLight();
        pointLight.setTranslateZ(-ViewerSettings.lightingRangeCoefficient * ViewerSettings.initBoundingBox);

        world.getChildren().addAll(pointLight, ambientLight);

        SubScene scene3D = new SubScene(world, 700, 400, true, SceneAntialiasing.BALANCED);

        camera = new PerspectiveCamera(true);
        camera.setTranslateZ(-450);
        scene3D.setCamera(camera);
        camera.setNearClip(0.1);
        camera.setFarClip(10000);

        scene3D.widthProperty().bind(mainScene.widthProperty());
        scene3D.heightProperty().bind(mainScene.heightProperty());
        scene3D.setFill(Color.rgb(120, 191, 222));

        mainScene.getChildren().add(scene3D);
    }

    public void rotateXYModelBy(double xAxis, double yAxis) {
        if (modelScene != null) {
            if (xAxis != 0)
                modelScene.getTransforms().addFirst(new Rotate(xAxis, Rotate.X_AXIS));
            if (yAxis != 0)
                modelScene.getTransforms().addFirst(new Rotate(yAxis, Rotate.Y_AXIS));
        }
    }

    public void rotateZModelBy(double zAxis) {
        if (modelScene != null) {
            if (zAxis != 0)
                modelScene.getTransforms().addFirst(new Rotate(zAxis, Rotate.Z_AXIS));
        }
    }

    public void moveCameraBy(double x, double y) {
        if (x != 0)
            camera.setTranslateX(camera.getTranslateX() + x);
        if (y != 0)
            camera.setTranslateY(camera.getTranslateY() + y);
    }

    public void changeCameraScaleBy(double z) {
        if (z != 0)
            camera.setTranslateZ(camera.getTranslateZ() + z);
    }

    public void resetView() {
        camera.setTranslateX(0);
        camera.setTranslateY(0);
        camera.setTranslateZ(-450);
    }

    public void setModelScene(Group scene) {
        if (modelScene != null) {
            removeModel();
        }

        modelScene = scene;
        ModelSceneNormalizer.normalize(modelScene);
        world.getChildren().add(modelScene);
    }

    public void removeModel() {
        world.getChildren().remove(modelScene);
    }
}
