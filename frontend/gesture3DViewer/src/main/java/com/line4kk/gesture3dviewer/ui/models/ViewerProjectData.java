package com.line4kk.gesture3dviewer.ui.models;

import com.line4kk.gesture3dviewer.model.ViewerSettings;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.scene.transform.Affine;

public class ViewerProjectData {
    public double cameraX;
    public double cameraY;
    public double cameraZ;
    public Affine modelSceneTransforms;
    public String currentObjectPath;

    public ViewerProjectData(double[] cameraCoordinates, Affine modelSceneTransforms) {
        this.modelSceneTransforms = (modelSceneTransforms == null) ? new Affine() : modelSceneTransforms;

        if (cameraCoordinates == null || cameraCoordinates.length != 3)
            return;
        cameraX = cameraCoordinates[0];
        cameraY = cameraCoordinates[1];
        cameraZ = cameraCoordinates[2];
    }

    public ViewerProjectData() {
        cameraX = cameraY = 0;
        cameraZ = ViewerSettings.initCameraZ;

        modelSceneTransforms = new Affine();
        currentObjectPath = null;
    }

    public static ViewerProjectData fromJson(JsonNode dataNode) {
        ViewerProjectData data = new ViewerProjectData();

        if (dataNode == null || dataNode.isNull() || dataNode.isMissingNode()) {
            return data;
        }

        JsonNode cameraNode = dataNode.path("camera");
        data.cameraX = cameraNode.path("x").asDouble(data.cameraX);
        data.cameraY = cameraNode.path("y").asDouble(data.cameraY);
        data.cameraZ = cameraNode.path("z").asDouble(data.cameraZ);

        JsonNode transformNode = dataNode.path("transform");
        if (transformNode.isObject()) {
            data.modelSceneTransforms = new Affine(
                    transformNode.path("mxx").asDouble(1.0),
                    transformNode.path("mxy").asDouble(0.0),
                    transformNode.path("mxz").asDouble(0.0),
                    transformNode.path("tx").asDouble(0.0),
                    transformNode.path("myx").asDouble(0.0),
                    transformNode.path("myy").asDouble(1.0),
                    transformNode.path("myz").asDouble(0.0),
                    transformNode.path("ty").asDouble(0.0),
                    transformNode.path("mzx").asDouble(0.0),
                    transformNode.path("mzy").asDouble(0.0),
                    transformNode.path("mzz").asDouble(1.0),
                    transformNode.path("tz").asDouble(0.0)
            );
        }

        JsonNode currentObjectNode = dataNode.path("currentObjectPath");
        if (!currentObjectNode.isMissingNode() && !currentObjectNode.isNull()) {
            data.currentObjectPath = currentObjectNode.asText(null);
        }

        return data;
    }

    public String toJson() {

        double mxx = modelSceneTransforms.getMxx();
        double mxy = modelSceneTransforms.getMxy();
        double mxz = modelSceneTransforms.getMxz();
        double tx  = modelSceneTransforms.getTx();

        double myx = modelSceneTransforms.getMyx();
        double myy = modelSceneTransforms.getMyy();
        double myz = modelSceneTransforms.getMyz();
        double ty  = modelSceneTransforms.getTy();

        double mzx = modelSceneTransforms.getMzx();
        double mzy = modelSceneTransforms.getMzy();
        double mzz = modelSceneTransforms.getMzz();
        double tz  = modelSceneTransforms.getTz();

        return "{"
                + "\"camera\": {"
                + "\"x\": " + cameraX + ","
                + "\"y\": " + cameraY + ","
                + "\"z\": " + cameraZ
                + "},"
                + "\"currentObjectPath\": "
                + (currentObjectPath == null ? "null" : "\"" + currentObjectPath + "\"")
                + ","
                + "\"transform\": {"
                + "\"mxx\": " + mxx + ","
                + "\"mxy\": " + mxy + ","
                + "\"mxz\": " + mxz + ","
                + "\"tx\": " + tx + ","

                + "\"myx\": " + myx + ","
                + "\"myy\": " + myy + ","
                + "\"myz\": " + myz + ","
                + "\"ty\": " + ty + ","

                + "\"mzx\": " + mzx + ","
                + "\"mzy\": " + mzy + ","
                + "\"mzz\": " + mzz + ","
                + "\"tz\": " + tz
                + "}"
                + "}";
    }
}
