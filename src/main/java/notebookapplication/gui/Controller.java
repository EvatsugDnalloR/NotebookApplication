package notebookapplication.gui;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.fxmisc.richtext.InlineCssTextArea;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {  // Implement Initializable
    @FXML
    private InlineCssTextArea textArea;

    @FXML
    private HBox groupBar;
    @FXML
    private VBox pageBar;

    private final PauseTransition debouncer = new PauseTransition(Duration.seconds(0.5));
    private String content = "";

    @FXML
    private ToggleButton defaultGroup;
    @FXML
    private ToggleButton defaultPage;

    @FXML
    private Button addGroup;
    @FXML
    private Button addPage;

    private ToggleGroup noteGroup = new ToggleGroup();
    private ToggleGroup notePages = new ToggleGroup();

    private int groupCount = 1;
    private int pageCount = 1;

    @Override  // Add initialize method
    public void initialize(URL location, ResourceBundle resources) {
        setupTextHandling();
        defaultGroup.setToggleGroup(noteGroup);
        defaultPage.setToggleGroup(notePages);
        defaultGroup.setSelected(true);
        defaultPage.setSelected(true);
        setUpNewPageGroup(defaultGroup, "Group");
        setUpNewPageGroup(defaultPage, "Page");
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

    @FXML
    private void addNewGroup() {
        groupCount++;
        ToggleButton newGroup = new ToggleButton("Group " + groupCount);

        // Add context menu for deletion
        setUpNewPageGroup(newGroup, "Group");

        // Add before the "+" button
        groupBar.getChildren().add(groupBar.getChildren().size() - 1, newGroup);
    }

    private void setUpNewPageGroup(ToggleButton newButton, String groupOrPage) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Delete " + groupOrPage);
        if  (groupOrPage.equals("Group")) {
            deleteItem.setOnAction(e -> deleteGroup(newButton));
            newButton.setToggleGroup(noteGroup);
        } else if (groupOrPage.equals("Page")) {
            deleteItem.setOnAction(e -> deletePage(newButton));
            newButton.setToggleGroup(notePages);
        } else {
            throw new IllegalArgumentException("Invalid group or page");
        }
        contextMenu.getItems().add(deleteItem);
        newButton.setContextMenu(contextMenu);
    }

    private void deleteGroup(ToggleButton groupButton) {
        groupBar.getChildren().remove(groupButton);
        groupCount--;
    }

    @FXML
    private void addNewPage() {
        pageCount++;
        ToggleButton newPage = new ToggleButton("Page " + pageCount);

        // Add context menu for deletion
        setUpNewPageGroup(newPage, "Page");

        // Add before the "+" button
        pageBar.getChildren().add(pageBar.getChildren().size() - 1, newPage);
    }

    private void deletePage(ToggleButton pageButton) {
        pageBar.getChildren().remove(pageButton);
        pageCount--;
    }
}