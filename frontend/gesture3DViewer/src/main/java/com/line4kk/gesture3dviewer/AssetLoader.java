package com.line4kk.gesture3dviewer;

import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.Assimp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class AssetLoader {
    private static final int ASSIMP_FLAGS =
            Assimp.aiProcess_Triangulate |
            Assimp.aiProcess_FlipUVs |
            Assimp.aiProcess_GenSmoothNormals |
            Assimp.aiProcess_JoinIdenticalVertices |
            Assimp.aiProcess_SortByPType;

    public static AIScene loadAsset(String path) {
        return loadAsset(Path.of(path));
    }

    public static AIScene loadAsset(Path path) {
        if (path == null) {
            return null;
        }

        Path source = path.toAbsolutePath().normalize();
        Path preparedSource = ensureMaterialReference(source);
        return Assimp.aiImportFile(preparedSource.toString(), ASSIMP_FLAGS);
    }

    private static Path ensureMaterialReference(Path objPath) {
        try {
            List<String> lines = Files.readAllLines(objPath, StandardCharsets.UTF_8);
            Path referencedMaterial = findReferencedMaterial(objPath, lines);
            if (referencedMaterial != null) {
                return objPath;
            }

            Path materialPath = findSiblingMaterial(objPath);
            if (materialPath == null) {
                return objPath;
            }

            Path tempObj = Files.createTempFile("gesture3dviewer-", ".obj");
            tempObj.toFile().deleteOnExit();

            String mtllibLine = "mtllib " + materialPath.toAbsolutePath().normalize().toString().replace('\\', '/');
            StringBuilder content = new StringBuilder(mtllibLine).append(System.lineSeparator());
            for (String line : lines) {
                content.append(line).append(System.lineSeparator());
            }

            Files.writeString(tempObj, content.toString(), StandardCharsets.UTF_8);
            return tempObj;
        }
        catch (IOException e) {
            return objPath;
        }
    }

    private static Path findReferencedMaterial(Path objPath, List<String> lines) {
        Path objDirectory = objPath.getParent();
        if (objDirectory == null) {
            return null;
        }

        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.toLowerCase().startsWith("mtllib ")) {
                continue;
            }

            String materialReference = trimmed.substring("mtllib ".length()).trim();
            if (materialReference.startsWith("\"") && materialReference.endsWith("\"") && materialReference.length() > 1) {
                materialReference = materialReference.substring(1, materialReference.length() - 1);
            }

            Path resolved = objDirectory.resolve(materialReference).normalize();
            if (Files.isRegularFile(resolved)) {
                return resolved;
            }
        }

        return null;
    }

    private static Path findSiblingMaterial(Path objPath) throws IOException {
        Path siblingDir = objPath.getParent();
        if (siblingDir == null) {
            return null;
        }

        String fileName = objPath.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        String baseName = dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;

        Path sameName = siblingDir.resolve(baseName + ".mtl");
        if (Files.isRegularFile(sameName)) {
            return sameName;
        }

        try (var stream = Files.list(siblingDir)) {
            List<Path> mtls = stream
                    .filter(path -> path.getFileName().toString().toLowerCase().endsWith(".mtl"))
                    .toList();
            if (mtls.size() == 1) {
                return mtls.get(0);
            }
        }

        return null;
    }
}
