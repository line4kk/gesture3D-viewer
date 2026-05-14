package com.line4kk.gesture3dviewer.ui;

import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;

import java.io.File;

public class FileTreeCell extends TreeCell<File> {

    @Override
    protected void updateItem(File file, boolean empty) {
        super.updateItem(file, empty);

        if (empty || file == null) {
            setText(null);
            setGraphic(null);
            return;
        }

        setText(file.getName());

        // Иконки по типу файла
        String icon;
        if (file.isDirectory()) {
            icon = getTreeItem().isExpanded() ? "📂" : "📁";
        } else {
            icon = getIconForExtension(file.getName());
        }

        Label iconLabel = new Label(icon);
        setGraphic(iconLabel);
    }

    private String getIconForExtension(String name) {
        if (name.endsWith(".obj")) {
            return "🧊";
        } else if (name.endsWith(".png") || name.endsWith(".jpg")) {
            return "🖼";
        } else if (name.endsWith(".json") || name.endsWith(".xml")) {
            return "⚙️";
        }
        return "📄";
    }
}