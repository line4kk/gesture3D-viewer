package com.line4kk.gesture3dviewer;

import com.github.sarxos.webcam.Webcam;
import com.line4kk.gesture3dviewer.model.ViewerSettings;
import com.line4kk.gesture3dviewer.model.UserSettings;
import com.line4kk.gesture3dviewer.model.UserSettingsManager;
import com.line4kk.gesture3dviewer.ui.FileTreeCell;
import com.line4kk.gesture3dviewer.ui.ScreenshotManager;
import com.line4kk.gesture3dviewer.ui.ProjectTreeBuilder;
import com.line4kk.gesture3dviewer.ui.UserVideoReceiver;
import com.line4kk.gesture3dviewer.ui.ViewerProject;
import com.line4kk.gesture3dviewer.ui.models.ViewerProjectData;
import com.line4kk.gesture3dviewer.ui.utils.UIChecks;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.stage.FileChooser;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.lwjgl.assimp.AIScene;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SceneController {

    @FXML
    private Pane mainScene;
    private Group world;
    private Group modelScene;
    private Camera camera;
    private SubScene scene3D;
    private Affine modelSceneTransforms;

    @FXML
    private TreeView<File> projectTree;
    @FXML
    private Button setModelSceneButton;
    @FXML
    private Button addFileToProjectButton;
    @FXML
    private Button deleteFileFromProjectButton;
    @FXML
    private Button reloadProjectTreeButton;
    @FXML
    private Button saveUserSettingsButton;
    @FXML
    private CheckBox showVideoCheckBox;
    @FXML
    private ImageView webcamView;
    @FXML
    private TextField rotateSensitivityField;
    @FXML
    private TextField cameraPanSensitivityField;
    @FXML
    private TextField cameraScaleSensitivityField;
    @FXML
    private TextField lightingRangeCoefficientField;
    @FXML
    private Slider rotateSensitivitySlider;
    @FXML
    private Slider cameraPanSensitivitySlider;
    @FXML
    private Slider cameraScaleSensitivitySlider;
    @FXML
    private Slider lightingRangeCoefficientSlider;
    @FXML
    private ColorPicker backgroundColorPicker;
    @FXML
    private ChoiceBox<String> choiceCamera;

    @FXML
    public Label objectFileLabel;
    @FXML
    public Label fpsLabel;
    @FXML
    public Label handsNumLabel;
    @FXML
    public Label currentPoseLabel;

    private ViewerProject currentProject = null;
    private boolean projectDirty = false;
    private boolean loadingProject = false;
    private boolean settingsDirty = false;
    private boolean settingsSyncing = false;
    private UserSettings currentUserSettings;
    private PointLight pointLight;
    private UserVideoReceiver videoReceiver;
    private Label screenshotToastLabel;
    private PauseTransition screenshotToastDelay;

    @FXML
    public void initialize() {
        // JavaFX вызывает автоматически
        world = new Group();  // "мир" на сцене - группа

        AmbientLight ambientLight = new AmbientLight(Color.color(0.5, 0.5, 0.5));
        pointLight = new PointLight();
        pointLight.setTranslateZ(-ViewerSettings.lightingRangeCoefficient * ViewerSettings.initBoundingBox);

        world.getChildren().addAll(pointLight, ambientLight);

        scene3D = new SubScene(world, 700, 400, true, SceneAntialiasing.BALANCED);

        camera = new PerspectiveCamera(true);
        camera.setTranslateZ(-450);
        scene3D.setCamera(camera);
        camera.setNearClip(0.1);
        camera.setFarClip(10000);

        scene3D.widthProperty().bind(mainScene.widthProperty());
        scene3D.heightProperty().bind(mainScene.heightProperty());
        scene3D.setFill(Color.web(ViewerSettings.backgroundColor));

        mainScene.getChildren().add(scene3D);

        projectTree.setCellFactory(tv -> new FileTreeCell());
        projectTree.getSelectionModel()
                .selectedItemProperty()
                .addListener((_, _, newItem) -> updateProjectActionButtons(newItem));
        projectTree.rootProperty()
                .addListener(obs -> reloadProjectTreeButton.setDisable(projectTree.getRoot() == null));
        projectTree.getSelectionModel()
                .selectedItemProperty()
                .addListener((_, _, newItem) -> updateProjectActionButtons(newItem));

        videoReceiver = new UserVideoReceiver(webcamView);
        currentUserSettings = UserSettingsManager.getCurrentSettings();
        initializeCameraList();
        initializeSettingsPane();
    }

    private void updateProjectActionButtons(TreeItem<File> selectedItem) {
        addFileToProjectButton.setDisable(selectedItem == null);

        boolean isObjFile = selectedItem != null
                && selectedItem.getValue().isFile()
                && selectedItem.getValue().getName().endsWith(".obj");
        setModelSceneButton.setDisable(!isObjFile);

        boolean isConfigFile = selectedItem != null
                && selectedItem.getValue().isFile()
                && selectedItem.getValue().getName().equals("config.json");
        deleteFileFromProjectButton.setDisable(isConfigFile || selectedItem == null);
    }

    private void initializeSettingsPane() {
        loadSettingsIntoControls(currentUserSettings);
        installNumericFilters();
        bindSettingsControls();
        saveUserSettingsButton.setDisable(true);
    }

    private void initializeCameraList() {
        List<Webcam> webcams = Webcam.getWebcams();

        for (Webcam webcam : webcams) {
            choiceCamera.getItems().add(webcam.getName().replaceAll("\\s+\\d+$", ""));
        }
        choiceCamera.setValue(choiceCamera.getItems().get(currentUserSettings.chosenCameraInd));

        new Thread(() -> {
            try {
                Thread.sleep(2000);
            } catch (Exception ignored) {}

            BackendRequester.send("SET_CAMERA " + choiceCamera.getValue());
        }).start();
    }

    private void installNumericFilters() {
        installNumericFilter(rotateSensitivityField);
        installNumericFilter(cameraPanSensitivityField);
        installNumericFilter(cameraScaleSensitivityField);
        installNumericFilter(lightingRangeCoefficientField);
    }

    private void markSettingsDirty() {
        saveUserSettingsButton.setDisable(false);
        settingsDirty = true;
    }

    private void clearSettingsDirty() {
        settingsDirty = false;
        saveUserSettingsButton.setDisable(true);
    }

    private void loadSettingsIntoControls(UserSettings settings) {
        settingsSyncing = true;
        try {
            rotateSensitivityField.setText(formatSetting(settings.rotateSensitivity));
            cameraPanSensitivityField.setText(formatSetting(settings.cameraPanSensitivity));
            cameraScaleSensitivityField.setText(formatSetting(settings.cameraScaleSensitivity));
            lightingRangeCoefficientField.setText(formatSetting(settings.lightingRangeCoefficient));

            rotateSensitivitySlider.setValue(settings.rotateSensitivity);
            cameraPanSensitivitySlider.setValue(settings.cameraPanSensitivity);
            cameraScaleSensitivitySlider.setValue(settings.cameraScaleSensitivity);
            lightingRangeCoefficientSlider.setValue(settings.lightingRangeCoefficient);

            backgroundColorPicker.setValue(Color.web(settings.backgroundColor));

            choiceCamera.setValue(choiceCamera.getItems().get(settings.chosenCameraInd));
            applyRuntimeSettings(settings);
        }
        finally {
            settingsSyncing = false;
        }
    }

    private void bindSettingsControls() {
        bindNumericSetting(rotateSensitivityField, rotateSensitivitySlider, value -> currentUserSettings.rotateSensitivity = value);
        bindNumericSetting(cameraPanSensitivityField, cameraPanSensitivitySlider, value -> currentUserSettings.cameraPanSensitivity = value);
        bindNumericSetting(cameraScaleSensitivityField, cameraScaleSensitivitySlider, value -> currentUserSettings.cameraScaleSensitivity = value);
        bindNumericSetting(lightingRangeCoefficientField, lightingRangeCoefficientSlider, value -> currentUserSettings.lightingRangeCoefficient = value);

        backgroundColorPicker.valueProperty().addListener((_, _, newValue) -> {
            if (settingsSyncing || newValue == null) {
                return;
            }

            currentUserSettings.backgroundColor = toHex(newValue);
            applyRuntimeSettings(currentUserSettings);
            markSettingsDirty();
        });

        choiceCamera.getSelectionModel().selectedIndexProperty().addListener(
                (_, _, newIndex) -> {
                    int selectedIndex = newIndex.intValue();
                    if (selectedIndex >= 0) {
                        System.out.println("Выбран элемент: " + selectedIndex);
                        currentUserSettings.chosenCameraInd = selectedIndex;
                        markSettingsDirty();
                        new Thread(() -> BackendRequester.send("SET_CAMERA " + choiceCamera.getValue())).start();

                    }
                }
        );
    }

    private void installNumericFilter(TextField field) {
        field.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.isEmpty() || newText.matches("-?\\d*(\\.\\d*)?")) {
                return change;
            }

            return null;
        }));
    }

    private void bindNumericSetting(TextField field, Slider slider, java.util.function.DoubleConsumer setter) {
        field.textProperty().addListener((_, _, newValue) -> {
            if (settingsSyncing) {
                return;
            }

            Double parsedValue = tryParseDouble(newValue);
            if (parsedValue == null) {
                return;
            }

            settingsSyncing = true;
            try {
                slider.setValue(parsedValue);
                setter.accept(parsedValue);
                applyRuntimeSettings(currentUserSettings);
                markSettingsDirty();
            }
            finally {
                settingsSyncing = false;
            }
        });

        slider.valueProperty().addListener((_, _, newValue) -> {
            if (settingsSyncing) {
                return;
            }

            settingsSyncing = true;
            try {
                double value = newValue.doubleValue();
                field.setText(formatSetting(value));
                setter.accept(value);
                applyRuntimeSettings(currentUserSettings);
                markSettingsDirty();
            }
            finally {
                settingsSyncing = false;
            }
        });
    }

    private Double tryParseDouble(String value) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return null;
            }

            return Double.parseDouble(value.trim());
        }
        catch (NumberFormatException e) {
            return null;
        }
    }

    private String formatSetting(double value) {
        if (Math.rint(value) == value) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }

    private void applyRuntimeSettings(UserSettings settings) {
        ViewerSettings.rotateSensitivity = settings.rotateSensitivity;
        ViewerSettings.cameraPanSensitivity = settings.cameraPanSensitivity;
        ViewerSettings.cameraScaleSensitivity = settings.cameraScaleSensitivity;
        ViewerSettings.lightingRangeCoefficient = settings.lightingRangeCoefficient;
        ViewerSettings.backgroundColor = settings.backgroundColor;
        ViewerSettings.chosenCameraInd = settings.chosenCameraInd;

        if (pointLight != null) {
            pointLight.setTranslateZ(-ViewerSettings.lightingRangeCoefficient * ViewerSettings.initBoundingBox);
        }

        if (scene3D != null) {
            scene3D.setFill(Color.web(ViewerSettings.backgroundColor));
        }
    }

    @FXML
    public void onSaveUserSettingsBtnClicked() {
        if (!syncCurrentSettingsFromControls()) {
            return;
        }

        UserSettingsManager.saveCurrent(currentUserSettings);
        clearSettingsDirty();
    }

    @FXML
    public void onResetUserSettingsBtnClicked() {
        currentUserSettings = UserSettings.defaults();
        loadSettingsIntoControls(currentUserSettings);
        markSettingsDirty();
    }

    private boolean syncCurrentSettingsFromControls() {
        Double rotateSensitivity = tryParseDouble(rotateSensitivityField.getText());
        Double cameraPanSensitivity = tryParseDouble(cameraPanSensitivityField.getText());
        Double cameraScaleSensitivity = tryParseDouble(cameraScaleSensitivityField.getText());
        Double lightingRangeCoefficient = tryParseDouble(lightingRangeCoefficientField.getText());

        if (rotateSensitivity == null
                || cameraPanSensitivity == null
                || cameraScaleSensitivity == null
                || lightingRangeCoefficient == null) {
            UIChecks.showError("Проверьте значения настроек.");
            return false;
        }

        currentUserSettings.rotateSensitivity = rotateSensitivity;
        currentUserSettings.cameraPanSensitivity = cameraPanSensitivity;
        currentUserSettings.cameraScaleSensitivity = cameraScaleSensitivity;
        currentUserSettings.lightingRangeCoefficient = lightingRangeCoefficient;
        currentUserSettings.backgroundColor = toHex(backgroundColorPicker.getValue());

        applyRuntimeSettings(currentUserSettings);
        return true;
    }

    private String toHex(Color color) {
        if (color == null) {
            return ViewerSettings.backgroundColor;
        }

        return String.format("#%02X%02X%02X",
                (int) Math.round(color.getRed() * 255),
                (int) Math.round(color.getGreen() * 255),
                (int) Math.round(color.getBlue() * 255));
    }

    public void setProject(ViewerProject project) {
        loadingProject = true;
        try {
            File projectFile = new File(project.getProjectFullPath().toUri());
            projectTree.setRoot(ProjectTreeBuilder.buildTree(projectFile));
            currentProject = project;
            applyProjectData(project.getProjectData());
            restoreLastOpenedObject(project);
            projectDirty = false;
        }
        finally {
            loadingProject = false;
        }
    }

    private boolean hasUnsavedChanges() {
        return currentProject != null && projectDirty;
    }

    private void markProjectDirty() {
        if (!loadingProject && currentProject != null) {
            projectDirty = true;
        }
    }

    private void clearProjectDirty() {
        projectDirty = false;
    }

    private boolean confirmProjectSwitch() {
        if (!hasUnsavedChanges()) {
            return true;
        }

        ButtonType saveButton = new ButtonType("Сохранить");
        ButtonType discardButton = new ButtonType("Не сохранять");
        ButtonType cancelButton = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);

        Alert alert = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Есть несохраненные изменения. Сохранить проект?",
                saveButton,
                discardButton,
                cancelButton
        );
        alert.setTitle("Есть несохраненные изменения. Сохранить проект?");
        alert.setHeaderText("Проект был изменен");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isEmpty() || result.get() == cancelButton) {
            return false;
        }

        if (result.get() == saveButton) {
            return saveCurrentProject();
        }

        return true;
    }

    public boolean confirmCloseIfNeeded() {
        if (!confirmProjectSwitch()) {
            return false;
        }

        if (!settingsDirty) {
            return true;
        }

        ButtonType saveButton = new ButtonType("Сохранить");
        ButtonType discardButton = new ButtonType("Не сохранять");
        ButtonType cancelButton = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);

        Alert alert = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Есть несохраненные изменения в настройках. Сохранить их?",
                saveButton,
                discardButton,
                cancelButton
        );
        alert.setTitle("Несохраненные настройки");
        alert.setHeaderText("Настройки были изменены");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isEmpty() || result.get() == cancelButton) {
            return false;
        }

        if (result.get() == saveButton) {
            if (!syncCurrentSettingsFromControls()) {
                return false;
            }

            UserSettingsManager.saveCurrent(currentUserSettings);
            clearSettingsDirty();
        }

        return true;
    }

    private boolean saveCurrentProject() {
        if (currentProject == null) {
            return true;
        }

        ViewerProjectData projectData = buildCurrentProjectData();
        if (!currentProject.save(projectData)) {
            return false;
        }

        syncProjectData(projectData);
        clearProjectDirty();
        return true;
    }

    private void syncProjectData(ViewerProjectData data) {
        if (currentProject == null || data == null) {
            return;
        }

        ViewerProjectData target = currentProject.getProjectData();
        target.cameraX = data.cameraX;
        target.cameraY = data.cameraY;
        target.cameraZ = data.cameraZ;
        target.modelSceneTransforms = data.modelSceneTransforms;
        target.currentObjectPath = data.currentObjectPath;
    }

    private void restoreLastOpenedObject(ViewerProject project) {
        if (project == null) {
            return;
        }

        ViewerProjectData data = project.getProjectData();
        if (data == null || data.currentObjectPath == null || data.currentObjectPath.isBlank()) {
            objectFileLabel.setText("не задан");
            return;
        }

        File modelFile = project.getProjectFullPath().resolve(data.currentObjectPath).toFile();
        if (!modelFile.isFile()) {
            objectFileLabel.setText("не задан");
            return;
        }

        loadingProject = true;
        try {
            openModelFile(modelFile, false);
            selectTreeItem(modelFile);
        }
        finally {
            loadingProject = false;
        }
    }

    private void openModelFile(File file, boolean markDirty) {
        AIScene aiScene = AssetLoader.loadAsset(file.getAbsolutePath());
        Group model = MeshConverter.convertScene(aiScene, file.toPath());

        setModelScene(model);
        objectFileLabel.setText(file.getName());

        if (currentProject != null) {
            currentProject.getProjectData().currentObjectPath = toProjectRelativePath(file);
        }

        if (markDirty) {
            markProjectDirty();
        }
    }

    private void selectTreeItem(File file) {
        if (projectTree.getRoot() == null) {
            return;
        }

        TreeItem<File> item = findTreeItem(projectTree.getRoot(), file);
        if (item != null) {
            projectTree.getSelectionModel().select(item);
        }
    }

    private TreeItem<File> findTreeItem(TreeItem<File> root, File file) {
        if (root == null || file == null) {
            return null;
        }

        File rootValue = root.getValue();
        if (rootValue != null && rootValue.equals(file)) {
            return root;
        }

        for (TreeItem<File> child : root.getChildren()) {
            TreeItem<File> found = findTreeItem(child, file);
            if (found != null) {
                return found;
            }
        }

        return null;
    }

    private String toProjectRelativePath(File file) {
        if (currentProject == null || file == null) {
            return null;
        }

        return currentProject.getProjectFullPath().relativize(file.toPath()).toString();
    }

    @FXML
    public void onCreateProjectBtnClicked(ActionEvent event) throws IOException {
        if (!confirmProjectSwitch()) {
            return;
        }

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/line4kk/gesture3dviewer/views/create-project-view.fxml")
        );

        Parent root = loader.load();

        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("О проекте");

        stage.initModality(Modality.APPLICATION_MODAL);
        stage.centerOnScreen();
        stage.setResizable(false);

        MenuItem item = (MenuItem) event.getSource();
        Window owner = item.getParentPopup().getOwnerWindow();

        stage.initOwner(owner);

        stage.showAndWait();
    }

    @FXML
    public void onOpenProjectBtnClicked(ActionEvent event) {
        if (!confirmProjectSwitch()) {
            return;
        }

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Выберите папку проекта");

        File chosenDir = directoryChooser.showDialog(projectTree.getScene().getWindow());
        if (chosenDir == null) {
            return;
        }

        ViewerProject project = ViewerProject.open(chosenDir.toPath());
        if (project != null) {
            setProject(project);
        }
    }

    @FXML
    public void onSaveProjectBtnClicked() {
        if (currentProject == null) {
            UIChecks.showError("Сначала создайте или откройте проект.");
            return;
        }

        saveCurrentProject();
    }

    @FXML
    public void onSaveProjectAsBtnClicked() {
        if (currentProject == null) {
            UIChecks.showError("Сначала создайте или откройте проект.");
            return;
        }

        TextInputDialog nameDialog = new TextInputDialog(currentProject.getProjectName());
        nameDialog.setTitle("Сохранить как");
        nameDialog.setHeaderText("Введите имя нового проекта");
        nameDialog.setContentText("Имя проекта:");

        Optional<String> nameResult = nameDialog.showAndWait();
        if (nameResult.isEmpty()) {
            return;
        }

        String projectName = nameResult.get().trim();
        if (projectName.isEmpty()) {
            UIChecks.showError("Имя проекта не может быть пустым.");
            return;
        }

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Выберите папку проекта");

        File selectedDir = directoryChooser.showDialog(projectTree.getScene().getWindow());
        if (selectedDir == null) {
            return;
        }

        ViewerProject savedProject = currentProject.saveAs(
                selectedDir.toPath(),
                projectName,
                buildCurrentProjectData()
        );

        if (savedProject != null) {
            setProject(savedProject);
        }
    }

    @FXML
    public void onAboutProjectBtnClicked(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/line4kk/gesture3dviewer/views/about-project-view.fxml")
        );

        Parent root = loader.load();

        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("О проекте");

        stage.initModality(Modality.APPLICATION_MODAL);
        stage.centerOnScreen();
        stage.setResizable(false);

        MenuItem item = (MenuItem) event.getSource();
        Window owner = item.getParentPopup().getOwnerWindow();

        stage.initOwner(owner);

        stage.showAndWait();
    }

    @FXML
    public void onExitBtnClicked() {
        if (confirmCloseIfNeeded()) {
            Platform.exit();
        }
    }

    public void captureScreenshot() {
        if (scene3D == null) {
            return;
        }

        if (!ScreenshotManager.ensureScreenshotDirectory()) {
            return;
        }

        if (ScreenshotManager.saveSubSceneSnapshot(scene3D, Color.web(ViewerSettings.backgroundColor)) != null) {
            showScreenshotToast("Скриншот сохранен");
        }
    }

    private void showScreenshotToast(String message) {
        if (mainScene == null) {
            return;
        }

        if (screenshotToastLabel == null) {
            screenshotToastLabel = new Label();
            screenshotToastLabel.setManaged(false);
            screenshotToastLabel.setMouseTransparent(true);
            screenshotToastLabel.setStyle(
                    "-fx-background-color: rgba(0, 0, 0, 0.70);" +
                    "-fx-text-fill: white;" +
                    "-fx-background-radius: 10;" +
                    "-fx-padding: 8 14 8 14;" +
                    "-fx-font-size: 13px;"
            );
            screenshotToastLabel.setOpacity(0);
            mainScene.getChildren().add(screenshotToastLabel);
        }

        screenshotToastLabel.setText(message);
        screenshotToastLabel.autosize();
        screenshotToastLabel.setLayoutX(16);
        screenshotToastLabel.setLayoutY(16);
        screenshotToastLabel.toFront();

        if (screenshotToastDelay != null) {
            screenshotToastDelay.stop();
        }

        screenshotToastLabel.setOpacity(0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(120), screenshotToastLabel);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        screenshotToastDelay = new PauseTransition(Duration.millis(700));
        screenshotToastDelay.setOnFinished(event -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(220), screenshotToastLabel);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.play();
        });

        fadeIn.setOnFinished(event -> screenshotToastDelay.playFromStart());
        fadeIn.playFromStart();
    }

    @FXML
    public void onSetModelSceneBtnClicked() {
        TreeItem<File> selectedItem = projectTree.getSelectionModel().getSelectedItem();
        if (selectedItem == null)
            return;

        File file = selectedItem.getValue();
        openModelFile(file, true);
    }

    @FXML
    public void onReloadProjectBtnClicked() {
        File currentProjectFile = projectTree.getRoot().getValue();
        projectTree.setRoot(ProjectTreeBuilder.buildTree(currentProjectFile));
    }

    @FXML
    public void onAddFileToProjectBtnClicked() {
        TreeItem<File> selectedItem = projectTree.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            return;
        }

        File targetDir = selectedItem.getValue().isDirectory()
                ? selectedItem.getValue()
                : selectedItem.getValue().getParentFile();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите файл для добавления в проект");

        List<File> chosenFiles = fileChooser.showOpenMultipleDialog(projectTree.getScene().getWindow());
        if (chosenFiles == null || chosenFiles.isEmpty()) {
            return;
        }

        List<File> addedFiles = new ArrayList<>();
        File fileToSelect = null;

        try {
            for (File chosenFile : chosenFiles) {
                File destination = new File(targetDir, chosenFile.getName());
                boolean existedBeforeCopy = destination.exists();

                if (existedBeforeCopy) {
                    Alert alert = new Alert(
                            Alert.AlertType.CONFIRMATION,
                            "Файл «" + chosenFile.getName() + "» уже существует. Заменить?",
                            ButtonType.OK,
                            ButtonType.CANCEL
                    );

                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isEmpty() || result.get() != ButtonType.OK) {
                        continue;
                    }
                }

                Files.copy(chosenFile.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);

                if (!existedBeforeCopy) {
                    addedFiles.add(destination);
                }

                if (destination.getName().toLowerCase().endsWith(".obj")) {
                    fileToSelect = destination;
                } else if (fileToSelect == null) {
                    fileToSelect = destination;
                }
            }

            if (!addedFiles.isEmpty()) {
                TreeItem<File> parentItem = selectedItem.getValue().isDirectory()
                        ? selectedItem
                        : selectedItem.getParent();

                for (File addedFile : addedFiles) {
                    TreeItem<File> newItem = new TreeItem<>(addedFile);
                    parentItem.getChildren().add(newItem);
                    parentItem.setExpanded(true);
                }
            }

            if (fileToSelect != null) {
                final File fileToSelectFinal = fileToSelect;
                Platform.runLater(() -> {
                    selectTreeItem(fileToSelectFinal);
                    updateProjectActionButtons(projectTree.getSelectionModel().getSelectedItem());
                });
            }
        }
        catch (IOException e) {
            UIChecks.showError("Не удалось скопировать файл: " + e.getMessage());
        }
    }

    @FXML
    public void onDeleteFileFromProjectBtnClicked() {
        TreeItem<File> selectedItem = projectTree.getSelectionModel().getSelectedItem();
        if (selectedItem == null) return;

        File file = selectedItem.getValue();

        if (selectedItem.getParent() == null) {
            UIChecks.showError("Нельзя удалить корневую папку проекта.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Переместить «" + file.getName() + "» в корзину?");
        alert.showAndWait().ifPresent(btn -> {
            if (btn != ButtonType.OK) return;

            boolean success = Desktop.getDesktop().moveToTrash(file);

            if (success) {
                selectedItem.getParent().getChildren().remove(selectedItem);
            } else {
                UIChecks.showError("Не удалось переместить файл в корзину.");
            }
        });
    }

    @FXML
    public void onShowVideoCheckBox() {
        if (showVideoCheckBox.isSelected()) {
            new Thread(() -> {
                String reply = BackendRequester.send("START_CAMERA");
                if ("OK".equals(reply)) {
                    videoReceiver.start();
                } else {
                    Platform.runLater(() -> showVideoCheckBox.setSelected(false));
                }
            }).start();
        } else {
            new Thread(() -> {
                BackendRequester.send("STOP_CAMERA");
                videoReceiver.stop();
            }).start();
        }
    }

    public UserVideoReceiver getVideoReceiver() {
        return videoReceiver;
    }


    public void rotateXYModelBy(double xAxis, double yAxis) {
        if (modelScene != null) {
            if (xAxis != 0)
                modelSceneTransforms.prepend(new Rotate(xAxis, Rotate.X_AXIS));
            if (yAxis != 0)
                modelSceneTransforms.prepend(new Rotate(yAxis, Rotate.Y_AXIS));
            markProjectDirty();
        }
    }

    public void rotateZModelBy(double zAxis) {
        if (modelScene != null) {
            if (zAxis != 0)
                modelSceneTransforms.prepend(new Rotate(zAxis, Rotate.Z_AXIS));
            markProjectDirty();
        }
    }

    public void moveCameraBy(double x, double y) {
        if (x != 0)
            camera.setTranslateX(camera.getTranslateX() + x);
        if (y != 0)
            camera.setTranslateY(camera.getTranslateY() + y);
        if (x != 0 || y != 0) {
            markProjectDirty();
        }
    }

    public void changeCameraScaleBy(double z) {
        if (z != 0)
            camera.setTranslateZ(camera.getTranslateZ() + z);
        if (z != 0) {
            markProjectDirty();
        }
    }

    public void resetView() {
        camera.setTranslateX(0);
        camera.setTranslateY(0);
        camera.setTranslateZ(-450);
        markProjectDirty();
    }

    public void setModelScene(Group scene) {
        if (modelScene != null) {
            removeModel();
        }

        modelScene = scene;
        ViewerProjectData projectData = currentProject == null ? null : currentProject.getProjectData();
        modelSceneTransforms = projectData != null && projectData.modelSceneTransforms != null
                ? new Affine(projectData.modelSceneTransforms)
                : new Affine();

        ModelSceneNormalizer.normalize(modelScene);
        modelScene.getTransforms().addFirst(modelSceneTransforms);
        world.getChildren().add(modelScene);
        markProjectDirty();
    }

    public void removeModel() {
        world.getChildren().remove(modelScene);
        modelScene = null;
        modelSceneTransforms = null;
        if (currentProject != null) {
            currentProject.getProjectData().currentObjectPath = null;
        }
        objectFileLabel.setText("не задан");
        markProjectDirty();
    }

    public double[] getCameraCoordinates() {
        return new double[] {camera.getTranslateX(), camera.getTranslateY(), camera.getTranslateZ()};
    }

    public Affine getModelSceneTransformsCopy() {
        if (modelSceneTransforms != null) {
            return new Affine(modelSceneTransforms);
        }

        ViewerProjectData projectData = currentProject == null ? null : currentProject.getProjectData();
        if (projectData != null && projectData.modelSceneTransforms != null) {
            return new Affine(projectData.modelSceneTransforms);
        }

        return new Affine();
    }

    private void applyProjectData(ViewerProjectData data) {
        if (data == null) {
            return;
        }

        camera.setTranslateX(data.cameraX);
        camera.setTranslateY(data.cameraY);
        camera.setTranslateZ(data.cameraZ);

        if (modelSceneTransforms != null && data.modelSceneTransforms != null) {
            modelSceneTransforms.setToTransform(data.modelSceneTransforms);
        }
    }

    private ViewerProjectData buildCurrentProjectData() {
        ViewerProjectData data = new ViewerProjectData(getCameraCoordinates(), getModelSceneTransformsCopy());
        if (currentProject != null) {
            data.currentObjectPath = currentProject.getProjectData().currentObjectPath;
        }
        return data;
    }
}

