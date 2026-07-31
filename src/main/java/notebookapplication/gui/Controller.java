package notebookapplication.gui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.SpinnerValueFactory.DoubleSpinnerValueFactory;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
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

    /** Font size spinner (editable, 8–72). */
    @FXML private Spinner<Double> fontSizeSpinner;

    /** Text colour picker. */
    @FXML private ColorPicker colorPicker;

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

    /** The app-level undo/redo manager shared with the facade. */
    private final UndoRedo undoRedo = new UndoRedo();

    /** The main facade that provides access to all notebook model operations. */
    private final NoteFacade facade = new NoteFacade(undoRedo);

    /** The current page being edited in the content area. */
    private NotePage currentPage;

    /**
     * CSS properties to apply on the next text insertion.
     * Accumulated as the user toggles formatting buttons (bold/italic/underline)
     * or changes font size with no text selected.
     */
    private String pendingCss;

    /** Default font family applied to new pages. */
    private static final String DEFAULT_FONT = "Arial";

    /** Default font size (pt) for new content. */
    private static final double DEFAULT_FONT_SIZE = 12.0;

    /** Default text colour for new content. */
    private static final Color DEFAULT_COLOR = Color.BLACK;

    /** Guards against programmatic spinner updates re-entering the value-change listener. */
    private boolean suppressSpinnerUpdate;

    /** Guards against programmatic colour-picker updates re-entering the value-change listener. */
    private boolean suppressColorUpdate;

    // ---------------------------------------------------------------

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        GroupBar groupBar = new GroupBar(facade);
        PageBar pageBar = new PageBar(facade);
        groupBarContainer.getChildren().addFirst(groupBar);
        pageBarContainer.getChildren().addFirst(pageBar);

        facade.addPropertyChangeListener(this);
        currentPage = facade.getCurrentPage();
        undoRedo.setOnChanged(this::updateUndoRedoMenuState);

        // Default base style: Arial at 12pt. loadPageContent() may override the family with the page's saved font.
        contentArea.setStyle("-fx-font-family: " + DEFAULT_FONT + "; -fx-font-size: " + DEFAULT_FONT_SIZE + "pt;");

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
        setupFontSizeSpinner();
        setupColorPicker();

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

            contentArea.richChanges()
                    .filter(ch -> !ch.getInserted().equals(ch.getRemoved()))
                    .subscribe(ch -> {
                if (pendingCss != null && !pendingCss.isEmpty()
                        && ch.getInserted().length() - ch.getRemoved().length() > 0) {
                    int start = ch.getPosition();
                    int insertedLen = ch.getInserted().length() - ch.getRemoved().length();
                    String current = contentArea.getStyleAtPosition(start);
                    // Strip base properties that conflict or overlap
                    // with pendingCss before merging. Font-size is a
                    // replacement property — strip the base's old value.
                    String base = CSSHelper.stripConflicting(current, pendingCss);
                    if (pendingCss.contains("-fx-font-size:")) {
                        base = CSSHelper.replaceProperty(base, "-fx-font-size:", "");
                    }
                    if (pendingCss.contains("-fx-fill:")) {
                        base = CSSHelper.replaceProperty(base, "-fx-fill:", "");
                    }
                    String merged = CSSHelper.mergeCss(base, pendingCss);
                    contentArea.setStyle(start, start + insertedLen, merged);
                }
                pendingCss = null;
            });
        });
    }

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
            String font = currentPage.getFontFamily();  // restore the page-level font
            applyPageFont(font != null ? font : DEFAULT_FONT);
            contentArea.getUndoManager().forgetHistory();
        }
    }

    // ---------------------------------------------------------------
    //  App-level undo/redo
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
    //  Toolbar: text undo / redo
    // ---------------------------------------------------------------

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
            if (focused) updatePasteState();
        });
    }

    private void updatePasteState() {
        paste.setDisable(!Clipboard.getSystemClipboard().hasString());
    }

    // ---------------------------------------------------------------
    //  Toolbar: font family selector
    // ---------------------------------------------------------------

    private static final LinkedHashMap<String, String> PREFERRED_FONTS = new LinkedHashMap<>();

    static {
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

    /** Populates the font selector and wires page-level font selection. */
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
        fontSelector.getSelectionModel().select(PREFERRED_FONTS.get(DEFAULT_FONT));

        fontSelector.setOnAction(_ -> {
            String label = fontSelector.getValue();
            if (label == null) return;
            String systemFont = getSystemFontName(label);
            applyPageFont(systemFont);
            if (currentPage != null) currentPage.setFontFamily(systemFont);
        });
    }

    /**
     * Applies a font to the entire content area and syncs the ComboBox without firing onAction.
     * Preserves the font-size property.
     *
     * @param systemFont  the font to apply
     */
    private void applyPageFont(String systemFont) {
        String current = contentArea.getStyle();
        if (current == null || current.isBlank()) {
            current = "-fx-font-size: " + DEFAULT_FONT_SIZE + "pt;";
        }
        String newStyle = CSSHelper.replaceProperty(current,
                "-fx-font-family:",
                "-fx-font-family: " + systemFont + ";");
        contentArea.setStyle(newStyle);
        String displayLabel = PREFERRED_FONTS.get(systemFont);
        if (displayLabel != null) fontSelector.getSelectionModel().select(displayLabel);
    }

    /** Looks up the system font name for a display label. */
    private String getSystemFontName(String displayLabel) {
        for (var entry : PREFERRED_FONTS.entrySet()) {
            if (entry.getValue().equals(displayLabel)) {
                return entry.getKey();
            }
        }
        return displayLabel;
    }

    // ---------------------------------------------------------------
    //  Toolbar: font size selector
    // ---------------------------------------------------------------

    private void setupFontSizeSpinner() {
        DoubleSpinnerValueFactory factory = setupDoubleSpinnerValueFactory();
        fontSizeSpinner.setValueFactory(factory);
        fontSizeSpinner.setEditable(true);

        // User changed the size (arrows, typing + Enter): apply it.
        factory.valueProperty().addListener((_, _, newVal) -> {
            if (suppressSpinnerUpdate || newVal == null) return;
            applyFontSize(newVal);
        });

        // Keep the spinner in sync with the caret / selection.
        contentArea.selectionProperty().addListener(
                (_, _, _) -> updateFontSizeState());
        contentArea.caretPositionProperty().addListener(
                (_, _, _) -> updateFontSizeState());
    }

    private DoubleSpinnerValueFactory setupDoubleSpinnerValueFactory() {
        DoubleSpinnerValueFactory factory = new DoubleSpinnerValueFactory(
                8.0, 72.0, DEFAULT_FONT_SIZE, 0.5);

        // Fault-tolerant conversion: unparseable input (e.g. "abc") falls back to the current value
        factory.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Double value) {
                return value == null ? "" : String.valueOf(value);
            }

            @Override
            public Double fromString(String text) {
                try {
                    return Double.parseDouble(text.trim());
                } catch (NumberFormatException e) {
                    return factory.getValue();
                }
            }
        });
        return factory;
    }

    /** Applies a font size to the selection or accumulates it for the next typed characters when nothing is selected.*/
    private void applyFontSize(double size) {
        String css = "-fx-font-size: " + size + "pt;";
        IndexRange sel = contentArea.getSelection();
        if (sel.getLength() > 0) {
            String current = getCurrentStyle(sel);
            String newStyle = CSSHelper.replaceProperty(current, "-fx-font-size:", css);
            contentArea.setStyle(sel.getStart(), sel.getEnd(), newStyle);
            contentArea.getUndoManager().preventMerge();
        } else {
            if (pendingCss == null) pendingCss = "";
            pendingCss = CSSHelper.replaceProperty(pendingCss, "-fx-font-size:", css);
        }
        contentArea.requestFocus();
    }

    private String getCurrentStyle(IndexRange sel) {
        int pos = Math.min(sel.getStart() + sel.getLength() / 2, contentArea.getLength() - 1);
        String current = "";
        if (contentArea.getLength() > 0 && pos >= 0) {
            current = contentArea.getStyleAtPosition(pos);
            if (current == null) current = "";
        }
        return current;
    }

    /** Updates the spinner to reflect the size at the selection / caret. */
    private void updateFontSizeState() {
        if (pendingCss != null) return;  // user has a pending choice

        IndexRange sel = contentArea.getSelection();
        Double size;
        if (sel.getLength() > 0) {
            // Selection: show the uniform size, or empty if mixed.
            size = selectionUniformFontSize(sel);
        } else {
            // Caret: show the size at the caret position.
            if (contentArea.getLength() == 0) {
                size = DEFAULT_FONT_SIZE;
            } else {
                int pos = contentArea.getCaretPosition();
                pos = pos > 0 ? pos - 1 : 0;
                size = CSSHelper.parseFontSize(contentArea.getStyleAtPosition(pos));
                if (size == null) size = DEFAULT_FONT_SIZE;
            }
        }

        if (size == null) {
            fontSizeSpinner.getEditor().setText("");
        } else {
            suppressSpinnerUpdate = true;
            try {
                fontSizeSpinner.getValueFactory().setValue(size);
            } finally {
                suppressSpinnerUpdate = false;
            }
        }
    }

    /**
     * Returns the font size if every character in the selection shares it; returns {@code null} if sizes are mixed.
     * Characters without an explicit size are treated as the default.
     */
    private Double selectionUniformFontSize(IndexRange sel) {
        if (sel.getLength() == 0 || contentArea.getLength() == 0) {
            return null;
        }
        Double common = null;
        for (int p = sel.getStart(); p < sel.getEnd(); p++) {
            Double size = CSSHelper.parseFontSize(contentArea.getStyleAtPosition(p));
            if (size == null) size = DEFAULT_FONT_SIZE;
            if (common == null) {
                common = size;
            } else if (Math.abs(common - size) > 1e-9) {
                return null;  // mixed sizes
            }
        }
        return common;
    }

    // ---------------------------------------------------------------
    //  Toolbar: colour picker
    // ---------------------------------------------------------------

    private void setupColorPicker() {
        colorPicker.setValue(DEFAULT_COLOR);

        // User changed the colour: apply it.
        colorPicker.valueProperty().addListener((_, _, newVal) -> {
            if (suppressColorUpdate || newVal == null) return;
            applyColor(newVal);
        });

        // Keep the picker in sync with the caret / selection.
        contentArea.selectionProperty().addListener(
                (_, _, _) -> updateColorState());
        contentArea.caretPositionProperty().addListener(
                (_, _, _) -> updateColorState());
    }

    /** Applies a colour to the selection, or accumulates it for the
     * next typed characters when nothing is selected. */
    private void applyColor(Color color) {
        String css = "-fx-fill: " + CSSHelper.colorToHex(color) + ";";
        IndexRange sel = contentArea.getSelection();
        if (sel.getLength() > 0) {
            String current = getCurrentStyle(sel);
            String newStyle = CSSHelper.replaceProperty(current, "-fx-fill:", css);
            contentArea.setStyle(sel.getStart(), sel.getEnd(), newStyle);
            contentArea.getUndoManager().preventMerge();
        } else {
            if (pendingCss == null) pendingCss = "";
            pendingCss = CSSHelper.replaceProperty(pendingCss, "-fx-fill:", css);
        }
        contentArea.requestFocus();
    }

    /** Updates the picker to reflect the colour at the selection / caret. */
    private void updateColorState() {
        if (pendingCss != null) return;  // user has a pending choice

        IndexRange sel = contentArea.getSelection();
        Color colour;
        if (sel.getLength() > 0) {
            // Selection: show the colour only if uniform, otherwise leave the picker untouched.
            colour = selectionUniformColor(sel);
            if (colour == null) return;
        } else {
            // Caret: show the colour at the caret position.
            if (contentArea.getLength() == 0) {
                colour = DEFAULT_COLOR;
            } else {
                int pos = contentArea.getCaretPosition();
                pos = pos > 0 ? pos - 1 : 0;
                colour = CSSHelper.parseColor(contentArea.getStyleAtPosition(pos));
                if (colour == null) colour = DEFAULT_COLOR;
            }
        }

        suppressColorUpdate = true;
        try {
            colorPicker.setValue(colour);
        } finally {
            suppressColorUpdate = false;
        }
    }

    /**
     * Returns the colour if every character in the selection shares
     * it; returns {@code null} if colours are mixed. Characters without
     * an explicit colour are treated as the default.
     */
    private Color selectionUniformColor(IndexRange sel) {
        if (sel.getLength() == 0 || contentArea.getLength() == 0) {
            return null;
        }
        Color common = null;
        for (int p = sel.getStart(); p < sel.getEnd(); p++) {
            Color colour = CSSHelper.parseColor(contentArea.getStyleAtPosition(p));
            if (colour == null) colour = DEFAULT_COLOR;
            if (common == null) {
                common = colour;
            } else if (!common.equals(colour)) {
                return null;  // mixed colours
            }
        }
        return common;
    }

    // ---------------------------------------------------------------
    //  Toolbar: bold / italic / underline
    // ---------------------------------------------------------------

    private void setupToggleFormatting(ToggleButton btn, String cssOn, String cssOff, String label, KeyCode hotkey) {
        btn.setTooltip(new Tooltip(label + " (Ctrl+" + hotkey.getName() + ")"));

        btn.setOnAction(_ -> {
            IndexRange sel = contentArea.getSelection();
            String add = btn.isSelected() ? cssOn : cssOff;
            String remove = btn.isSelected() ? cssOff : cssOn;
            if (sel.getLength() > 0) {
                String current = getCurrentStyle(sel);
                String newStyle = CSSHelper.ensureProperty(current, add);
                newStyle = CSSHelper.stripProperty(newStyle, remove);
                contentArea.setStyle(sel.getStart(), sel.getEnd(), newStyle);
                contentArea.getUndoManager().preventMerge();
            } else {
                if (pendingCss == null) pendingCss = "";
                pendingCss = CSSHelper.ensureProperty(pendingCss, add);
                pendingCss = CSSHelper.stripProperty(pendingCss, remove);
                btn.setSelected(pendingCss.contains(cssOn));
            }
            contentArea.requestFocus();
        });

        contentArea.selectionProperty().addListener(
                (_, _, _) -> updateToggleState(btn, cssOn));
        contentArea.caretPositionProperty().addListener(
                (_, _, _) -> updateToggleState(btn, cssOn));
    }

    private void updateToggleState(ToggleButton btn, String cssOn) {
        if (pendingCss != null) {
            btn.setSelected(pendingCss.contains(cssOn));
            return;
        }

        IndexRange sel = contentArea.getSelection();
        int pos = sel.getLength() > 0 ? sel.getStart() : contentArea.getCaretPosition();
        pos = pos > 0 ? pos - 1 : 0;

        String style = contentArea.getLength() == 0 ? "" : contentArea.getStyleAtPosition(pos);
        btn.setSelected(style != null && style.contains(cssOn));
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
        alert.setHeaderText("NotebookApplication v2.6.3");

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
                        new java.net.URI("https://github.com/EvatsugDnalloR/NotebookApplication"));
            } catch (Exception ignored) { }  // Browser not available, silently ignore
        });

        alert.getDialogPane().setContent(new VBox(content, repoLink));
        alert.getDialogPane().setPrefWidth(420);
        alert.showAndWait();
    }
}
