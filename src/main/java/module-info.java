module notebookapplication {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

//    requires org.controlsfx.controls;
//    requires com.dlsc.formsfx;
//    requires net.synedra.validatorfx;
//    requires org.kordamp.ikonli.javafx;
//    requires eu.hansolo.tilesfx;
    requires java.desktop;

//    requires org.fxmisc.richtext;
//    requires reactfx;

    exports notebookapplication.gui;
    exports notebookapplication.model;
//    exports notebookapplication.command;

    opens notebookapplication.gui to javafx.fxml;
}