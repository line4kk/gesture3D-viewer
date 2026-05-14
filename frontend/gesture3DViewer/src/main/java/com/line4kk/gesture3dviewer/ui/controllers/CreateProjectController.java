package com.line4kk.gesture3dviewer.ui.controllers;

import com.line4kk.gesture3dviewer.Application;
import com.line4kk.gesture3dviewer.ui.ViewerProject;
import com.line4kk.gesture3dviewer.ui.utils.UIChecks;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;


public class CreateProjectController {
    @FXML
    Button createButton;
    @FXML
    Button cancelButton;
    @FXML
    Button sourceButton;
    @FXML
    TextField pathField;
    @FXML
    TextField nameField;

    @FXML
    private void onCreateBtnClicked() {
        if (!UIChecks.isValidFolderName(nameField.getText())) return;

        if (!UIChecks.isValidPath(pathField.getText())) return;

        ViewerProject project = new ViewerProject(pathField.getText(), nameField.getText());
        project.createDirectory();
        Application.getController().setProject(project);
        closeWindow();
    }

    @FXML
    private void onSourceBtnClicked() {
        Stage stage = (Stage)sourceButton.getScene().getWindow();

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Выберите папку");
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        File selectedDir = directoryChooser.showDialog(stage);

        if (selectedDir != null) {
            System.out.println(selectedDir.getAbsolutePath());
            pathField.setText(selectedDir.getAbsolutePath());
        }
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
