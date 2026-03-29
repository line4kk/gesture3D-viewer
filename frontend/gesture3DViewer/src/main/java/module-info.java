module com.line4kk.gesture3dviewer {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.line4kk.gesture3dviewer to javafx.fxml;
    exports com.line4kk.gesture3dviewer;
}