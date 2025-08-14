package notebookapplication.gui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import notebookapplication.model.NoteFacade;
import notebookapplication.model.NotePage;
import notebookapplication.model.EventPropertyNameEnum;
import org.fxmisc.richtext.InlineCssTextArea;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable, PropertyChangeListener {  // Implement Initializable
    @FXML private TextArea contentArea;
    @FXML private HBox groupBarContainer;
    @FXML private VBox pageBarContainer;
    @FXML private Button addGroupBtn;
    @FXML private Button addPageBtn;

    private GroupBar groupBar;
    private PageBar pageBar;

    private final NoteFacade facade = new NoteFacade();
    private NotePage currentPage;

    @Override  // Add initialize method
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize GUI components with facade
        groupBar = new GroupBar(facade);
        pageBar = new PageBar(facade);

        groupBarContainer.getChildren().addFirst(groupBar);
        pageBarContainer.getChildren().addFirst(pageBar);

        // Register for content updates
        facade.addPropertyChangeListener(this);
        currentPage = facade.getCurrentPage();
        loadPageContent();

        setupButtons();
    }

    private void setupButtons() {
        addGroupBtn.setOnAction(e -> facade.createNewGroup());
        addPageBtn.setOnAction(e -> facade.createNewPage(facade.getCurrentGroup()));
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        EventPropertyNameEnum event = EventPropertyNameEnum.fromPropertyName(evt.getPropertyName());
        if (event == null) { throw new IllegalArgumentException("Unknown property: " + evt.getPropertyName()); }

        switch (event) {
            case SWITCH_TO_PAGE:
                saveCurrentContent();
                currentPage = (NotePage) evt.getNewValue();
                loadPageContent();
                break;

            case SET_CONTENT:
                // If current page content changed externally
                if (evt.getSource() == currentPage) {
                    loadPageContent();
                }
                break;
        }
    }

    private void saveCurrentContent() {
        if (currentPage != null) {
            currentPage.setPlainText(contentArea.getText());
        }
    }

    private void loadPageContent() {
        if (currentPage != null) {
            contentArea.setText(currentPage.getPlainTextContent());
        }
    }

    public void saveAllContent() {
        saveCurrentContent();
        try {
            facade.saveNotebook("notebook.dat");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}