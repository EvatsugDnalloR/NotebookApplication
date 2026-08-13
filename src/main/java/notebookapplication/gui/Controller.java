package notebookapplication.gui;

import java.awt.Desktop;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
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
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.Spinner;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import notebookapplication.command.UndoRedo;
import notebookapplication.model.EventPropertyNameEnum;
import notebookapplication.model.NoteFacade;
import notebookapplication.model.NotePage;
import org.fxmisc.richtext.InlineCssTextArea;


/**
 * Main controller that coordinates the model and the view.
 *
 * <p>Acts as the assembly layer: it owns the {@code @FXML} components,
 * the notebook facade and the page lifecycle, and delegates the toolbar
 * feature wiring to three helpers:
 *
 * <ul>
 *   <li>{@link TextFormatting} — bold/italic/underline,
 *       cut/copy/paste, text-level undo/redo</li>
 *   <li>{@link FontStyle} — font family, font size, colour</li>
 *   <li>{@link ParagraphStyle} — text alignment, bullet /
 *       numbered / checkbox lists</li>
 * </ul>
 *
 * <p>Implements both Initializable and PropertyChangeListener.
 */
public class Controller implements Initializable, PropertyChangeListener {
    private static final Logger LOGGER = Logger.getLogger(Controller.class.getName());

    // ---------------------------------------------------------------
    //  FXML UI components
    // ---------------------------------------------------------------

    /**
     * The main content area where users can view and edit the text of the current note page.
     *
     * <p>Uses RichTextFX's {@code InlineCssTextArea} which provides per-keystroke undo/redo,
     * inline CSS styling, and a virtualised text flow for large documents.
     */
    @FXML private InlineCssTextArea contentArea;

    /** Container for the GroupBar. */
    @FXML private HBox groupBarContainer;

    /** Container for the PageBar. */
    @FXML private VBox pageBarContainer;

    /** Button for adding a new note group. */
    @FXML private Button addGroupBtn;

    /** Button for adding a new note page. */
    @FXML private Button addPageBtn;

    // ---------------------------------------------------------------
    //  FXML text formatting toolbar
    // ---------------------------------------------------------------

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

    // ---------------------------------------------------------------
    //  FXML font/colour toolbar
    // ---------------------------------------------------------------

    /** Font selector combobox. */
    @FXML private ComboBox<String> fontSelector;

    /** Font size spinner (editable, 8–72). */
    @FXML private Spinner<Double> fontSizeSpinner;

    /** Text colour picker. */
    @FXML private ColorPicker colorPicker;

    // ---------------------------------------------------------------
    //  FXML paragraph toolbar
    // ---------------------------------------------------------------

    /** Left-align radio menu item. */
    @FXML private RadioMenuItem leftAlign;

    /** Centre-align radio menu item. */
    @FXML private RadioMenuItem centerAlign;

    /** Right-align radio menu item. */
    @FXML private RadioMenuItem rightAlign;

    /** Toolbar bullet-points toggle button. */
    @FXML private ToggleButton bulletPoints;

    /** Toolbar numbered-list toggle button. */
    @FXML private ToggleButton numberListing;

    /** Toolbar checkbox toggle button. */
    @FXML private ToggleButton checkBoxes;

    // ---------------------------------------------------------------
    //  FXML menu components
    // ---------------------------------------------------------------

    /** File → Save. */
    @FXML private MenuItem menuSave;

    /** File → Load. */
    @FXML private MenuItem menuLoad;

    /** File → Export. */
    @FXML private MenuItem menuExport;

    /** File → Import. */
    @FXML private MenuItem menuImport;

    /** File → Close. */
    @FXML private MenuItem menuClose;

    /** Help → About. */
    @FXML private MenuItem menuAbout;

    /** Edit → Undo (app-level). */
    @FXML private MenuItem menuUndo;

    /** Edit → Redo (app-level). */
    @FXML private MenuItem menuRedo;

    // ---------------------------------------------------------------
    //  Internal app states components
    // ---------------------------------------------------------------

    /** The app-level undo/redo manager shared with the facade. */
    private final UndoRedo undoRedo = new UndoRedo();

    /** The main facade that provides access to all notebook model operations. */
    private final NoteFacade facade = new NoteFacade(undoRedo);

    /** The page currently being edited in the content area. */
    private NotePage currentPage;

    /**
     * CSS properties to apply on the next text insertion.
     * Accumulated as the user toggles formatting (bold/italic/underline, font size, colour) with no text selected.
     * Shared with the helpers.
     */
    private String pendingCss;

    /** Character-level formatting toolbar wiring. */
    private TextFormatting textFormatting;

    /** Font / colour toolbar wiring. */
    private FontStyle fontStyle;

    /** Paragraph-level toolbar wiring. */
    private ParagraphStyle paragraphStyle;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Notebook model + UI scaffolding
        GroupBar groupBar = new GroupBar(facade);
        PageBar pageBar = new PageBar(facade);
        groupBarContainer.getChildren().addFirst(groupBar);
        pageBarContainer.getChildren().addFirst(pageBar);

        facade.addPropertyChangeListener(this);
        currentPage = facade.getCurrentPage();
        undoRedo.setOnChanged(this::updateUndoRedoMenuState);

        // loadPageContent() may override the family with the page's saved font.
        contentArea.setStyle("-fx-font-family: "
                + FontStyle.DEFAULT_FONT    // Default base style: Arial at 12pt.
                + "; -fx-font-size: "
                + FontStyle.DEFAULT_FONT_SIZE + "pt;");

        // Wire toolbar feature helpers
        textFormatting = new TextFormatting(contentArea,
                textUndo, textRedo, cut, copy, paste,
                bold, italic, underline,
                () -> pendingCss, css -> pendingCss = css,
                () -> currentPage);
        fontStyle = new FontStyle(contentArea,
                fontSelector, fontSizeSpinner, colorPicker,
                () -> pendingCss, css -> pendingCss = css,
                () -> currentPage);
        paragraphStyle = new ParagraphStyle(contentArea,
                leftAlign, centerAlign, rightAlign,
                bulletPoints, numberListing, checkBoxes);

        textFormatting.setupAll();
        fontStyle.setupAll();
        paragraphStyle.setupAll();

        loadPageContent();
        setupButtons();
        setupMenuActions();

        // Autoload existing notebook on startup
        if (new File("notebook.dat").exists()) {
            try {
                facade.loadNotebook("notebook.dat");
                currentPage = facade.getCurrentPage();
                loadPageContent();
                LOGGER.info("Auto-loaded notebook from notebook.dat");
            } catch (IOException | ClassNotFoundException e) {
                LOGGER.log(Level.SEVERE, "Failed to auto-load notebook", e);
            }
        }

        Platform.runLater(() -> {
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setOnCloseRequest(_ -> handleSave());

            contentArea.getScene().addEventFilter(
                    KeyEvent.KEY_PRESSED, event -> {
                        if (event.isControlDown()
                                && !event.isShiftDown()) {
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
                            } else if (code == KeyCode.Z) {
                                textFormatting.safeTextUndo();
                                event.consume();
                            } else if (code == KeyCode.Y) {
                                textFormatting.safeTextRedo();
                                event.consume();
                            }
                        }

                        // Backspace on an empty first paragraph that carries a list style: clear the grouping.
                        if (event.getCode() == KeyCode.BACK_SPACE) {
                            if (paragraphStyle.clearFirstParagraphListStyleOnBackspace()) {
                                event.consume();
                            }
                        }
                    });

            // Apply accumulated pending CSS when text is inserted
            contentArea.richChanges()
                    .filter(ch -> !ch.getInserted().equals(ch.getRemoved()))
                    .subscribe(ch -> {
                        if (pendingCss != null && !pendingCss.isEmpty()
                                && ch.getInserted().length() - ch.getRemoved().length() > 0) {
                            int start = ch.getPosition();
                            int insertedLen = ch.getInserted().length() - ch.getRemoved().length();
                            String current = contentArea.getStyleAtPosition(start);
                            // Strip base properties that conflict or overlap with pendingCss before merging.
                            String base = CssHelper.stripConflicting(current, pendingCss);
                            if (pendingCss.contains("-fx-font-size:")) {
                                base = CssHelper.replaceProperty(base, "-fx-font-size:", "");
                            }
                            if (pendingCss.contains("-fx-fill:")) {
                                base = CssHelper.replaceProperty(base, "-fx-fill:", "");
                            }
                            String merged = CssHelper.mergeCss(base, pendingCss);
                            contentArea.setStyle(start, start + insertedLen, merged);
                            // Keep the style change out of the undo merge so it never merges with adjacent edits.
                            contentArea.getUndoManager().preventMerge();
                        }
                        pendingCss = null;
                    });
        });
    }

    // ---------------------------------------------------------------
    //  Button / menu wiring
    // ---------------------------------------------------------------

    private void setupButtons() {
        addGroupBtn.setOnAction(_ -> facade.createNewGroup());
        addPageBtn.setOnAction(_ -> facade.createNewPage(facade.getCurrentGroup()));
    }

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

    // ---------------------------------------------------------------
    //  Property change handling
    // ---------------------------------------------------------------

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        EventPropertyNameEnum event = EventPropertyNameEnum.fromPropertyName(evt.getPropertyName());
        if (event == null) {
            throw new IllegalArgumentException(
                    "Unknown property: " + evt.getPropertyName());
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

    private void saveCurrentContent() {
        if (currentPage != null) {
            String html = HtmlBridge.extractHtml(contentArea);
            currentPage.setHtmlBody(html);
        }
    }

    private void loadPageContent() {
        if (currentPage != null) {
            String html = currentPage.toHtml();
            HtmlBridge.populateArea(contentArea, html);
            // Restore the page-level font and update the selector
            String font = currentPage.getFontFamily();
            fontStyle.applyPageFont(font != null ? font : FontStyle.DEFAULT_FONT);
            contentArea.getUndoManager().forgetHistory();
        }
    }

    // ---------------------------------------------------------------
    //  App-level undo / redo
    // ---------------------------------------------------------------

    private void handleUndo() {
        saveCurrentContent();
        undoRedo.undo();
        currentPage = facade.getCurrentPage();
        loadPageContent();
    }

    private void handleRedo() {
        saveCurrentContent();
        undoRedo.redo();
        currentPage = facade.getCurrentPage();
        loadPageContent();
    }

    private void updateUndoRedoMenuState() {
        menuUndo.setDisable(!undoRedo.canUndo());
        menuRedo.setDisable(!undoRedo.canRedo());
    }

    // ---------------------------------------------------------------
    //  File menu actions
    // ---------------------------------------------------------------

    private void handleSave() {
        saveCurrentContent();
        try {
            facade.saveNotebook("notebook.dat");
            LOGGER.info("Notebook saved to notebook.dat");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to save notebook", e);
        }
    }

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

    private void handleExport() {
        saveCurrentContent();
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Notebook");
        chooser.setInitialFileName("notebook.dat");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Notebook Data (*.dat)", "*.dat"));
        File file = chooser.showSaveDialog(contentArea.getScene().getWindow());
        if (file != null) {
            try {
                facade.saveNotebook(file.getAbsolutePath());
                LOGGER.info("Notebook exported to " + file.getAbsolutePath());
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Failed to export notebook", e);
            }
        }
    }

    private void handleImport() {
        saveCurrentContent();
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Import Notebook");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Notebook Data (*.dat)", "*.dat"));
        File file = chooser.showOpenDialog(contentArea.getScene().getWindow());
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

    private void handleClose() {
        handleSave();
        Stage stage = (Stage) contentArea.getScene().getWindow();
        stage.close();
    }

    private void handleAbout() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("About NotebookApplication");
        alert.setHeaderText("NotebookApplication v2.8.4");

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
                Desktop.getDesktop().browse(new URI("https://github.com/EvatsugDnalloR/NotebookApplication"));
            } catch (Exception e) {
                LOGGER.log(Level.INFO, "Error ignored when initialising Github link", e);
            }   // Browser not available — silently ignore
        });

        alert.getDialogPane().setContent(new VBox(content, repoLink));
        alert.getDialogPane().setPrefWidth(420);
        alert.showAndWait();
    }
}
