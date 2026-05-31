package com.line4kk.gesture3dviewer;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class BackendProcess {
    private Process process;

    public BackendProcess() {
        try {
            File backendDir = resolveBackendDir();
            ProcessBuilder pb = buildProcess(backendDir).inheritIO();
            pb.redirectErrorStream(true);
            process = pb.start();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to start backend process", e);
        }
    }

    private ProcessBuilder buildProcess(File backendDir) {
        File onedirExe = new File(backendDir, "backend.exe");
        if (onedirExe.isFile()) {
            return new ProcessBuilder(onedirExe.getAbsolutePath());
        }

        String pythonExe = new File(backendDir, "venv\\Scripts\\python.exe").getAbsolutePath();
        ProcessBuilder pb = new ProcessBuilder(
                pythonExe,
                "-m",
                "src.main"
        );
        pb.directory(backendDir);
        return pb;
    }

    private File resolveBackendDir() throws IOException {
        File codeSourceLocation = getCodeSourceLocation();
        if (codeSourceLocation.isFile() && codeSourceLocation.getName().endsWith(".jar")) {
            return new File(codeSourceLocation.getParentFile(), "backend").getCanonicalFile();
        }

        return new File(System.getProperty("user.dir"), "..\\backend").getCanonicalFile();
    }

    private File getCodeSourceLocation() throws IOException {
        try {
            return new File(BackendProcess.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getCanonicalFile();
        } catch (URISyntaxException e) {
            throw new IOException("Failed to resolve application location", e);
        }
    }

    public void stop() {
        if (process != null && process.isAlive()) {
            process.destroyForcibly();
        }
    }
}
