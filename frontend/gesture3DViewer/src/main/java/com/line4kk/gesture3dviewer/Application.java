package com.line4kk.gesture3dviewer;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

import java.io.IOException;

public class Application extends javafx.application.Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 700, 400);
        stage.setTitle("Gesture3D Viewer");
        stage.setScene(scene);

        MainScene controller = fxmlLoader.getController();
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

        stage.show();
    }
}
