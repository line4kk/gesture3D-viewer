package com.line4kk.gesture3dviewer.ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class AboutProjectController {

    @FXML private Label versionLabel;
    @FXML private Label authorLabel;

    @FXML
    public void initialize() {
        versionLabel.setText("0.1.0-alpha");
        authorLabel.setText("line4kk");
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) versionLabel.getScene().getWindow();
        stage.close();
    }
}