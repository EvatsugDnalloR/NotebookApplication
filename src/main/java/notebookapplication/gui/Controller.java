package notebookapplication.gui;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.util.Duration;
import org.fxmisc.richtext.InlineCssTextArea;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {  // Implement Initializable
    @FXML
    private InlineCssTextArea textArea;

    private final PauseTransition debouncer = new PauseTransition(Duration.seconds(0.5));
    private String content = "";

    @Override  // Add initialize method
    public void initialize(URL location, ResourceBundle resources) {
        setupTextHandling();
    }

    private void setupTextHandling() {
        System.out.println("Setting up text handling...");
        textArea.richChanges().subscribe(change -> {
            System.out.println("Change detected: " + change);
            debouncer.setOnFinished(e -> saveContent());
            debouncer.playFromStart();
        });
    }

    @FXML
    public void debugging() {
        System.out.println("Current content: " + content);
//        System.out.println("TextArea content: " + textArea.getText());
    }

    private void saveContent() {
        content = textArea.getText();
//        System.out.println("Auto-saved: " + content);
    }
}