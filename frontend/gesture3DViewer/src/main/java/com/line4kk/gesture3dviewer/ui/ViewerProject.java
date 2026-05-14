package com.line4kk.gesture3dviewer.ui;

import com.line4kk.gesture3dviewer.ui.models.ViewerProjectData;
import com.line4kk.gesture3dviewer.ui.utils.UIChecks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ViewerProject {
    private final Path projectFullPath;
    private final String projectName;
    private final ViewerProjectData projectData;

    public ViewerProject(String projectPath, String projectName, ViewerProjectData projectData) {
        this.projectFullPath = Path.of(projectPath, projectName);
        this.projectName = projectName;
        this.projectData = projectData;
    }

    public ViewerProject(String projectPath, String projectName) {
        this.projectFullPath = Path.of(projectPath, projectName);
        this.projectName = projectName;
        this.projectData = new ViewerProjectData();
    }

    public void createDirectory() {
        try {
            Files.createDirectory(projectFullPath);
            Files.createFile(projectFullPath.resolve("config.json"));
            Files.writeString(projectFullPath.resolve("config.json"), toJson());
        }
        catch (IOException e) {
            UIChecks.showError("Ошибка при создании проекта: " + e);
        }
    }

    public String toJson() {
        return "{\"name\": " + projectName
                + ", \"data\": " + projectData.toJson() + "}";
    }

    public Path getProjectFullPath() {
        return projectFullPath;
    }
}
