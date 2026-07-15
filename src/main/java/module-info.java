module notebookapplication {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.logging;
    requires org.fxmisc.richtext;
    requires org.fxmisc.undo;
    requires reactfx;

    exports notebookapplication.gui;
    exports notebookapplication.model;
    exports notebookapplication.command;

    opens notebookapplication.gui to javafx.fxml;
    opens notebookapplication.model to javafx.fxml;
}
