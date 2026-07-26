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
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.fxmisc.richtext.InlineCssTextArea;
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
     * The main content area where users can view and edit the text of the
     * current note page.
     *
     * <p>Uses RichTextFX's {@code InlineCssTextArea} which provides per-keystroke
     * undo/redo, inline CSS styling, and a virtualised text flow for large
     * documents.
     */
    @FXML private InlineCssTextArea contentArea;

    /**
     * Container for the GroupBar component that displays note groups as
     * horizontal tabs by holding the custom GroupBar component, and the "+"
     * button for adding new groups.
     */
    @FXML private HBox groupBarContainer;

    /**
     * Container for the PageBar component that displays note pages as
     * vertical tabs by holding the custom PageBar component, and the "+"
     * button for adding new pages.
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

    /** Toolbar undo button for text-level undo. */
    @FXML private Button textUndo;

    /** Toolbar redo button for text-level redo. */
    @FXML private Button textRedo;

    /** Toolbar cut button. */
    @FXML private Button cut;

    /** Toolbar copy button. */
    @FXML private Button copy;

    /** Toolbar paste button. */
    @FXML private Button paste;

    /** Toolbar bold toggle button. */
    @FXML private ToggleButton bold;

    /** Toolbar italic toggle button. */
    @FXML private ToggleButton italic;

    /** Toolbar underline toggle button. */
    @FXML private ToggleButton underline;

    /**
     * Menu item under File -> Save. Triggers notebook serialization to disk.
     * Keyboard accelerator: Ctrl+S.
     */
    @FXML private MenuItem menuSave;

    /**
     * Menu item under File -> Load. Triggers notebook deserialization from
     * disk. Keyboard accelerator: Ctrl+L.
     */
    @FXML private MenuItem menuLoad;

    /**
     * Menu item under File → Export. Exports notebook data to a user-chosen
     * location.
     */
    @FXML private MenuItem menuExport;

    /**
     * Menu item under File → Import. Imports notebook data from a user-chosen
     * file.
     */
    @FXML private MenuItem menuImport;

    /** Menu item under File -> Close. Saves and exits the application. */
    @FXML private MenuItem menuClose;

    /** Menu item under Help -> About. Shows application information dialogue. */
    @FXML private MenuItem menuAbout;

    /** Menu item under Edit → Undo. Keyboard shortcut: Ctrl+Shift+Z. */
    @FXML private MenuItem menuUndo;

    /** Menu item under Edit → Redo. Keyboard shortcut: Ctrl+Shift+Y. */
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
     * <p>- Operation Context: Ensures operations affect the correct page during
     *        UI events
     *
     * <p>This reference works in coordination with the facade's current page but
     * serves specific UI management needs that require a stable reference
     * throughout content synchronisation operations.
     */
    private NotePage currentPage;

    // ---------------------------------------------------------------
    //  Text formatting pending state
    // ---------------------------------------------------------------

    /**
     * CSS properties to apply on the next text insertion. Accumulated as
     * the user toggles formatting buttons (bold/italic/underline) with no
     * text selected. Cleared after one insertion or when the caret moves
     * to a position with conflicting formatting.
     */
    private String pendingCss;

    /**
     * Initialises the controller after its root element has been completely
     * processed. Sets up the GroupBar, PageBar, content area, and event
     * listeners.
     *
     * @param location  the location used to resolve relative paths for the
     *                  root object, or null
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

        // Override InlineCssTextArea's built-in monospace default so that italic renders correctly.
        contentArea.setStyle("-fx-font-family: sans-serif;");

        loadPageContent();
        setupButtons();
        setupMenuActions();
        setupTextUndoRedo();
        setupCutCopyPaste();
        setupToggleFormatting(bold, "-fx-font-weight: bold;", "Bold", KeyCode.B);
        setupToggleFormatting(italic, "-fx-font-style: italic;", "Italic", KeyCode.I);
        setupToggleFormatting(underline, "-fx-underline: true;", "Underline", KeyCode.U);

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

        // Auto-save on window close, keyboard shortcuts,
        // and pending-CSS-on-insertion subscriber.
        Platform.runLater(() -> {
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setOnCloseRequest(_ -> handleSave());

            // Scene-level shortcuts for text formatting
            contentArea.getScene().addEventFilter(
                    KeyEvent.KEY_PRESSED, event -> {
                if (event.isControlDown() && !event.isShiftDown()) {
                    KeyCode code = event.getCode();
                    if (code == KeyCode.B) {
                        bold.fire();
                        event.consume();
                    } else if (code == KeyCode.I) {
                        italic.fire();
                        event.consume();
                    } else if (code == KeyCode.U) {
                        underline.fire();
                        event.consume();
                    }
                }
            });

            // Apply accumulated pending CSS when text is inserted
            contentArea.richChanges()
                    .filter(ch -> !ch.getInserted().equals(ch.getRemoved()))
                    .subscribe(ch -> {
                if (pendingCss != null && !pendingCss.isEmpty()) {
                    int start = ch.getPosition();
                    int insertedLen = ch.getInserted().length() - ch.getRemoved().length();
                    if (insertedLen > 0) {
                        String current = contentArea.getStyleAtPosition(start);
                        String merged = mergeCss(current, pendingCss);
                        contentArea.setStyle(start, start + insertedLen, merged);
                    }
                    pendingCss = null;
                }
            });
        });
    }

    /** Configures the action handlers for the add group and add page buttons. */
    private void setupButtons() {
        addGroupBtn.setOnAction(_ -> facade.createNewGroup());
        addPageBtn.setOnAction(_ -> facade.createNewPage(facade.getCurrentGroup()));
    }

    /**
     * Configures menu item accelerators and action handlers for
     * File → Save / Load / Export / Import / Close, Edit → Undo / Redo,
     * and Help → About.
     */
    private void setupMenuActions() {
        menuSave.setAccelerator(new KeyCodeCombination(
                KeyCode.S, KeyCombination.CONTROL_DOWN));
        menuLoad.setAccelerator(new KeyCodeCombination(
                KeyCode.L, KeyCombination.CONTROL_DOWN));
        menuSave.setOnAction(_ -> handleSave());
        menuLoad.setOnAction(_ -> handleLoad());

        menuExport.setAccelerator(new KeyCodeCombination(
                KeyCode.S, KeyCombination.CONTROL_DOWN,
                KeyCombination.SHIFT_DOWN));
        menuImport.setAccelerator(new KeyCodeCombination(
                KeyCode.L, KeyCombination.CONTROL_DOWN,
                KeyCombination.SHIFT_DOWN));
        menuExport.setOnAction(_ -> handleExport());
        menuImport.setOnAction(_ -> handleImport());

        menuClose.setOnAction(_ -> handleClose());
        menuAbout.setOnAction(_ -> handleAbout());

        menuUndo.setAccelerator(new KeyCodeCombination(
                KeyCode.Z, KeyCombination.ALT_DOWN));
        menuRedo.setAccelerator(new KeyCodeCombination(
                KeyCode.Y, KeyCombination.ALT_DOWN));
        menuUndo.setOnAction(_ -> handleUndo());
        menuRedo.setOnAction(_ -> handleRedo());
    }

    /**
     * Handles property change events from the model.
     * Responds to page switching and content changes to keep the UI
     * synchronised.
     *
     * @param evt the property change event
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
                if (evt.getSource() == currentPage) {
                    loadPageContent();
                }
                break;

            default:
                break;
        }
    }

    // ---------------------------------------------------------------
    //  Content save / load
    // ---------------------------------------------------------------

    /** Saves content from the editor to the active page model via HTML. */
    private void saveCurrentContent() {
        if (currentPage != null) {
            String html = HtmlBridge.extractHtml(contentArea);
            currentPage.setHtmlBody(html);
        }
    }

    /**
     * Loads content from the active page model into the editor.
     * Resets the undo history so the user can't undo into the previous
     * page's state.
     */
    private void loadPageContent() {
        if (currentPage != null) {
            String html = currentPage.toHtml();
            HtmlBridge.populateArea(contentArea, html);
            contentArea.getUndoManager().forgetHistory();
        }
    }

    // ---------------------------------------------------------------
    //  App-level undo/redo
    // ---------------------------------------------------------------

    /** Undoes the most recent app-level command. */
    private void handleUndo() {
        saveCurrentContent();
        undoRedo.undo();
        currentPage = facade.getCurrentPage();
        loadPageContent();
    }

    /** Redoes the most recently undone app-level command. */
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

    // ---------------------------------------------------------------
    //  Text-level undo/redo
    // ---------------------------------------------------------------

    /** Wires toolbar undo/redo buttons with tooltips, actions, and state. */
    @SuppressWarnings("unchecked")
    private void setupTextUndoRedo() {
        textUndo.setTooltip(new Tooltip("Undo (Ctrl+Z)"));
        textRedo.setTooltip(new Tooltip("Redo (Ctrl+Y)"));

        textUndo.setOnAction(_ -> contentArea.getUndoManager().undo());
        textRedo.setOnAction(_ -> contentArea.getUndoManager().redo());

        contentArea.getUndoManager().undoAvailableProperty()
                .addListener((_, _, available) ->
                        textUndo.setDisable(!(boolean) available));
        contentArea.getUndoManager().redoAvailableProperty()
                .addListener((_, _, available) ->
                        textRedo.setDisable(!(boolean) available));

        textUndo.setDisable(!contentArea.getUndoManager().isUndoAvailable());
        textRedo.setDisable(!contentArea.getUndoManager().isRedoAvailable());
    }

    // ---------------------------------------------------------------
    //  Toolbar: cut / copy / paste
    // ---------------------------------------------------------------

    /** Wires toolbar cut/copy/paste buttons with tooltips and state. */
    private void setupCutCopyPaste() {
        cut.setTooltip(new Tooltip("Cut (Ctrl+X)"));
        copy.setTooltip(new Tooltip("Copy (Ctrl+C)"));
        paste.setTooltip(new Tooltip("Paste (Ctrl+V)"));

        cut.setOnAction(_ -> contentArea.cut());
        copy.setOnAction(_ -> contentArea.copy());
        paste.setOnAction(_ -> contentArea.paste());

        updatePasteState();
        contentArea.focusedProperty().addListener((_, _, focused) -> {
            if (focused) {
                updatePasteState();
            }
        });
    }

    /** Updates paste button availability based on clipboard content. */
    private void updatePasteState() {
        paste.setDisable(!Clipboard.getSystemClipboard().hasString());
    }


    // ---------------------------------------------------------------
    //  Toolbar: bold / italic / underline
    // ---------------------------------------------------------------

    /**
     * Configures one toggle button for a formatting property.
     *
     * @param btn      the toggle button to wire
     * @param cssProp  the CSS property to toggle (e.g.
     *                 {@code "-fx-font-weight: bold;"})
     * @param label    human-readable label for the tooltip
     * @param hotkey   keyboard shortcut key (e.g. {@code KeyCode.B})
     */
    private void setupToggleFormatting(ToggleButton btn, String cssProp, String label, KeyCode hotkey) {
        btn.setTooltip(new Tooltip(label + " (Ctrl+" + hotkey.getName() + ")"));

        btn.setOnAction(_ -> {
            javafx.scene.control.IndexRange sel = contentArea.getSelection();
            if (sel.getLength() > 0) {
                // Apply toggle directly to selection
                String current = contentArea.getStyleAtPosition(sel.getStart());
                String newStyle = toggleCss(current, cssProp);
                contentArea.setStyle(sel.getStart(), sel.getEnd(), newStyle);
            } else {
                // Accumulate for next typed text
                if (pendingCss == null) {
                    pendingCss = "";
                }
                pendingCss = toggleCss(pendingCss, cssProp);
            }
            contentArea.requestFocus();
        });

        // Keep toggle state in sync with caret / selection position
        contentArea.selectionProperty().addListener(
                (_, _, _) -> updateToggleState(btn, cssProp));
        contentArea.caretPositionProperty().addListener(
                (_, _, _) -> updateToggleState(btn, cssProp));
    }

    /** Reads the style at the current position and sets the button state. */
    private void updateToggleState(ToggleButton btn, String cssProp) {
        javafx.scene.control.IndexRange sel = contentArea.getSelection();
        int pos;
        if (sel.getLength() > 0) {
            pos = sel.getStart();
        } else {
            pos = contentArea.getCaretPosition();
            if (pos > 0) {
                pos--;
            }
        }
        String style = contentArea.getStyleAtPosition(pos);
        btn.setSelected(style != null && style.contains(cssProp));
    }

    /**
     * Toggles a single CSS property in a style string.
     * If the property is present it is removed; otherwise it is appended.
     *
     * @param style   current CSS style string (maybe null)
     * @param cssProp the property to toggle (e.g. {@code "-fx-font-weight: bold;"})
     * @return the new CSS style string with the property toggled
     */
    private static String toggleCss(String style, String cssProp) {
        if (style == null) {
            return cssProp;
        }
        return style.contains(cssProp) ? style.replace(cssProp, "") : style + cssProp;
    }

    /**
     * Merges two CSS style strings, keeping the right-hand side values
     * where there is overlap.
     *
     * @param base the existing style
     * @param add  the style to overlay (wins on conflict)
     * @return merged CSS string
     */
    private static String mergeCss(String base, String add) {
        if (base == null || base.isBlank()) {
            return add;
        }
        if (add == null || add.isBlank()) {
            return base;
        }
        return base.trim() + " " + add.trim();
    }

    // ---------------------------------------------------------------
    //  File menu actions
    // ---------------------------------------------------------------

    /** Saves the current editor content and persists the notebook to disk. */
    private void handleSave() {
        saveCurrentContent();
        try {
            facade.saveNotebook("notebook.dat");
            LOGGER.info("Notebook saved to notebook.dat");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to save notebook", e);
        }
    }

    /** Loads the notebook from disk and rebuilds the UI. */
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

    /** Exports the notebook to a user-chosen location via a file dialog. */
    private void handleExport() {
        saveCurrentContent();
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Notebook");
        chooser.setInitialFileName("notebook.dat");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Notebook Data (*.dat)", "*.dat"));
        File file = chooser.showSaveDialog(
                contentArea.getScene().getWindow());
        if (file != null) {
            try {
                facade.saveNotebook(file.getAbsolutePath());
                LOGGER.info("Notebook exported to " + file.getAbsolutePath());
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Failed to export notebook", e);
            }
        }
    }

    /** Imports a notebook from a user-chosen file. */
    private void handleImport() {
        saveCurrentContent();
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Import Notebook");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Notebook Data (*.dat)", "*.dat"));
        File file = chooser.showOpenDialog(
                contentArea.getScene().getWindow());
        if (file != null) {
            try {
                facade.loadNotebook(file.getAbsolutePath());
                currentPage = facade.getCurrentPage();
                loadPageContent();
                LOGGER.info("Notebook imported from " + file.getAbsolutePath());
            } catch (IOException | ClassNotFoundException e) {
                LOGGER.log(Level.SEVERE, "Failed to import notebook", e);
            }
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
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("About NotebookApplication");
        alert.setHeaderText("NotebookApplication v2.2.1");

        Label content = new Label("""
                A OneNote-like notebook application built with
                JavaFX 24.

                Features
                Rich text editing, note groups and pages,
                bullet points and checkboxes, undo/redo
                for group and page operations, manual and
                auto save/load.

                Author     EvatsugDnalloR
                License    MIT License

                Built with
                JavaFX 24 · JDK 24 · Maven
                RichTextFX (InlineCssTextArea)

                Repository
                """);

        Hyperlink repoLink = new Hyperlink("github.com/EvatsugDnalloR/NotebookApplication");
        repoLink.setOnAction(_ -> {
            try {
                java.awt.Desktop.getDesktop().browse(
                        new java.net.URI(
                                "https://github.com/EvatsugDnalloR/"
                                + "NotebookApplication"));
            } catch (Exception ignored) {
                // Browser not available — silently ignore
            }
        });

        alert.getDialogPane().setContent(
                new VBox(content, repoLink));
        alert.getDialogPane().setPrefWidth(420);
        alert.showAndWait();
    }
}
