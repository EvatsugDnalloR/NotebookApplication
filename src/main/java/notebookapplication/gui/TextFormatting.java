package notebookapplication.gui;

import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.scene.control.Button;
import javafx.scene.control.IndexRange;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCode;
import notebookapplication.model.NotePage;
import org.fxmisc.richtext.InlineCssTextArea;


/**
 * Wires the character-level formatting toolbar: bold / italic / underline toggles, cut / copy / paste,
 * and the text-level undo/redo buttons (with a safety net for RichTextFX undo-stack inconsistencies).
 *
 * <p>Shared mutable state ({@code pendingCss} and the current {@code NotePage}) is injected
 * through functional interfaces so this helper stays decoupled from the controller.
 */
public class TextFormatting {
    private static final Logger LOGGER = Logger.getLogger(TextFormatting.class.getName());

    private final InlineCssTextArea contentArea;
    private final Button textUndo;
    private final Button textRedo;
    private final Button cut;
    private final Button copy;
    private final Button paste;
    private final ToggleButton bold;
    private final ToggleButton italic;
    private final ToggleButton underline;
    private final Supplier<String> getPendingCss;
    private final Consumer<String> setPendingCss;
    private final Supplier<NotePage> getCurrentPage;

    /**
     * TextFormatting helper class constructor.
     *
     * @param contentArea    the editor
     * @param textUndo       toolbar undo button
     * @param textRedo       toolbar redo button
     * @param cut            toolbar cut button
     * @param copy           toolbar copy button
     * @param paste          toolbar paste button
     * @param bold           bold toggle button
     * @param italic         italic toggle button
     * @param underline      underline toggle button
     * @param getPendingCss  reads the shared pending-CSS buffer
     * @param setPendingCss  writes the shared pending-CSS buffer
     * @param getCurrentPage supplies the page currently being edited
     */
    public TextFormatting(InlineCssTextArea contentArea,
                          Button textUndo, Button textRedo, Button cut, Button copy, Button paste,
                          ToggleButton bold, ToggleButton italic, ToggleButton underline,
                          Supplier<String> getPendingCss, Consumer<String> setPendingCss,
                          Supplier<NotePage> getCurrentPage) {
        this.contentArea = contentArea;
        this.textUndo = textUndo;
        this.textRedo = textRedo;
        this.cut = cut;
        this.copy = copy;
        this.paste = paste;
        this.bold = bold;
        this.italic = italic;
        this.underline = underline;
        this.getPendingCss = getPendingCss;
        this.setPendingCss = setPendingCss;
        this.getCurrentPage = getCurrentPage;
    }

    /** Wires all toolbar components managed by this helper. */
    public void setupAll() {
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
    }

    // ---------------------------------------------------------------
    //  Text undo / redo
    // ---------------------------------------------------------------

    /** Undo with a safety net for RichTextFX undo-stack inconsistencies. */
    public void safeTextUndo() {
        try {
            contentArea.getUndoManager().undo();
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Text undo history inconsistent — resetting", e);
            recoverAfterUndoError();
        }
    }

    /** Redo with a safety net for RichTextFX undo-stack inconsistencies. */
    public void safeTextRedo() {
        try {
            contentArea.getUndoManager().redo();
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Text redo history inconsistent — resetting", e);
            recoverAfterUndoError();
        }
    }

    /** Clears the broken undo history and reloads the page content. */
    private void recoverAfterUndoError() {
        contentArea.getUndoManager().forgetHistory();
        NotePage page = getCurrentPage.get();
        if (page != null) {
            HtmlBridge.populateArea(contentArea, page.toHtml());
        }
    }

    @SuppressWarnings("unchecked")
    private void setupTextUndoRedo() {
        textUndo.setTooltip(new Tooltip("Undo (Ctrl+Z)"));
        textRedo.setTooltip(new Tooltip("Redo (Ctrl+Y)"));

        textUndo.setOnAction(_ -> safeTextUndo());
        textRedo.setOnAction(_ -> safeTextRedo());

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
    //  Cut / copy / paste
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
                    if (focused) {
                        updatePasteState();
                    }
                });
    }

    private void updatePasteState() {
        paste.setDisable(!Clipboard.getSystemClipboard().hasString());
    }

    // ---------------------------------------------------------------
    //  Bold / italic / underline
    // ---------------------------------------------------------------

    private void setupToggleFormatting(ToggleButton btn, String cssOn, String cssOff, String label, KeyCode hotkey) {
        btn.setTooltip(new Tooltip(label + " (Ctrl+" + hotkey.getName() + ")"));

        btn.setOnAction(_ -> {
            IndexRange sel = contentArea.getSelection();
            String add = btn.isSelected() ? cssOn : cssOff;
            String remove = btn.isSelected() ? cssOff : cssOn;
            if (sel.getLength() > 0) {
                String current = CssHelper.getCurrentStyle(sel, contentArea);
                String newStyle = CssHelper.ensureProperty(current, add);
                newStyle = CssHelper.stripProperty(newStyle, remove);
                contentArea.setStyle(sel.getStart(), sel.getEnd(), newStyle);
                contentArea.getUndoManager().preventMerge();
            } else {
                String pending = getPendingCss.get();
                if (pending == null) {
                    pending = "";
                }
                pending = CssHelper.ensureProperty(pending, add);
                pending = CssHelper.stripProperty(pending, remove);
                setPendingCss.accept(pending);
                btn.setSelected(pending.contains(cssOn));
            }
            contentArea.requestFocus();
        });

        contentArea.selectionProperty().addListener(
                (_, _, _) -> updateToggleState(btn, cssOn));
        contentArea.caretPositionProperty().addListener(
                (_, _, _) -> updateToggleState(btn, cssOn));
    }

    private void updateToggleState(ToggleButton btn, String cssOn) {
        String pending = getPendingCss.get();
        if (pending != null) {
            btn.setSelected(pending.contains(cssOn));
            return;
        }

        IndexRange sel = contentArea.getSelection();
        int pos = sel.getLength() > 0 ? sel.getStart() : contentArea.getCaretPosition();
        pos = pos > 0 ? pos - 1 : 0;

        String style = contentArea.getLength() == 0 ? "" : contentArea.getStyleAtPosition(pos);
        btn.setSelected(style != null && style.contains(cssOn));
    }
}
