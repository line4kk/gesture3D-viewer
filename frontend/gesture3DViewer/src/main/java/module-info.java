module com.line4kk.gesture3dviewer {
    requires javafx.controls;
    requires javafx.fxml;
    requires jeromq;
    requires com.fasterxml.jackson.databind;
    requires jdk.compiler;
    requires org.lwjgl.assimp;
    requires javafx.graphics;
    requires atlantafx.base;
    requires java.desktop;


    exports com.line4kk.gesture3dviewer;
    exports com.line4kk.gesture3dviewer.model;
    exports com.line4kk.gesture3dviewer.ui;
    exports com.line4kk.gesture3dviewer.ui.models;
    exports com.line4kk.gesture3dviewer.ui.controllers;
    exports com.line4kk.gesture3dviewer.ui.utils;


    opens com.line4kk.gesture3dviewer.model to com.fasterxml.jackson.databind;
    opens com.line4kk.gesture3dviewer.ui to javafx.fxml;
    opens com.line4kk.gesture3dviewer to com.fasterxml.jackson.databind, javafx.fxml;
    opens com.line4kk.gesture3dviewer.ui.controllers to javafx.fxml;
}