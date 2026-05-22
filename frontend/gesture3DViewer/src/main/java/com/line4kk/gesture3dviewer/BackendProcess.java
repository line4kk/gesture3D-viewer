package com.line4kk.gesture3dviewer;

import java.io.File;
import java.io.IOException;

public class BackendProcess {
    // Путь относительно user.dir
    private final String PATH = "..\\backend";
    private Process process;

    public BackendProcess() {
        try {
            File backendDir = new File(System.getProperty("user.dir"), PATH).getCanonicalFile();
            String pythonExe = new File(backendDir, "venv\\Scripts\\python.exe").getAbsolutePath();

            ProcessBuilder pb = new ProcessBuilder(
                    pythonExe,
                    "-m",
                    "src.main"
            ).inheritIO();

            pb.directory(backendDir);

            pb.redirectErrorStream(true);
            process = pb.start();

        } catch (IOException e) {

        }

    }

    public void stop() {
        if (process != null && process.isAlive())
            process.destroyForcibly();
    }
}
