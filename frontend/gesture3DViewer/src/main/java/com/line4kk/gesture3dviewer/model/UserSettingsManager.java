package com.line4kk.gesture3dviewer.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.line4kk.gesture3dviewer.ui.utils.UIChecks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class UserSettingsManager {
    private static final ObjectMapper MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private static final Path SETTINGS_PATH = AppPaths.resolveInAppBase("user-settings.json");
    private static final Path LEGACY_SETTINGS_PATH = Path.of(System.getProperty("user.dir"), "user-settings.json");
    private static UserSettings currentSettings;

    private UserSettingsManager() {
    }

    public static UserSettings loadOrCreate() {
        UserSettings settings;

        if (Files.exists(SETTINGS_PATH)) {
            try {
                settings = MAPPER.readValue(SETTINGS_PATH.toFile(), UserSettings.class);
            }
            catch (IOException e) {
                settings = UserSettings.defaults();
                writeDefaultSettings(settings);
                apply(settings);
                return settings;
            }
        }
        else if (Files.exists(LEGACY_SETTINGS_PATH)) {
            try {
                settings = MAPPER.readValue(LEGACY_SETTINGS_PATH.toFile(), UserSettings.class);
                save(settings);
            }
            catch (IOException e) {
                settings = UserSettings.defaults();
                writeDefaultSettings(settings);
                apply(settings);
                currentSettings = settings;
                return settings;
            }
        } else {
            settings = UserSettings.defaults();
            writeDefaultSettings(settings);
        }

        apply(settings);
        currentSettings = settings;
        return settings;
    }

    public static void save(UserSettings settings) {
        if (settings == null) {
            settings = UserSettings.defaults();
        }

        currentSettings = settings;
        apply(settings);

        try {
            Files.createDirectories(SETTINGS_PATH.getParent());
            MAPPER.writeValue(SETTINGS_PATH.toFile(), settings);
        }
        catch (IOException e) {
            UIChecks.showError("Не удалось сохранить настройки: " + e.getMessage());
        }
    }

    public static UserSettings getCurrentSettings() {
        if (currentSettings == null) {
            currentSettings = loadOrCreate();
        }

        return currentSettings;
    }

    public static void saveCurrent() {
        save(currentSettings);
    }

    public static void saveCurrent(UserSettings settings) {
        save(settings);
    }

    public static Path getSettingsPath() {
        return SETTINGS_PATH;
    }

    private static void writeDefaultSettings(UserSettings settings) {
        save(settings);
    }

    private static void apply(UserSettings settings) {
        ViewerSettings.rotateSensitivity = settings.rotateSensitivity;
        ViewerSettings.cameraPanSensitivity = settings.cameraPanSensitivity;
        ViewerSettings.cameraScaleSensitivity = settings.cameraScaleSensitivity;
        ViewerSettings.initBoundingBox = settings.initBoundingBox;
        ViewerSettings.initCameraZ = settings.initCameraZ;
        ViewerSettings.lightingRangeCoefficient = settings.lightingRangeCoefficient;
        ViewerSettings.backgroundColor = settings.backgroundColor;
        ViewerSettings.chosenCameraInd = settings.chosenCameraInd;
    }
}
