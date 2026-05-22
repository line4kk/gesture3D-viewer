package com.line4kk.gesture3dviewer.model;

import java.nio.file.Path;

public final class AppPaths {
    private static final String BASE_DIR_PROPERTY = "gesture3dviewer.baseDir";
    private static final String BASE_DIR_ENV = "GESTURE3DVIEWER_BASE_DIR";

    private AppPaths() {
    }

    public static Path getAppBaseDir() {
        String configuredBaseDir = System.getProperty(BASE_DIR_PROPERTY);
        if (configuredBaseDir == null || configuredBaseDir.isBlank()) {
            configuredBaseDir = System.getenv(BASE_DIR_ENV);
        }

        if (configuredBaseDir != null && !configuredBaseDir.isBlank()) {
            return Path.of(configuredBaseDir).toAbsolutePath().normalize();
        }

        return Path.of(System.getProperty("user.dir"));
    }

    public static Path resolveInAppBase(String first, String... more) {
        return getAppBaseDir().resolve(Path.of(first, more));
    }

}
