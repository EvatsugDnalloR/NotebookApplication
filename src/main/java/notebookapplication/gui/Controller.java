package notebookapplication.gui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import notebookapplication.model.EventPropertyNameEnum;
import notebookapplication.model.NoteFacade;
import notebookapplication.model.NotePage;


/**
 * Main controller class that coordinates between the model and view components.
 *
 * <p>Handles UI initialisation, button actions, and content synchronisation.
 *
 * <p>Implements both Initializable and PropertyChangeListener interfaces.
 */
public class Controller implements Initializable, PropertyChangeListener {
    /*
    TODO: Replace TextArea by InlineCssTextArea and apply the text editing features.
    TODO: Complete basic UI of the NotebookApplication.
     */

    /**
     * The main content area where users can view and edit the text of the current note page.
     *
     * <p>This {@code TextArea} displays the plain text content of the currently selected NotePage,
     * should be later on updated to {@code InlineCssTextArea} that supports
     * advanced text editing features.
     */
    @FXML private TextArea contentArea;

    /**
     * Container for the GroupBar component that displays note groups as horizontal tabs
     * by holding the custom GroupBar component, and the "+" button for adding new groups.
     */
    @FXML private HBox groupBarContainer;

    /**
     * Container for the PageBar component that displays note pages as vertical tabs
     * by holding the custom PageBar component, and the "+" button for adding new pages.
     */
    @FXML private VBox pageBarContainer;

    /**
     * Button that triggers the creation of a new note group when clicked,
     * positioned at the end of the group bar for intuitive access.
     */
    @FXML private Button addGroupBtn;

    /**
     * Button that triggers the creation of a new note page when clicked,
     * positioned at the bottom of the page bar for intuitive access.
     */
    @FXML private Button addPageBtn;

    /** The main facade that provides access to all notebook model operations.  */
    private final NoteFacade facade = new NoteFacade();

    /**
     * The current page being edited in the content area.
     * This local reference is maintained for several reasons:
     *
     * <p>- Content Synchronization: Manages saving/loading between UI and model
     *
     * <p>- Event Handling: Provides stable reference for event source comparison
     *
     * <p>- Performance: Avoids repeated calls to facade.getCurrentPage()
     *
     * <p>- State Management: Tracks which page's content is currently displayed
     *
     * <p>- Operation Context: Ensures operations affect the correct page during UI events
     *
     * <p>This reference works in coordination with the facade's current page but
     * serves specific UI management needs that require a stable reference
     * throughout content synchronisation operations.
     */
    private NotePage currentPage;

    /**
     * Initialises the controller after its root element has been completely processed.
     * Sets up the GroupBar, PageBar, content area, and event listeners.
     *
     * @param location the location used to resolve relative paths for the root object, or null
     * @param resources the resources used to localise the root object, or null
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize GUI components with facade
        GroupBar groupBar = new GroupBar(facade);
        PageBar pageBar = new PageBar(facade);
        groupBarContainer.getChildren().addFirst(groupBar);
        pageBarContainer.getChildren().addFirst(pageBar);

        // Register for content updates
        facade.addPropertyChangeListener(this);
        currentPage = facade.getCurrentPage();

        loadPageContent();
        setupButtons();
    }

    /** Configures the action handlers for the add group and add page buttons.  */
    private void setupButtons() {
        addGroupBtn.setOnAction(_ -> facade.createNewGroup());
        addPageBtn.setOnAction(_ -> facade.createNewPage(facade.getCurrentGroup()));
    }

    /**
     * Handles property change events from the model.
     * Responds to page switching and content changes to keep the UI synchronised.
     *
     * @param evt the property change event containing information about the change
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        EventPropertyNameEnum event = EventPropertyNameEnum.fromPropertyName(evt.getPropertyName());
        if (event == null) {
            throw new IllegalArgumentException("Unknown property: " + evt.getPropertyName());
        }

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

            default:
                //throw new IllegalArgumentException("Unknown property: " + event);
                break;
        }
    }

    /**
     * Saves the current content of the text area to the active page model.
     *
     * <p>Called before switching pages to ensure content is persisted.
     */
    private void saveCurrentContent() {
        if (currentPage != null) {
            currentPage.setPlainText(contentArea.getText());
        }
    }

    /**
     * Loads content from the active page model into the text area.
     *
     * <p>Called after page switching or when content changes externally.
     */

    private void loadPageContent() {
        if (currentPage != null) {
            contentArea.setText(currentPage.getPlainTextContent());
        }
    }

    /**
     * Saves all content and persists the complete notebook state to a file.
     *
     * <p>Can be called from menu actions or other UI components.
     */
    public void saveAllContent() {
        saveCurrentContent();
        try {
            facade.saveNotebook("notebook.dat");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}