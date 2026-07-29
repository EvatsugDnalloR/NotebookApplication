package notebookapplication.gui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.IndexRange;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
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
     * <p>Uses RichTextFX's {@code InlineCssTextArea} which provides
     * per-keystroke undo/redo, inline CSS styling, and a virtualised text
     * flow for large documents.
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

    /** Button for adding a new note group. */
    @FXML private Button addGroupBtn;

    /** Button for adding a new note page. */
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

    /** Font selector combobox. */
    @FXML private ComboBox<String> fontSelector;

    /**
     * Menu item under File → Save. Triggers notebook serialization to disk.
     * Keyboard accelerator: Ctrl+S.
     */
    @FXML private MenuItem menuSave;

    /**
     * Menu item under File → Load. Triggers notebook deserialization from
     * disk. Keyboard accelerator: Ctrl+L.
     */
    @FXML private MenuItem menuLoad;

    /**
     * Menu item under File → Export. Exports notebook data to a user-chosen
     * location.
     */
    @FXML private MenuItem menuExport;

    /**
     * Menu item under File → Import. Imports notebook data from a
     * user-chosen file.
     */
    @FXML private MenuItem menuImport;

    /** Menu item under File → Close. Saves and exits. */
    @FXML private MenuItem menuClose;

    /** Menu item under Help → About. */
    @FXML private MenuItem menuAbout;

    /** Menu item under Edit → Undo (app-level). */
    @FXML private MenuItem menuUndo;

    /** Menu item under Edit → Redo (app-level). */
    @FXML private MenuItem menuRedo;

    /** The app-level undo/redo manager shared with the facade. */
    private final UndoRedo undoRedo = new UndoRedo();

    /** The main facade that provides access to all notebook model
     * operations.  */
    private final NoteFacade facade = new NoteFacade(undoRedo);

    /** The current page being edited in the content area. */
    private NotePage currentPage;

    /**
     * CSS properties to apply on the next text insertion. Accumulated as
     * the user toggles formatting buttons (bold/italic/underline) with no
     * text selected. Each property has an ON variant (e.g. {@code
     * "-fx-font-weight: bold;"}) and an OFF variant (e.g. {@code
     * "-fx-font-weight: normal;"}). Clearing the ON variant alone is not
     * enough — at a position that already has bold, new text inherits the
     * existing style. The OFF variant explicitly overrides it.
     */
    private String pendingCss;

    // ---------------------------------------------------------------

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

        // Override InlineCssTextArea's built-in monospace default so that italic renders correctly
        contentArea.setStyle("-fx-font-family: sans-serif;");

        loadPageContent();
        setupButtons();
        setupMenuActions();
        setupTextUndoRedo();
        setupCutCopyPaste();
        setupToggleFormatting(bold,
                "-fx-font-weight: bold;", "-fx-font-weight: normal;",
                "Bold", KeyCode.B);
        setupToggleFormatting(italic,
                "-fx-font-style: italic;", "-fx-font-style: normal;",
                "Italic", KeyCode.I);
        setupToggleFormatting(underline,
                "-fx-underline: true;", "-fx-underline: false;",
                "Underline", KeyCode.U);
        setupFontSelector();

        // Autoload existing notebook on startup
        if (new File("notebook.dat").exists()) {
            try {
                facade.loadNotebook("notebook.dat");
                currentPage = facade.getCurrentPage();
                loadPageContent();
                LOGGER.info(
                        "Auto-loaded notebook from notebook.dat");
            } catch (IOException | ClassNotFoundException e) {
                LOGGER.log(Level.SEVERE,
                        "Failed to auto-load notebook", e);
            }
        }

        // Auto-save on close, keyboard shortcuts, pending-CSS subscriber.
        Platform.runLater(() -> {
            Stage stage =
                    (Stage) contentArea.getScene().getWindow();
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
                    .filter(ch -> !ch.getInserted()
                            .equals(ch.getRemoved()))
                    .subscribe(ch -> {
                if (pendingCss != null && !pendingCss.isEmpty()
                        && ch.getInserted().length() - ch.getRemoved().length() > 0) {
                    int start = ch.getPosition();
                    int insertedLen = ch.getInserted().length() - ch.getRemoved().length();
                    String current = contentArea.getStyleAtPosition(start);
                    // Strip base properties that conflict with pendingCss before merging
                    String merged = CSSHelper.mergeCss(CSSHelper.stripConflicting(current, pendingCss), pendingCss);
                    contentArea.setStyle(start, start + insertedLen, merged);
                }
                pendingCss = null;
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
        menuSave.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
        menuLoad.setAccelerator(new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN));
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

        menuUndo.setAccelerator(new KeyCodeCombination(KeyCode.Z, KeyCombination.ALT_DOWN));
        menuRedo.setAccelerator(new KeyCodeCombination(KeyCode.Y, KeyCombination.ALT_DOWN));
        menuUndo.setOnAction(_ -> handleUndo());
        menuRedo.setOnAction(_ -> handleRedo());
        updateUndoRedoMenuState();
    }

    /**
     * Handles property change events from the model.
     * Responds to page switching and content changes to keep the UI synchronised.
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
     * Resets the undo history so the user can't undo into the previous page's state.
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
    //  Toolbar: text undo / redo
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
        contentArea.focusedProperty().addListener(
                (_, _, focused) -> {
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
    //  Toolbar: font family/size/colour selector
    // ---------------------------------------------------------------

    /**
     * System-font-name → display-label pairs for the font selector.
     * Display labels use the format {@code "English Name (中文名)"}
     * for CJK fonts so both readers can find them.
     */
    private static final LinkedHashMap<String, String> PREFERRED_FONTS = new LinkedHashMap<>();

    static {
        // Latin / code / general-purpose fonts
        PREFERRED_FONTS.put("Arial", "Arial");
        PREFERRED_FONTS.put("Calibri", "Calibri");
        PREFERRED_FONTS.put("Cambria", "Cambria");
        PREFERRED_FONTS.put("Cascadia Code", "Cascadia Code");
        PREFERRED_FONTS.put("Century Gothic", "Century Gothic");
        PREFERRED_FONTS.put("Consolas", "Consolas");
        PREFERRED_FONTS.put("Georgia", "Georgia");
        PREFERRED_FONTS.put("JetBrainsMono NF", "JetBrainsMono NF");
        PREFERRED_FONTS.put("Segoe UI", "Segoe UI");
        PREFERRED_FONTS.put("Verdana", "Verdana");

        // CJK fonts with bilingual display names
        PREFERRED_FONTS.put("Microsoft YaHei", "Microsoft YaHei (微软雅黑)");
        PREFERRED_FONTS.put("Microsoft JhengHei", "Microsoft JhengHei (微軟正黑體)");
        PREFERRED_FONTS.put("SimSun", "SimSun (宋体)");
        PREFERRED_FONTS.put("SimHei", "SimHei (黑体)");
        PREFERRED_FONTS.put("FangSong", "FangSong (仿宋)");
        PREFERRED_FONTS.put("KaiTi", "KaiTi (楷体)");
        PREFERRED_FONTS.put("DengXian", "DengXian (等线)");
        PREFERRED_FONTS.put("NSimSun", "NSimSun (新宋体)");
        PREFERRED_FONTS.put("Yu Gothic", "Yu Gothic (游ゴシック)");
        PREFERRED_FONTS.put("华文细黑", "华文细黑 (STHeiti Light)");
    }

    /** Pre-populates the font selector with a curated subset of
     * installed system fonts. */
    private void setupFontSelector() {
        fontSelector.getItems().clear();
        for (String family : Font.getFamilies()) {
            for (var entry : PREFERRED_FONTS.entrySet()) {
                if (family.equalsIgnoreCase(entry.getKey())) {
                    fontSelector.getItems().add(entry.getValue());
                    break;
                }
            }
        }
        if (!fontSelector.getItems().isEmpty()) {
            fontSelector.getSelectionModel().selectFirst();
        }
    }

    // ---------------------------------------------------------------
    //  Toolbar: bold / italic / underline
    // ---------------------------------------------------------------

    /**
     * Configures one toggle button for a formatting property with explicit ON and OFF CSS variants.
     *
     * <p>With a selection, the ON/OFF variant is applied directly.
     * With no selection, the variant is accumulated in
     * {@link #pendingCss} so that it takes effect on the next typed character.
     *
     * @param btn     the toggle button to wire
     * @param cssOn   CSS to apply when the property is ON (e.g. {@code "-fx-font-weight: bold;"})
     * @param cssOff  CSS to apply when the property is OFF (e.g. {@code "-fx-font-weight: normal;"})
     * @param label   human-readable label for the tooltip
     * @param hotkey  keyboard shortcut key
     */
    private void setupToggleFormatting(ToggleButton btn, String cssOn, String cssOff, String label, KeyCode hotkey) {
        btn.setTooltip(new Tooltip(label + " (Ctrl+" + hotkey.getName() + ")"));

        btn.setOnAction(_ -> {
            IndexRange sel = contentArea.getSelection();
            String add = btn.isSelected() ? cssOn : cssOff;
            String remove = btn.isSelected() ? cssOff : cssOn;
            if (sel.getLength() > 0) {
                // Read from the middle of the selection to avoid paragraph-boundary / newline artefacts.
                int pos = Math.min(sel.getStart() + sel.getLength() / 2, contentArea.getLength() - 1);
                String current = "";
                if (contentArea.getLength() > 0 && pos >= 0) {
                    current = contentArea.getStyleAtPosition(pos);
                    if (current == null) {
                        current = "";
                    }
                }
                String newStyle = CSSHelper.ensureProperty(current, add);
                newStyle = CSSHelper.stripProperty(newStyle, remove);
                contentArea.setStyle(sel.getStart(), sel.getEnd(), newStyle);

                // Prevent UndoManager from merging this style change with adjacent ones
                contentArea.getUndoManager().preventMerge();
            } else {
                /*
                 * No selection, record the intent for the next typed character.
                 * Use the button's selected state to decide whether to add the ON or OFF variant.
                 * Also remove the opposite variant so that pendingCss never contains conflicting properties.
                 */
                if (pendingCss == null) {
                    pendingCss = "";
                }
                pendingCss = CSSHelper.ensureProperty(pendingCss, add);
                pendingCss = CSSHelper.stripProperty(pendingCss, remove);
                // Sync button with pending state immediately, not relying on listener timing
                btn.setSelected(pendingCss.contains(cssOn));
            }
            contentArea.requestFocus();
        });

        contentArea.selectionProperty().addListener(
                (_, _, _) -> updateToggleState(btn, cssOn));
        contentArea.caretPositionProperty().addListener(
                (_, _, _) -> updateToggleState(btn, cssOn));
    }

    /** Reads the style at the current position and sets the button state. */
    private void updateToggleState(ToggleButton btn, String cssOn) {
        // When the user has a pending formatting choice, the button reflects what WILL be applied, not what IS currently there.
        if (pendingCss != null) {
            btn.setSelected(pendingCss.contains(cssOn));
            return;
        }
        IndexRange sel = contentArea.getSelection();

        // Get the position to probe style for the current selection / caret
        int pos = sel.getLength() > 0 ? sel.getStart() : contentArea.getCaretPosition();
        pos = pos > 0 ? pos - 1: 0;

        // Check if position is valid before returning style
        String style = contentArea.getLength() == 0 ? "" : contentArea.getStyleAtPosition(pos);
        btn.setSelected(style != null && style.contains(cssOn));
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
                new FileChooser.ExtensionFilter("Notebook Data (*.dat)", "*.dat")
        );
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
        alert.setHeaderText("NotebookApplication v2.3.2");

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
                        new java.net.URI("https://github.com/EvatsugDnalloR/NotebookApplication")
                );
            } catch (Exception ignored) {
                // Browser not available — silently ignore
            }
        });

        alert.getDialogPane().setContent(new VBox(content, repoLink));
        alert.getDialogPane().setPrefWidth(420);
        alert.showAndWait();
    }
}