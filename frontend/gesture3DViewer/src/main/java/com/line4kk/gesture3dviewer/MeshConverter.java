package com.line4kk.gesture3dviewer;

import javafx.scene.Group;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import org.lwjgl.assimp.*;
import javafx.scene.shape.TriangleMesh;

import java.nio.IntBuffer;

public class MeshConverter {
    
    private static Group convertMesh(AIMesh aiMesh) {
        Group outMesh = new Group();

        int vertexCount = aiMesh.mNumVertices();

        boolean hasUV =
                aiMesh.mTextureCoords(0) != null &&
                        aiMesh.mNumUVComponents(0) > 0;
        AIVector3D.Buffer uvBuf = aiMesh.mTextureCoords(0);


        float[] vertices = new float[vertexCount * 3];
        float[] texCoords = new float[vertexCount * 2];
        float[] normals = new float[vertexCount * 3];

        AIVector3D.Buffer nBuf = aiMesh.mNormals();
        boolean hasNormals = nBuf != null;
        float minX = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;
        float minZ = Float.MAX_VALUE;
        float maxZ = -Float.MAX_VALUE;

        for (int i = 0; i < vertexCount; i++) {
            // Vertices
            AIVector3D v = aiMesh.mVertices().get(i);
            vertices[i * 3]     = v.x();
            vertices[i * 3 + 1] = v.y();
            vertices[i * 3 + 2] = v.z();

            minX = Math.min(v.x(), minX);
            maxX = Math.max(v.x(), maxX);
            minY = Math.min(v.y(), minY);
            maxY = Math.max(v.y(), maxY);
            minZ = Math.min(v.z(), minZ);
            maxZ = Math.max(v.z(), maxZ);

            // Textures
            if (hasUV && uvBuf != null && i < uvBuf.remaining()) {
                AIVector3D uv = uvBuf.get(i);

                texCoords[i * 2]     = uv.x();
                texCoords[i * 2 + 1] = uv.y();
            } else {
                // fallback textures
                texCoords[i * 2]     = 0;
                texCoords[i * 2 + 1] = 0;
            }
            // Normals
            if (hasNormals) {
                AIVector3D n = nBuf.get(i);

                normals[i * 3]     = n.x();
                normals[i * 3 + 1] = n.y();
                normals[i * 3 + 2] = n.z();
            } else {
                // fallback normals
                normals[i * 3] = 0;
                normals[i * 3 + 1] = 1;
                normals[i * 3 + 2] = 0;
            }
        }
        BoundingBox bb = new BoundingBox(minX, maxX, minY, maxY, minZ, maxZ);

        int types = aiMesh.mPrimitiveTypes();
        boolean isLine = (types & Assimp.aiPrimitiveType_LINE) != 0;
        boolean isTriangle = (types & Assimp.aiPrimitiveType_TRIANGLE) != 0;


        if (isTriangle) {
            TriangleMesh triangleMesh = new TriangleMesh();
            triangleMesh.getPoints().addAll(vertices);
            triangleMesh.getTexCoords().addAll(texCoords);
            triangleMesh.getNormals().addAll(normals);

            for (int i = 0; i < aiMesh.mNumFaces(); i++) {

                AIFace face = aiMesh.mFaces().get(i);
                IntBuffer idx = face.mIndices();

                int[] faces = new int[6];

                int i0 = idx.get(0);
                int i1 = idx.get(1);
                int i2 = idx.get(2);

                faces[0] = i0; faces[1] = i0;
                faces[2] = i1; faces[3] = i1;
                faces[4] = i2; faces[5] = i2;

                triangleMesh.getFaces().addAll(faces);
            }

            MeshView meshView  = new MeshView(triangleMesh);
            meshView.setCullFace(CullFace.NONE);
            outMesh.getChildren().add(meshView);

        }
        else if (isLine) {
            LineMeshFabric lineMeshFabric = new LineMeshFabric(aiMesh.mVertices(), bb.maxSize());
            for (int i = 0; i < aiMesh.mNumFaces(); i++) {
                AIFace face = aiMesh.mFaces().get(i);
                Group lineMesh = lineMeshFabric.create(face);
                outMesh.getChildren().add(lineMesh);
            }
        }

        return outMesh;
    }

    public static Group convertScene(AIScene scene) {
        // Получить группу всей сцены модели

        Group modelScene = new Group();

        if (scene == null || scene.mMeshes() == null) {
            return modelScene;
        }

        for (int i = 0; i < scene.mNumMeshes(); i++) {
            AIMesh aiMesh = AIMesh.create(scene.mMeshes().get(i));
            Group mesh = convertMesh(aiMesh);
            modelScene.getChildren().add(mesh);
        }

        return modelScene;
    }
}
