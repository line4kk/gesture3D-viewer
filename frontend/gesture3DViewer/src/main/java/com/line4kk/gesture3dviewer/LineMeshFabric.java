package com.line4kk.gesture3dviewer;

import javafx.scene.Group;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import org.lwjgl.assimp.AIFace;
import org.lwjgl.assimp.AIVector3D;

import java.nio.IntBuffer;

public class LineMeshFabric {
    private final double THICKNESS = 0.001;
    private final double modelSize;
    private final AIVector3D.Buffer vertices;

    public LineMeshFabric(AIVector3D.Buffer vertices, double modelSize) {
        this.vertices = vertices;
        this.modelSize = modelSize;
    }

    public Group create(AIFace face) {
        Group lineMesh = new Group();
        if (face.mNumIndices() < 2) return lineMesh;

        IntBuffer idx = face.mIndices();

        for (int i = 0; i < face.mNumIndices() - 1; i++) {

            int a = idx.get(i);
            int b = idx.get(i + 1);

            AIVector3D v1 = vertices.get(a);
            AIVector3D v2 = vertices.get(b);

            Cylinder line = createCylinderBetween(v1, v2);

            lineMesh.getChildren().add(line);
        }

        return lineMesh;
    }

    private Cylinder createCylinderBetween(AIVector3D p1, AIVector3D p2) {
        double x1 = p1.x();
        double y1 = p1.y();
        double z1 = p1.z();

        double x2 = p2.x();
        double y2 = p2.y();
        double z2 = p2.z();

        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;

        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);

        double radius = modelSize * THICKNESS;
        Cylinder cylinder = new Cylinder(radius, length);

        // центр линии
        cylinder.getTransforms().add(new Translate(
                (x1 + x2) / 2,
                (y1 + y2) / 2,
                (z1 + z2) / 2
        ));

        // ориентация (упрощённо)
        double theta = Math.toDegrees(Math.atan2(Math.sqrt(dx * dx + dz * dz), dy));
        double phi = Math.toDegrees(Math.atan2(dx, dz));

        cylinder.getTransforms().add(new Rotate(phi, Rotate.Y_AXIS));
        cylinder.getTransforms().add(new Rotate(theta, Rotate.X_AXIS));

        return cylinder;
    }

}
