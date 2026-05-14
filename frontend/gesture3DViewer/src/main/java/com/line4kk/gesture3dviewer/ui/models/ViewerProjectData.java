package com.line4kk.gesture3dviewer.ui.models;

import com.line4kk.gesture3dviewer.model.ViewerSettings;
import javafx.scene.transform.Affine;

public class ViewerProjectData {
    public double cameraX;
    public double cameraY;
    public double cameraZ;
    public Affine modelSceneTransforms;

    public ViewerProjectData(double[] cameraCoordinates, Affine modelSceneTransforms) {
        this.modelSceneTransforms = modelSceneTransforms;

        if (cameraCoordinates.length != 3)
            return;
        cameraX = cameraCoordinates[0];
        cameraY = cameraCoordinates[1];
        cameraZ = cameraCoordinates[2];
    }

    public ViewerProjectData() {
        cameraX = cameraY = 0;
        cameraZ = ViewerSettings.initCameraZ;

        modelSceneTransforms = new Affine();
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
