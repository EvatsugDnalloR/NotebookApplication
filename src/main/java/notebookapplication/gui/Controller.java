package notebookapplication.gui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Stage;
import notebookapplication.command.UndoRedo;
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
    private static final Logger LOGGER = Logger.getLogger(Controller.class.getName());

    /**
     * The main content area where users can view and edit the text of the current note page.
     *
     * <p>Uses JavaFX's built-in {@code HTMLEditor} which provides a WYSIWYG rich-text editing
     * experience with a built-in toolbar for bold, italic, underline, font family,
     * font size, text colour, and other formatting options.
     */
    @FXML private HTMLEditor contentArea;

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

    /**
     * Menu item under File -> Save. Triggers notebook serialization to disk.
     * Keyboard accelerator: Ctrl+S.
     */
    @FXML private MenuItem menuSave;

    /**
     * Menu item under File -> Load. Triggers notebook deserialization from disk.
     * Keyboard accelerator: Ctrl+L.
     */
    @FXML private MenuItem menuLoad;

    /**
     * Menu item under File -> Close. Saves and exits the application.
     */
    @FXML private MenuItem menuClose;

    /**
     * Menu item under Help -> About. Shows application information dialogue.
     */
    @FXML private MenuItem menuAbout;

    /**
     * Menu item under Edit → Undo. Keyboard shortcut: Ctrl+Z.
     */
    @FXML private MenuItem menuUndo;

    /**
     * Menu item under Edit → Redo. Keyboard shortcut: Ctrl+Y.
     */
    @FXML private MenuItem menuRedo;

    /** The undo/redo manager shared with the facade. */
    private final UndoRedo undoRedo = new UndoRedo();

    /** The main facade that provides access to all notebook model operations.  */
    private final NoteFacade facade = new NoteFacade(undoRedo);

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
        // Initialise GUI components with facade
        GroupBar groupBar = new GroupBar(facade);
        PageBar pageBar = new PageBar(facade);
        groupBarContainer.getChildren().addFirst(groupBar);
        pageBarContainer.getChildren().addFirst(pageBar);

        // Register for content updates
        facade.addPropertyChangeListener(this);
        currentPage = facade.getCurrentPage();
        undoRedo.setOnChanged(this::updateUndoRedoMenuState);

        loadPageContent();
        setupButtons();
        setupMenuActions();

        // Autoload existing notebook on startup
        if (new File("notebook.dat").exists()) {
            try {
                facade.loadNotebook("notebook.dat");
                currentPage = facade.getCurrentPage();
                loadPageContent();
                LOGGER.info("Auto-loaded existing notebook from notebook.dat");
            } catch (IOException | ClassNotFoundException e) {
                LOGGER.log(Level.SEVERE, "Failed to auto-load notebook", e);
            }
        }

        // Auto-save on window close (deferred until scene is attached)
        Platform.runLater(() -> {
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setOnCloseRequest(_ -> handleSave());
        });
    }

    /** Configures the action handlers for the add group and add page buttons.  */
    private void setupButtons() {
        addGroupBtn.setOnAction(_ -> facade.createNewGroup());
        addPageBtn.setOnAction(_ -> facade.createNewPage(facade.getCurrentGroup()));
    }

    /**
     * Configures menu item accelerators and action handlers for File → Save / Load.
     * Sets Ctrl+S for save and Ctrl+L for load.
     */
    private void setupMenuActions() {
        menuSave.setAccelerator(
                new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
        menuLoad.setAccelerator(
                new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN));
        menuSave.setOnAction(_ -> handleSave());
        menuLoad.setOnAction(_ -> handleLoad());
        menuClose.setOnAction(_ -> handleClose());
        menuAbout.setOnAction(_ -> handleAbout());
        menuUndo.setAccelerator(
                new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN,
                        KeyCombination.SHIFT_DOWN));
        menuRedo.setAccelerator(
                new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN,
                        KeyCombination.SHIFT_DOWN));
        menuUndo.setOnAction(_ -> handleUndo());
        menuRedo.setOnAction(_ -> handleRedo());
        updateUndoRedoMenuState();
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
                break;
        }
    }

    /**
     * Saves the current content from the HTMLEditor to the active page model.
     *
     * <p>Extracts the body content from the HTMLEditor's full HTML document and
     * stores it on the model via {@link NotePage#setHtmlBody(String)}.
     * Called before switching pages to ensure content is persisted.
     */
    private void saveCurrentContent() {
        if (currentPage != null) {
            String fullHtml = contentArea.getHtmlText();
            String bodyContent = extractBodyContent(fullHtml);
            currentPage.setHtmlBody(bodyContent);
        }
    }

    /**
     * Loads content from the active page model into the HTMLEditor.
     *
     * <p>Generates HTML from the page model via {@link NotePage#toHtml()},
     * wraps it in a full HTML document, and sets it on the editor.
     * Called after page switching or when content changes externally.
     */
    private void loadPageContent() {
        if (currentPage != null) {
            String bodyHtml = currentPage.toHtml();
            String fullDocument = wrapHtmlDocument(bodyHtml);
            contentArea.setHtmlText(fullDocument);
        }
    }

    /**
     * Wraps HTML body content in a complete HTML document suitable for
     * {@link HTMLEditor#setHtmlText(String)}.
     *
     * <p>The {@code contenteditable="true"} attribute on the body tag is required
     * by HTMLEditor; without it the editor becomes read-only.
     *
     * @param bodyContent the inner HTML to place inside the body tag
     * @return a complete HTML document string
     */
    private String wrapHtmlDocument(String bodyContent) {
        return "<html><head></head><body contenteditable=\"true\">"
                + bodyContent
                + "</body></html>";
    }

    /**
     * Extracts the inner body content from an HTMLEditor-produced HTML document.
     *
     * <p>HTMLEditor's {@link HTMLEditor#getHtmlText()} returns a full HTML document:
     * {@code <html><head>...</head><body contenteditable="true">...content...</body></html>}.
     * This method strips the outer document structure and our own wrapper div
     * to retrieve just the user-editable content.
     *
     * @param fullHtml the complete HTML document from HTMLEditor
     * @return the inner body HTML content, with wrapper div removed if present
     */
    private String extractBodyContent(String fullHtml) {
        // Locate the <body> tag
        int bodyTagStart = fullHtml.indexOf("<body");
        if (bodyTagStart == -1) {
            // No body tag found — return content as-is (defensive fallback)
            return fullHtml;
        }

        int bodyContentStart = fullHtml.indexOf(">", bodyTagStart) + 1;
        int bodyEnd = fullHtml.indexOf("</body>", bodyContentStart);
        if (bodyEnd == -1) {
            // No closing body tag — return everything after <body...>
            return fullHtml.substring(bodyContentStart);
        }

        String bodyContent = fullHtml.substring(bodyContentStart, bodyEnd).trim();

        // Strip our own wrapper div if present (added by NotePage.toHtml())
        final String wrapperStart = "<div class='note-content'>";
        final String wrapperEnd = "</div>";
        if (bodyContent.startsWith(wrapperStart) && bodyContent.endsWith(wrapperEnd)) {
            bodyContent = bodyContent.substring(wrapperStart.length(),
                    bodyContent.length() - wrapperEnd.length());
        }

        return bodyContent;
    }

    /** Saves the current HTMLEditor content and persists the notebook to disk. */
    private void handleSave() {
        saveCurrentContent();
        try {
            facade.saveNotebook("notebook.dat");
            LOGGER.info("Notebook saved to notebook.dat");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to save notebook", e);
        }
    }

    /** Loads the notebook from disk and rebuilds the UI to reflect loaded state. */
    private void handleLoad() {
        saveCurrentContent();
        try {
            facade.loadNotebook("notebook.dat");
            currentPage = facade.getCurrentPage();
            loadPageContent();
            LOGGER.info("Notebook loaded from notebook.dat");
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Failed to load notebook", e);
        }
    }

    /** Saves the notebook and exits the application. */
    private void handleClose() {
        handleSave();
        Stage stage = (Stage) contentArea.getScene().getWindow();
        stage.close();
    }

    /** Shows an About dialogue with application information. */
    private void handleAbout() {
        // TODO: better About info including author name, GitHub repo link, license info etc.
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("About NotebookApplication");
        alert.setHeaderText("NotebookApplication v1.0.0");
        alert.setContentText("""
                A OneNote-like notebook application built with JavaFX.

                Features:
                - Rich text editing (bold, italic, underline, fonts, colours)
                - Note groups and pages (OneNote-style organisation)
                - Advanced text grouping (bullet points, checkboxes)
                - Save and load notebook state
                - Auto-save on exit, auto-load on startup

                Built with:
                - JavaFX 24
                - JDK 24
                - HTMLEditor (WebKit-based rich text)
                """);
        alert.getDialogPane().setPrefWidth(420);
        alert.showAndWait();
    }

    /** Undoes the most recent command (text edit or app operation). */
    private void handleUndo() {
        saveCurrentContent();
        undoRedo.undo();
        currentPage = facade.getCurrentPage();
        loadPageContent();
    }

    /** Redoes the most recently undone command. */
    private void handleRedo() {
        saveCurrentContent();
        undoRedo.redo();
        currentPage = facade.getCurrentPage();
        loadPageContent();
    }

    /** Enables or disables the Undo/Redo menu items based on stack state. */
    private void updateUndoRedoMenuState() {
        menuUndo.setDisable(!undoRedo.canUndo());
        menuRedo.setDisable(!undoRedo.canRedo());
    }
}