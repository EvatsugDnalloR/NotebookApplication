module notebookapplication {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires java.desktop;
    requires java.logging;

    exports notebookapplication.gui;
    exports notebookapplication.model;

    opens notebookapplication.gui to javafx.fxml;
}