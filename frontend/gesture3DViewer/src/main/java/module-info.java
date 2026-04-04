module com.line4kk.gesture3dviewer {
    requires javafx.controls;
    requires javafx.fxml;
    requires jeromq;
    requires com.fasterxml.jackson.databind;
    requires jdk.compiler;


    opens com.line4kk.gesture3dviewer to javafx.fxml;
    exports com.line4kk.gesture3dviewer;
    exports com.line4kk.gesture3dviewer.model;

    opens com.line4kk.gesture3dviewer.model to com.fasterxml.jackson.databind;
}