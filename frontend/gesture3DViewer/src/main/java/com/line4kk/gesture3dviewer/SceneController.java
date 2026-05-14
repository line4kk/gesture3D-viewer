package com.line4kk.gesture3dviewer;

import com.line4kk.gesture3dviewer.model.ViewerSettings;
import com.line4kk.gesture3dviewer.ui.FileTreeCell;
import com.line4kk.gesture3dviewer.ui.ProjectTreeBuilder;
import com.line4kk.gesture3dviewer.ui.UserVideoReceiver;
import com.line4kk.gesture3dviewer.ui.ViewerProject;
import com.line4kk.gesture3dviewer.ui.utils.UIChecks;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.lwjgl.assimp.AIScene;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

public class SceneController {

    @FXML
    private Pane mainScene;
    private Group world;
    private Group modelScene;
    private Camera camera;
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
    private CheckBox showVideoCheckBox;
    @FXML
    private ImageView webcamView;

    private UserVideoReceiver videoReceiver;

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

        projectTree.setCellFactory(tv -> new FileTreeCell());
        projectTree.getSelectionModel()
                .selectedItemProperty()
                .addListener((_, _, newItem) -> {
                    boolean isObjFile = newItem != null
                            && newItem.getValue().isFile()
                            && newItem.getValue().getName().endsWith(".obj");

                    setModelSceneButton.setDisable(!isObjFile);
                });
        projectTree.rootProperty()
                .addListener(obs -> reloadProjectTreeButton.setDisable(projectTree.getRoot() == null));
        projectTree.getSelectionModel()
                .selectedItemProperty()
                .addListener((_, _, newItem) -> addFileToProjectButton.setDisable(newItem == null));
        projectTree.getSelectionModel()
                .selectedItemProperty()
                .addListener((_, _, newItem) -> {
                    boolean isConfigFile = newItem != null
                            && newItem.getValue().isFile()
                            && newItem.getValue().getName().equals("config.json");

                    deleteFileFromProjectButton.setDisable(isConfigFile);
                });


        videoReceiver = new UserVideoReceiver(webcamView);
    }

    public void setProject(ViewerProject project) {
        File projectFile = new File(project.getProjectFullPath().toUri());
        projectTree.setRoot(ProjectTreeBuilder.buildTree(projectFile));
    }

    @FXML
    public void onCreateProjectBtnClicked(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/line4kk/gesture3dviewer/views/create-project-view.fxml")
        );

        Parent root = loader.load();

        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Создать проект");

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
        Platform.exit();
    }

    @FXML
    public void onSetModelSceneBtnClicked() {
        TreeItem<File> selectedItem = projectTree.getSelectionModel().getSelectedItem();
        if (selectedItem == null)
            return;

        File file = selectedItem.getValue();
        String path = file.getAbsolutePath();
        AIScene aiScene = AssetLoader.loadAsset(path);
        Group modelScene = MeshConverter.convertScene(aiScene);

        setModelScene(modelScene);
    }

    @FXML
    public void onReloadProjectBtnClicked() {
        File currentProjectFile = projectTree.getRoot().getValue();
        projectTree.setRoot(ProjectTreeBuilder.buildTree(currentProjectFile));
    }

    @FXML
    public void onAddFileToProjectBtnClicked() {
        // Получаем выбранный элемент в дереве
        TreeItem<File> selectedItem = projectTree.getSelectionModel().getSelectedItem();
        if (selectedItem == null) return;

        // Определяем целевую директорию
        // Если выбран файл — берём его родительскую папку
        File targetDir = selectedItem.getValue().isDirectory()
                ? selectedItem.getValue()
                : selectedItem.getValue().getParentFile();

        // Открываем проводник
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите файл для добавления в проект");

        File chosenFile = fileChooser.showOpenDialog(projectTree.getScene().getWindow());
        if (chosenFile == null) return; // пользователь закрыл проводник

        File destination = new File(targetDir, chosenFile.getName());

        // Если файл с таким именем уже существует
        if (destination.exists()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                    "Файл «" + chosenFile.getName() + "» уже существует. Заменить?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) return;
        }

        try {
            if (!destination.exists()) {
                // Если выбран файл — добавляем в родительскую папку, иначе в саму папку
                TreeItem<File> newItem = new TreeItem<>(destination);
                TreeItem<File> parentItem = selectedItem.getValue().isDirectory()
                        ? selectedItem
                        : selectedItem.getParent();

                parentItem.getChildren().add(newItem);
                parentItem.setExpanded(true);

                // Выделяем добавленный файл
                projectTree.getSelectionModel().select(newItem);
            }
            Files.copy(chosenFile.toPath(), destination.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);


        } catch (IOException e) {
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
        }
    }

    public void rotateZModelBy(double zAxis) {
        if (modelScene != null) {
            if (zAxis != 0)
                modelSceneTransforms.prepend(new Rotate(zAxis, Rotate.Z_AXIS));
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
        modelSceneTransforms = new Affine();

        ModelSceneNormalizer.normalize(modelScene);
        modelScene.getTransforms().addFirst(modelSceneTransforms);
        world.getChildren().add(modelScene);
    }

    public void removeModel() {
        world.getChildren().remove(modelScene);
    }

    public double[] getCameraCoordinates() {
        return new double[] {camera.getTranslateX(), camera.getTranslateY(), camera.getTranslateZ()};
    }

    public Affine getModelSceneTransformsCopy() {
        return new Affine(modelSceneTransforms);
    }
}
