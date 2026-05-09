package com.line4kk.gesture3dviewer;

import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.Assimp;

public class AssetLoader {
    public static AIScene loadAsset(String path) {
        return Assimp.aiImportFile(path,
                Assimp.aiProcess_Triangulate |
                Assimp.aiProcess_FlipUVs |
                Assimp.aiProcess_GenSmoothNormals |
                Assimp.aiProcess_JoinIdenticalVertices |
                Assimp.aiProcess_SortByPType);
    }
}
