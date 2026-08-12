package notebookapplication.gui;

import javafx.scene.control.CheckBox;
import javafx.scene.control.IndexRange;
import javafx.scene.control.Label;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import org.fxmisc.richtext.InlineCssTextArea;
import org.fxmisc.richtext.model.TwoDimensional.Bias;


/**
 * Wires the paragraph-level formatting toolbar: text alignment (left / centre / right)
 * and the list grouping features (bullet points, numbered lists, checkboxes).
 *
 * <p>Alignment and list style are stored as paragraph styles and rendered via the paragraph graphic factory.
 */
public class ParagraphStyle {
    private final InlineCssTextArea contentArea;
    private final RadioMenuItem leftAlign;
    private final RadioMenuItem centerAlign;
    private final RadioMenuItem rightAlign;
    private final ToggleButton bulletPoints;
    private final ToggleButton numberListing;
    private final ToggleButton checkBoxes;

    /**
     * ParagraphStyle helper class constructor.
     *
     * @param contentArea    the editor
     * @param leftAlign      left-align radio item
     * @param centerAlign    centre-align radio item
     * @param rightAlign     right-align radio item
     * @param bulletPoints   bullet-points toggle button
     * @param numberListing  numbered-list toggle button
     * @param checkBoxes     checkbox toggle button
     */
    public ParagraphStyle(InlineCssTextArea contentArea,
                          RadioMenuItem leftAlign, RadioMenuItem centerAlign, RadioMenuItem rightAlign,
                          ToggleButton bulletPoints, ToggleButton numberListing, ToggleButton checkBoxes) {
        this.contentArea = contentArea;
        this.leftAlign = leftAlign;
        this.centerAlign = centerAlign;
        this.rightAlign = rightAlign;
        this.bulletPoints = bulletPoints;
        this.numberListing = numberListing;
        this.checkBoxes = checkBoxes;
    }

    /** Wires all paragraph-level components managed by this helper. */
    public void setupAll() {
        setupTextAlignment();
        setupListFormatting();
    }

    // ---------------------------------------------------------------
    //  Text alignment
    // ---------------------------------------------------------------

    /** Wires the alignment menu (left/centre/right) to paragraph styles. */
    private void setupTextAlignment() {
        ToggleGroup group = new ToggleGroup();
        leftAlign.setToggleGroup(group);
        centerAlign.setToggleGroup(group);
        rightAlign.setToggleGroup(group);
        leftAlign.setSelected(true);  // default: left alignment

        leftAlign.setOnAction(_ -> applyAlignment("left"));
        centerAlign.setOnAction(_ -> applyAlignment("center"));
        rightAlign.setOnAction(_ -> applyAlignment("right"));

        contentArea.selectionProperty().addListener(
                (_, _, _) -> updateAlignmentState());
        contentArea.caretPositionProperty().addListener(
                (_, _, _) -> updateAlignmentState());
    }

    /** Applies an alignment to the caret paragraph, or to every paragraph the selection spans. */
    private void applyAlignment(String alignment) {
        IndexRange sel = contentArea.getSelection();
        int startPar = contentArea.offsetToPosition(sel.getStart(), Bias.Forward).getMajor();
        int endPar = contentArea.offsetToPosition(Math.max(sel.getStart(), sel.getEnd() - 1), Bias.Forward).getMajor();
        String css = "-fx-text-alignment: " + alignment + ";";
        for (int i = startPar; i <= endPar; i++) {
            String style = contentArea.getParagraphs().get(i).getParagraphStyle();
            String newStyle = CssHelper.replaceProperty(style, "-fx-text-alignment:", css);
            contentArea.setParagraphStyle(i, newStyle);
        }
        contentArea.requestFocus();
    }

    /** Syncs the radio items with the alignment of the caret paragraph. */
    private void updateAlignmentState() {
        int par = contentArea.getCurrentParagraph();
        String style = contentArea.getParagraphs().get(par).getParagraphStyle();
        String alignment = CssHelper.getExtractedString(style, "-fx-text-alignment:");
        if (alignment == null) {
            alignment = "left";  // default
        }
        leftAlign.setSelected("left".equals(alignment));
        centerAlign.setSelected("center".equals(alignment));
        rightAlign.setSelected("right".equals(alignment));
    }

    // ---------------------------------------------------------------
    //  Bullet points / numbered lists / checkboxes
    // ---------------------------------------------------------------

    /** Wires bullet/numbered/checkbox toggles and the paragraph graphic. */
    private void setupListFormatting() {
        ToggleGroup group = new ToggleGroup();
        bulletPoints.setToggleGroup(group);
        numberListing.setToggleGroup(group);
        checkBoxes.setToggleGroup(group);
        bulletPoints.setTooltip(new Tooltip("Bullet points"));
        numberListing.setTooltip(new Tooltip("Numbered list"));
        checkBoxes.setTooltip(new Tooltip("Checkboxes"));

        bulletPoints.setOnAction(_ -> applyListStyle(bulletPoints.isSelected() ? "bullet" : null));
        numberListing.setOnAction(_ -> applyListStyle(numberListing.isSelected() ? "decimal" : null));
        checkBoxes.setOnAction(_ -> applyListStyle(checkBoxes.isSelected() ? "checkbox" : null));

        contentArea.selectionProperty().addListener(
                (_, _, _) -> updateListState());
        contentArea.caretPositionProperty().addListener(
                (_, _, _) -> updateListState());

        contentArea.setParagraphGraphicFactory(
                this::createParagraphGraphic);
    }

    /** Applies a list style to the caret paragraph / selected paragraphs; {@code null} clears the list style. */
    private void applyListStyle(String listStyle) {
        IndexRange sel = contentArea.getSelection();
        int startPar = contentArea.offsetToPosition(sel.getStart(), Bias.Backward).getMajor();
        int endPar = sel.getLength() > 0
                ? contentArea.offsetToPosition(sel.getEnd() - 1, Bias.Forward).getMajor()
                : startPar;
        String css = listStyle == null ? "" : "-fx-list-style: " + listStyle + ";";
        for (int i = startPar; i <= endPar; i++) {
            String style = contentArea.getParagraphs().get(i).getParagraphStyle();
            String newStyle = CssHelper.replaceProperty(style, "-fx-list-style:", css);
            contentArea.setParagraphStyle(i, newStyle);
            contentArea.getUndoManager().preventMerge();
        }
        contentArea.requestFocus();
    }

    /** Syncs the list toggle buttons with the caret paragraph. */
    private void updateListState() {
        int par = contentArea.getCurrentParagraph();
        if (par < 0 || par >= contentArea.getParagraphs().size()) {
            return;
        }
        String style = contentArea.getParagraphs().get(par).getParagraphStyle();
        String ls = CssHelper.getExtractedString(style, "-fx-list-style:");
        bulletPoints.setSelected("bullet".equals(ls));
        numberListing.setSelected("decimal".equals(ls));
        checkBoxes.setSelected("checkbox".equals(ls));
    }

    /**
     * If the caret is in an empty first paragraph that has a list style, clears the style.
     * The first paragraph cannot be merged upward with Backspace,
     * so without this the grouping marker would be stuck forever.
     *
     * @return true if the style was cleared (event should be consumed)
     */
    public boolean clearFirstParagraphListStyleOnBackspace() {
        if (contentArea.getCurrentParagraph() != 0) {
            return false;
        }
        if (contentArea.getParagraphs().getFirst().length() != 0) {
            return false;
        }
        if (contentArea.getCaretPosition() != 0) {
            return false;
        }
        String style = contentArea.getParagraphs().getFirst().getParagraphStyle();
        if (CssHelper.getExtractedString(style, "-fx-list-style:") == null) {
            return false;
        }
        applyListStyle(null);
        return true;
    }

    /** Builds the leading graphic for a paragraph: bullet marker,
     * auto-numbered label, or a clickable checkbox. */
    private javafx.scene.Node createParagraphGraphic(int parIndex) {
        if (parIndex < 0 || parIndex >= contentArea.getParagraphs().size()) {
            return null;
        }
        String style = contentArea.getParagraphs().get(parIndex).getParagraphStyle();
        String listStyle = CssHelper.getExtractedString(style, "-fx-list-style:");
        if (listStyle == null) {
            return null;  // plain paragraph — no graphic
        }
        switch (listStyle) {
            case "bullet": {
                Label bullet = new Label("•  ");
                bullet.setTranslateY(2);
                return bullet;
            }
            case "decimal": {
                Label number = new Label(countDecimalBefore(parIndex) + ".  ");
                number.setTranslateY(2);
                return number;
            }
            case "checkbox": {
                CheckBox cb = new CheckBox();
                String checked = CssHelper.getExtractedString(style, "-fx-checked:");
                cb.setSelected("true".equals(checked));
                cb.setOnAction(_ -> {
                    String current = contentArea.getParagraphs().get(parIndex).getParagraphStyle();
                    String newStyle = CssHelper.replaceProperty(
                            current == null ? "" : current,
                            "-fx-checked:",
                            "-fx-checked: " + cb.isSelected() + ";");
                    contentArea.setParagraphStyle(parIndex, newStyle);
                    contentArea.getUndoManager().preventMerge();
                });
                return cb;
            }
            default:
                return null;
        }
    }

    /**
     * Counts the sequence number of a numbered paragraph:
     *      increments through preceding consecutive decimal paragraphs,
     *      resets after any non-decimal (or plain) paragraph.
     */
    private int countDecimalBefore(int parIndex) {
        int count = 1;
        for (int i = 0; i < parIndex; i++) {
            String style = contentArea.getParagraphs().get(i).getParagraphStyle();
            String ls = CssHelper.getExtractedString(style, "-fx-list-style:");
            if ("decimal".equals(ls)) {
                count++;
            } else if (ls != null) {
                count = 1;  // other list type breaks the sequence
            }
        }
        return count;
    }
}
