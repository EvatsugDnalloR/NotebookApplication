package notebookapplication.gui;

import java.util.LinkedHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.IndexRange;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.DoubleSpinnerValueFactory;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import notebookapplication.model.NotePage;
import org.fxmisc.richtext.InlineCssTextArea;


/**
 * Wires the font-family selector, the font-size spinner and the text colour picker.
 * Font size and colour are character-level formats (selection or pending for the next typed characters),
 * while the font family is a page-level setting stored on the {@code NotePage}.
 *
 * <p>Shared mutable state ({@code pendingCss} and the current
 * {@code NotePage}) is injected through functional interfaces.
 */
public class FontStyle {
    /** Default font family applied to new pages. */
    public static final String DEFAULT_FONT = "Arial";

    /** Default font size (pt) for new content. */
    public static final double DEFAULT_FONT_SIZE = 12.0;

    /** Default text colour for new content. */
    public static final Color DEFAULT_COLOR = Color.BLACK;

    /** Placeholder shown in the picker for a mixed-colour selection. */
    public static final Color SENTINEL_COLOR = Color.TRANSPARENT;

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
        PREFERRED_FONTS.put("Microsoft YaHei",
                "Microsoft YaHei (微软雅黑)");
        PREFERRED_FONTS.put("Microsoft JhengHei",
                "Microsoft JhengHei (微軟正黑體)");
        PREFERRED_FONTS.put("SimSun", "SimSun (宋体)");
        PREFERRED_FONTS.put("SimHei", "SimHei (黑体)");
        PREFERRED_FONTS.put("FangSong", "FangSong (仿宋)");
        PREFERRED_FONTS.put("KaiTi", "KaiTi (楷体)");
        PREFERRED_FONTS.put("DengXian", "DengXian (等线)");
        PREFERRED_FONTS.put("NSimSun", "NSimSun (新宋体)");
        PREFERRED_FONTS.put("Yu Gothic", "Yu Gothic (游ゴシック)");
        PREFERRED_FONTS.put("华文细黑", "华文细黑 (STHeiti Light)");
    }

    private final InlineCssTextArea contentArea;
    private final ComboBox<String> fontSelector;
    private final Spinner<Double> fontSizeSpinner;
    private final ColorPicker colorPicker;
    private final Supplier<String> getPendingCss;
    private final Consumer<String> setPendingCss;
    private final Supplier<NotePage> getCurrentPage;

    private boolean suppressSpinnerUpdate;
    private boolean suppressColorUpdate;

    /**
     * FontStyle helper class constructor.
     *
     * @param contentArea    the editor
     * @param fontSelector   font-family combo box
     * @param fontSizeSpinner font-size spinner
     * @param colorPicker    text colour picker
     * @param getPendingCss  reads the shared pending-CSS buffer
     * @param setPendingCss  writes the shared pending-CSS buffer
     * @param getCurrentPage supplies the page currently being edited
     */
    public FontStyle(InlineCssTextArea contentArea,
                     ComboBox<String> fontSelector,
                     Spinner<Double> fontSizeSpinner,
                     ColorPicker colorPicker,
                     Supplier<String> getPendingCss,
                     Consumer<String> setPendingCss,
                     Supplier<NotePage> getCurrentPage) {
        this.contentArea = contentArea;
        this.fontSelector = fontSelector;
        this.fontSizeSpinner = fontSizeSpinner;
        this.colorPicker = colorPicker;
        this.getPendingCss = getPendingCss;
        this.setPendingCss = setPendingCss;
        this.getCurrentPage = getCurrentPage;
    }

    /** Wires all font/colour components managed by this helper. */
    public void setupAll() {
        setupFontSelector();
        setupFontSizeSpinner();
        setupColorPicker();
    }

    // ---------------------------------------------------------------
    //  Font family (page-level)
    // ---------------------------------------------------------------

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
            if (label == null) {
                return;
            }
            String systemFont = getSystemFontName(label);
            applyPageFont(systemFont);
            NotePage page = getCurrentPage.get();
            if (page != null) {
                page.setFontFamily(systemFont);
            }
        });
    }

    /**
     * Applies a font to the entire content area and syncs the combo box
     * without firing onAction. Preserves the font-size property.
     *
     * @param systemFont the font to apply
     */
    public void applyPageFont(String systemFont) {
        String current = contentArea.getStyle();
        if (current == null || current.isBlank()) {
            current = "-fx-font-size: " + DEFAULT_FONT_SIZE + "pt;";
        }
        String newStyle = CssHelper.replaceProperty(current,
                "-fx-font-family:",
                "-fx-font-family: " + systemFont + ";");
        contentArea.setStyle(newStyle);
        String displayLabel = PREFERRED_FONTS.get(systemFont);
        if (displayLabel != null) {
            fontSelector.getSelectionModel().select(displayLabel);
        }
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
    //  Font size (character-level)
    // ---------------------------------------------------------------

    private void setupFontSizeSpinner() {
        DoubleSpinnerValueFactory factory = setupDoubleSpinnerValueFactory();
        fontSizeSpinner.setValueFactory(factory);
        fontSizeSpinner.setEditable(true);

        // User changed the size (arrows, typing + Enter): apply it.
        factory.valueProperty().addListener((_, _, newVal) -> {
            if (suppressSpinnerUpdate || newVal == null) {
                return;
            }
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

    /**
     * Applies a font size to the selection,
     * or accumulates it for the next typed characters when nothing is selected.
     */
    private void applyFontSize(double size) {
        String css = "-fx-font-size: " + size + "pt;";
        IndexRange sel = contentArea.getSelection();
        if (sel.getLength() > 0) {
            String current = CssHelper.getCurrentStyle(sel, contentArea);
            String newStyle = CssHelper.replaceProperty(current, "-fx-font-size:", css);
            contentArea.setStyle(sel.getStart(), sel.getEnd(), newStyle);
            contentArea.getUndoManager().preventMerge();
        } else {
            String pending = getPendingCss.get();
            if (pending == null) {
                pending = "";
            }
            setPendingCss.accept(CssHelper.replaceProperty(pending, "-fx-font-size:", css));
        }
        contentArea.requestFocus();
    }

    /** Updates the spinner to reflect the size at the selection / caret. */
    private void updateFontSizeState() {
        if (getPendingCss.get() != null) {
            return;  // user has a pending choice
        }

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
                size = CssHelper.parseFontSize(contentArea.getStyleAtPosition(pos));
                if (size == null) {
                    size = DEFAULT_FONT_SIZE;
                }
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
     * Returns the font size if every character in the selection shares it; {@code null} if sizes are mixed.
     * Characters without an explicit size are treated as the default.
     */
    private Double selectionUniformFontSize(IndexRange sel) {
        if (sel.getLength() == 0 || contentArea.getLength() == 0) {
            return null;
        }
        Double common = null;
        for (int p = sel.getStart(); p < sel.getEnd(); p++) {
            Double size = CssHelper.parseFontSize(
                    contentArea.getStyleAtPosition(p));
            if (size == null) {
                size = DEFAULT_FONT_SIZE;
            }
            if (common == null) {
                common = size;
            } else if (Math.abs(common - size) > 1e-9) {
                return null;  // mixed sizes
            }
        }
        return common;
    }

    // ---------------------------------------------------------------
    //  Colour picker (character-level)
    // ---------------------------------------------------------------

    private void setupColorPicker() {
        colorPicker.setValue(DEFAULT_COLOR);

        // User changed the colour: apply it.
        colorPicker.valueProperty().addListener((_, _, newVal) -> {
            if (suppressColorUpdate || newVal == null || SENTINEL_COLOR.equals(newVal)) {
                return;  // ignore placeholder
            }
            applyColor(newVal);
        });

        // Keep the picker in sync with the caret / selection.
        contentArea.selectionProperty().addListener(
                (_, _, _) -> updateColorState());
        contentArea.caretPositionProperty().addListener(
                (_, _, _) -> updateColorState());
    }

    /**
     * Applies a colour to the selection,
     * or accumulates it for the next typed characters when nothing is selected.
     */
    private void applyColor(Color color) {
        String css = "-fx-fill: " + CssHelper.colorToHex(color) + ";";
        IndexRange sel = contentArea.getSelection();
        if (sel.getLength() > 0) {
            String current = CssHelper.getCurrentStyle(sel, contentArea);
            String newStyle = CssHelper.replaceProperty(current, "-fx-fill:", css);
            contentArea.setStyle(sel.getStart(), sel.getEnd(), newStyle);
            contentArea.getUndoManager().preventMerge();
        } else {
            String pending = getPendingCss.get();
            if (pending == null) {
                pending = "";
            }
            setPendingCss.accept(CssHelper.replaceProperty(pending, "-fx-fill:", css));
        }
        contentArea.requestFocus();
    }

    /** Updates the picker to reflect the colour at the selection / caret. */
    private void updateColorState() {
        if (getPendingCss.get() != null) {
            return;  // user has a pending choice
        }

        IndexRange sel = contentArea.getSelection();
        Color colour;
        if (sel.getLength() > 0) {
            // Selection: show the colour only if uniform.
            colour = selectionUniformColor(sel);
            if (colour == null) {
                // Mixed colours — show the transparent placeholder.
                showSentinelColor();
                return;
            }
        } else {
            // Caret: show the colour at the caret position.
            if (contentArea.getLength() == 0) {
                colour = DEFAULT_COLOR;
            } else {
                int pos = contentArea.getCaretPosition();
                pos = pos > 0 ? pos - 1 : 0;
                colour = CssHelper.parseColor(contentArea.getStyleAtPosition(pos));
                if (colour == null) {
                    colour = DEFAULT_COLOR;
                }
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
     * Returns the colour if every character in the selection shares it; {@code null} if colours are mixed.
     * Characters without an explicit colour are treated as the default.
     */
    private Color selectionUniformColor(IndexRange sel) {
        if (sel.getLength() == 0 || contentArea.getLength() == 0) {
            return null;
        }
        Color common = null;
        for (int p = sel.getStart(); p < sel.getEnd(); p++) {
            Color colour = CssHelper.parseColor(
                    contentArea.getStyleAtPosition(p));
            if (colour == null) {
                colour = DEFAULT_COLOR;
            }
            if (common == null) {
                common = colour;
            } else if (!common.equals(colour)) {
                return null;  // mixed colours
            }
        }
        return common;
    }

    /** Shows the transparent placeholder in the picker. */
    private void showSentinelColor() {
        suppressColorUpdate = true;
        try {
            colorPicker.setValue(SENTINEL_COLOR);
        } finally {
            suppressColorUpdate = false;
        }
    }
}
