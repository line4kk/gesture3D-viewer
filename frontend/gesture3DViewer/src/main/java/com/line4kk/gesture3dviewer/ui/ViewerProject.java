package com.line4kk.gesture3dviewer.ui;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.line4kk.gesture3dviewer.ui.models.ViewerProjectData;
import com.line4kk.gesture3dviewer.ui.utils.UIChecks;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class ViewerProject {
    private static final ObjectMapper MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    private final Path projectFullPath;
    private final String projectName;
    private final ViewerProjectData projectData;

    public ViewerProject(String projectPath, String projectName, ViewerProjectData projectData) {
        this.projectFullPath = Path.of(projectPath, projectName);
        this.projectName = projectName;
        this.projectData = projectData == null ? new ViewerProjectData() : projectData;
    }

    public ViewerProject(String projectPath, String projectName) {
        this(projectPath, projectName, new ViewerProjectData());
    }

    public boolean createDirectory() {
        try {
            if (Files.exists(projectFullPath)) {
                UIChecks.showError("Папка проекта уже существует: " + projectFullPath);
                return false;
            }

            Files.createDirectories(projectFullPath);
            return save(projectData);
        }
        catch (IOException e) {
            UIChecks.showError("Ошибка при создании проекта: " + e.getMessage());
            return false;
        }
    }

    public boolean save() {
        return save(projectData);
    }

    public boolean save(ViewerProjectData data) {
        try {
            Files.createDirectories(projectFullPath);
            MAPPER.writeValue(projectFullPath.resolve("config.json").toFile(), buildJson(data));
            return true;
        }
        catch (IOException e) {
            UIChecks.showError("Ошибка при сохранении проекта: " + e.getMessage());
            return false;
        }
    }

    public static ViewerProject open(Path projectPath) {
        try {
            JsonNode root = MAPPER.readTree(projectPath.resolve("config.json").toFile());
            String projectName = root.path("name").asText(projectPath.getFileName().toString());
            ViewerProjectData data = ViewerProjectData.fromJson(root.path("data"));

            Path parent = projectPath.getParent();
            String sourcePath = parent == null ? projectPath.toString() : parent.toString();
            return new ViewerProject(sourcePath, projectName, data);
        }
        catch (IOException e) {
            UIChecks.showError("Ошибка при открытии проекта: " + e.getMessage());
            return null;
        }
    }

    public ViewerProject saveAs(Path targetParent, String newProjectName, ViewerProjectData data) {
        Path destination = targetParent.resolve(newProjectName);

        try {
            if (Files.exists(destination)) {
                UIChecks.showError("Папка проекта уже существует: " + destination);
                return null;
            }

            copyDirectory(projectFullPath, destination);
            ViewerProject copiedProject = new ViewerProject(targetParent.toString(), newProjectName, data);
            if (copiedProject.save(data)) {
                return copiedProject;
            }
            return null;
        }
        catch (IOException e) {
            UIChecks.showError("Ошибка при сохранении проекта как: " + e.getMessage());
            return null;
        }
    }


    public Path getProjectFullPath() {
        return projectFullPath;
    }

    public String getProjectName() {
        return projectName;
    }

    public ViewerProjectData getProjectData() {
        return projectData;
    }

    private JsonNode buildJson(ViewerProjectData data) {
        ViewerProjectData safeData = data == null ? new ViewerProjectData() : data;

        var root = MAPPER.createObjectNode();
        root.put("name", projectName);

        var dataNode = root.putObject("data");
        var cameraNode = dataNode.putObject("camera");
        cameraNode.put("x", safeData.cameraX);
        cameraNode.put("y", safeData.cameraY);
        cameraNode.put("z", safeData.cameraZ);
        if (safeData.currentObjectPath != null) {
            dataNode.put("currentObjectPath", safeData.currentObjectPath);
        }

        var transform = safeData.modelSceneTransforms;
        var transformNode = dataNode.putObject("transform");
        transformNode.put("mxx", transform.getMxx());
        transformNode.put("mxy", transform.getMxy());
        transformNode.put("mxz", transform.getMxz());
        transformNode.put("tx", transform.getTx());
        transformNode.put("myx", transform.getMyx());
        transformNode.put("myy", transform.getMyy());
        transformNode.put("myz", transform.getMyz());
        transformNode.put("ty", transform.getTy());
        transformNode.put("mzx", transform.getMzx());
        transformNode.put("mzy", transform.getMzy());
        transformNode.put("mzz", transform.getMzz());
        transformNode.put("tz", transform.getTz());

        return root;
    }

    private void copyDirectory(Path source, Path destination) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path relative = source.relativize(dir);
                Files.createDirectories(destination.resolve(relative));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path relative = source.relativize(file);
                Files.copy(file, destination.resolve(relative));
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
