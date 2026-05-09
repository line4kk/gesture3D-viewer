module com.line4kk.gesture3dviewer {
    requires javafx.controls;
    requires javafx.fxml;
    requires jeromq;
    requires com.fasterxml.jackson.databind;
    requires jdk.compiler;
    requires org.lwjgl.assimp;
    requires javafx.graphics;


    exports com.line4kk.gesture3dviewer;
    exports com.line4kk.gesture3dviewer.model;

    opens com.line4kk.gesture3dviewer.model to com.fasterxml.jackson.databind;
    opens com.line4kk.gesture3dviewer to com.fasterxml.jackson.databind, javafx.fxml;
}