package com.line4kk.gesture3dviewer.ui.utils;

import javafx.scene.control.Alert;

import java.io.File;
import java.util.Set;

public class UIChecks {
    public static boolean isValidFolderName(String name) {

        if (name == null || name.isBlank()) {
            showError("Имя не может быть пустым");
            return false;
        }

        // запрещённые символы Windows
        if (name.chars().anyMatch(c ->
                c == '\\' || c == '/' || c == ':' ||
                        c == '*' || c == '?' || c == '"' ||
                        c == '<' || c == '>' || c == '|')) {
            System.out.println(name);
            showError("Недопустимые символы в имени");
            return false;
        }

        // нельзя системные имена
        Set<String> reserved = Set.of(
                "CON", "PRN", "AUX", "NUL",
                "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9",
                "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"
        );

        if (reserved.contains(name.toUpperCase())) {
            showError("Это системное имя Windows и его нельзя использовать");
            return false;
        }

        // нельзя заканчивать на точку или пробел
        if (name.endsWith(".") || name.endsWith(" ")) {
            showError("Имя не может заканчиваться точкой или пробелом");
            return false;
        }

        return true;
    }

    public static boolean isValidPath(String path) {

        if (path == null || path.isBlank()) {
            showError("Путь не указан");
            return false;
        }

        File dir = new File(path);

        // существует ли
        if (!dir.exists()) {
            showError("Путь не существует");
            return false;
        }

        // это папка
        if (!dir.isDirectory()) {
            showError("Путь для создания проекта должен указывать на папку");
            return false;
        }

        // можно ли писать
        if (!dir.canWrite()) {
            showError("Нет прав на запись в эту папку");
            return false;
        }

        return true;
    }

    public static void showError(String message) {

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }
}
