package com.line4kk.gesture3dviewer;

import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;
import javafx.scene.shape.TriangleMesh;

import java.nio.IntBuffer;

public class MeshConverter {
    
    private static Group convertMesh(AIMesh aiMesh, PointerBuffer materials) {
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

            AIMaterial aiMaterial = AIMaterial.create(materials.get(aiMesh.mMaterialIndex()));
            PhongMaterial phongMaterial = convertMaterial(aiMaterial);
            meshView.setMaterial(phongMaterial);

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

    private static PhongMaterial convertMaterial(AIMaterial aiMaterial) {

        PhongMaterial material = new PhongMaterial();

        // 1. DIFFUSE COLOR
        AIColor4D diffuse = AIColor4D.create();
        int result = Assimp.aiGetMaterialColor(
                aiMaterial,
                Assimp.AI_MATKEY_COLOR_DIFFUSE,
                Assimp.aiTextureType_NONE,
                0,
                diffuse
        );

        if (result == 0) {
            material.setDiffuseColor(
                    javafx.scene.paint.Color.color(
                            diffuse.r(),
                            diffuse.g(),
                            diffuse.b(),
                            diffuse.a()
                    )
            );
        }

        // 2. DIFFUSE TEXTURE
        AIString path = AIString.calloc();

        if (Assimp.aiGetMaterialTexture(
                aiMaterial,
                Assimp.aiTextureType_DIFFUSE,
                0,
                path,
                (IntBuffer) null,
                null,
                null,
                null,
                null,
                null
        ) == 0) {

            String texPath = path.dataString();
            if (!texPath.isBlank()) {
                try {
                    java.io.File file = new java.io.File("gesture3DViewer/src/main/resources/com/line4kk/gesture3dviewer/models/mcqueen/" + texPath);
                    Image img = new Image(file.toURI().toString());
                    material.setDiffuseMap(img);

                } catch (Exception ignored) {}
            }
        }

        // 3. SPECULAR COLOR
        AIColor4D specular = AIColor4D.create();

        if (Assimp.aiGetMaterialColor(
                aiMaterial,
                Assimp.AI_MATKEY_COLOR_SPECULAR,
                Assimp.aiTextureType_NONE,
                0,
                specular
        ) == 0) {

            material.setSpecularColor(
                    javafx.scene.paint.Color.color(
                            specular.r(),
                            specular.g(),
                            specular.b()
                    )
            );
        }

        // 4. SHININESS
        float[] shininess = new float[1];
        int[] count = new int[1];

        if (Assimp.aiGetMaterialFloatArray(
                aiMaterial,
                Assimp.AI_MATKEY_SHININESS,
                Assimp.aiTextureType_NONE,
                0,
                shininess,
                count
        ) == 0 && count[0] > 0) {

            material.setSpecularPower(shininess[0]);
        }

        // 5. OPACITY
        float[] opacity = new float[1];

        if (Assimp.aiGetMaterialFloatArray(
                aiMaterial,
                Assimp.AI_MATKEY_OPACITY,
                Assimp.aiTextureType_NONE,
                0,
                opacity,
                count
        ) == 0 && count[0] > 0) {

            double o = opacity[0];

            material.setDiffuseColor(
                    material.getDiffuseColor().deriveColor(
                            0, 1, 1, o
                    )
            );
        }

        // 6. NORMAL MAP (BUMP)
        AIString normalPath = AIString.calloc();

        if (Assimp.aiGetMaterialTexture(
                aiMaterial,
                Assimp.aiTextureType_NORMALS,
                0,
                normalPath,
                (IntBuffer) null,
                null,
                null,
                null,
                null,
                null
        ) == 0) {

            String texPath = normalPath.dataString();

            if (!texPath.isBlank()) {
                try {
                    java.io.File file = new java.io.File("gesture3DViewer/src/main/resources/com/line4kk/gesture3dviewer/models/mcqueen/" + texPath);
                    Image img = new Image(file.toURI().toString());

                    material.setBumpMap(img);

                } catch (Exception ignored) {}
            }
        }

        // 7. EMISSIVE
        AIColor4D emissive = AIColor4D.create();

        if (Assimp.aiGetMaterialColor(
                aiMaterial,
                Assimp.AI_MATKEY_COLOR_EMISSIVE,
                Assimp.aiTextureType_NONE,
                0,
                emissive
        ) == 0) {

            material.setSelfIlluminationMap(
                    new javafx.scene.image.WritableImage(1, 1)
            );
        }

        return material;
    }

    public static Group convertScene(AIScene scene) {
        // Получить группу всей сцены модели

        Group modelScene = new Group();

        if (scene == null || scene.mMeshes() == null) {
            return modelScene;
        }

        for (int i = 0; i < scene.mNumMeshes(); i++) {
            AIMesh aiMesh = AIMesh.create(scene.mMeshes().get(i));
            Group mesh = convertMesh(aiMesh, scene.mMaterials());
            modelScene.getChildren().add(mesh);
        }

        return modelScene;
    }
}
