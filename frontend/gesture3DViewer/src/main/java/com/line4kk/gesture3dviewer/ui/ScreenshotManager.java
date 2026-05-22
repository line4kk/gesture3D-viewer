package com.line4kk.gesture3dviewer.ui;

import com.line4kk.gesture3dviewer.ui.utils.UIChecks;
import com.line4kk.gesture3dviewer.model.AppPaths;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.SubScene;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class ScreenshotManager {
    private static final Path SCREENSHOT_DIR = AppPaths.resolveInAppBase("screenshots");
    private static final DateTimeFormatter FILE_NAME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss_SSS");

    private ScreenshotManager() {
    }

    public static Path saveSubSceneSnapshot(SubScene subScene, Color fill) {
        if (subScene == null) {
            return null;
        }

        try {
            Files.createDirectories(SCREENSHOT_DIR);

            SnapshotParameters parameters = new SnapshotParameters();
            parameters.setFill(fill == null ? Color.BLACK : fill);

            WritableImage image = subScene.snapshot(parameters, null);
            BufferedImage bufferedImage = toBufferedImage(image);

            Path file = buildScreenshotFilePath();
            ImageIO.write(bufferedImage, "png", file.toFile());
            return file;
        }
        catch (IOException e) {
            UIChecks.showError("Не удалось сохранить скриншот: " + e.getMessage());
            return null;
        }
    }

    public static boolean ensureScreenshotDirectory() {
        try {
            Files.createDirectories(SCREENSHOT_DIR);
            return true;
        }
        catch (IOException e) {
            UIChecks.showError("Не удалось подготовить папку для скриншотов: " + e.getMessage());
            return false;
        }
    }

    public static Path getScreenshotDirectory() {
        return SCREENSHOT_DIR;
    }

    private static Path buildScreenshotFilePath() {
        String fileName = "screenshot_" + LocalDateTime.now().format(FILE_NAME_FORMATTER) + ".png";
        return SCREENSHOT_DIR.resolve(fileName);
    }

    private static BufferedImage toBufferedImage(WritableImage image) {
        int width = (int) Math.max(1, Math.round(image.getWidth()));
        int height = (int) Math.max(1, Math.round(image.getHeight()));

        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        var pixelReader = image.getPixelReader();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                bufferedImage.setRGB(x, y, pixelReader.getArgb(x, y));
            }
        }

        return bufferedImage;
    }
}
