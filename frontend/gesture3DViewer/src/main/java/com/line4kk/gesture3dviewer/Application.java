package com.line4kk.gesture3dviewer;

import atlantafx.base.theme.CupertinoLight;
import com.line4kk.gesture3dviewer.model.AccumulatedData;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.lwjgl.assimp.AIScene;

import java.io.IOException;

public class Application extends javafx.application.Application {
    private static SceneController controller;
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("views/main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 800);
        stage.setTitle("Gesture3D Viewer");
        stage.setScene(scene);
        scene.getRoot().requestFocus();

        controller = fxmlLoader.getController();
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.RIGHT) {
                controller.rotateXYModelBy(0, -15);
            }
            if (event.getCode() == KeyCode.LEFT) {
                controller.rotateXYModelBy(0, 15);
            }
            if (event.getCode() == KeyCode.UP) {
                controller.rotateXYModelBy(-15, 0);
            }
            if (event.getCode() == KeyCode.DOWN) {
                controller.rotateXYModelBy(15, 0);
            }
            if (event.getCode() == KeyCode.D) {
                controller.rotateZModelBy(15);
            }
            if (event.getCode() == KeyCode.A) {
                controller.rotateZModelBy(-15);
            }
            if (event.getCode() == KeyCode.Y) {
                controller.moveCameraBy(0, -10);
            }
            if (event.getCode() == KeyCode.H) {
                controller.moveCameraBy(0, 10);

            }
            if (event.getCode() == KeyCode.G) {
                controller.moveCameraBy(-10, 0);
            }
            if (event.getCode() == KeyCode.J) {
                controller.moveCameraBy(10, 0);
            }
            if (event.getCode() == KeyCode.BACK_SPACE) {
                controller.resetView();
            }
            if (event.getCode() == KeyCode.SPACE) {
//                AIScene aiScene = AssetLoader.loadAsset("gesture3DViewer/src/main/resources/com/line4kk/gesture3dviewer/models/bugatti.obj");
//                AIScene aiScene = AssetLoader.loadAsset("gesture3DViewer/src/main/resources/com/line4kk/gesture3dviewer/models/model_1.obj");
//                AIScene aiScene = AssetLoader.loadAsset("gesture3DViewer/src/main/resources/com/line4kk/gesture3dviewer/models/banjofrog.obj");
                AIScene aiScene = AssetLoader.loadAsset("gesture3DViewer/src/main/resources/com/line4kk/gesture3dviewer/models/mcqueen/mcqueen.obj");
                Group modelScene = MeshConverter.convertScene(aiScene);
                controller.setModelScene(modelScene);
            }
            if (event.getCode() == KeyCode.ALT) {
                controller.removeModel();
            }

        });

        scene.setOnScroll(event -> {
            if (event.getDeltaY() > 0) {
                controller.changeCameraScaleBy(20);
            } else {
                controller.changeCameraScaleBy(-20);
            }
        });

        DataReceiver receiver = new DataReceiver();
        Thread thread = new Thread(receiver);
        thread.setDaemon(true);
        thread.start();

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long l) {
                AccumulatedData accumulatedData = receiver.getAccumulator().consume();
                if (accumulatedData.getDegreesX() != 0 || accumulatedData.getDegreesY() != 0) {
                    controller.rotateXYModelBy(accumulatedData.getDegreesX(), accumulatedData.getDegreesY());
                }
                if (accumulatedData.getDegreesZ() != 0) {
                    controller.rotateZModelBy(accumulatedData.getDegreesZ());
                }
                if (accumulatedData.getDxCameraPan() != 0 || accumulatedData.getDyCameraPan() != 0) {
                    controller.moveCameraBy(accumulatedData.getDxCameraPan(), accumulatedData.getDyCameraPan());
                }
                if (accumulatedData.getDeltaScale() != 0) {
                    controller.changeCameraScaleBy(accumulatedData.getDeltaScale());
                }
                if (accumulatedData.isResetView()) {
                    controller.resetView();
                }
                if (accumulatedData.isScreenshot()) {

                }

                controller.getVideoReceiver().tick();
            }
        };

        timer.start();

        Application.setUserAgentStylesheet(new CupertinoLight().getUserAgentStylesheet());

        stage.show();
        stage.setMaximized(true);
    }

    public static SceneController getController() {
        return controller;
    }
}
