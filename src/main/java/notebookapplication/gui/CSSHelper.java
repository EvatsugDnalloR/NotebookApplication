package notebookapplication.gui;


/**
 * Static utility methods for manipulating inline CSS style strings used
 * by RichTextFX's {@link org.fxmisc.richtext.InlineCssTextArea}.
 *
 * <p>Formatting toggle buttons (bold, italic, underline),
 * accumulate pending CSS across multiple button presses with no text selected.
 * When the user finally types, the accumulated CSS is applied to the inserted characters.
 */
public final class CSSHelper {

    /** Property pairs that conflict (ON / OFF variants). */
    private static final String[][] CONFLICT_PAIRS = {
            {"-fx-font-weight: bold;", "-fx-font-weight: normal;"},
            {"-fx-font-style: italic;", "-fx-font-style: normal;"},
            {"-fx-underline: true;", "-fx-underline: false;"},
    };

    /** Appends a CSS property to the style string if it is not already present. */
    public static String ensureProperty(String style, String prop) {
        if (style == null || style.isEmpty()) {
            return prop;
        }
        if (style.contains(prop)) {
            return style;
        }
        return style + " " + prop;
    }

    /**
     * Removes a CSS property from the style string if present;
     * collapses double spaces.
     */
    public static String stripProperty(String style, String prop) {
        if (style == null) {
            return "";
        }
        return style.replace(prop, "").replaceAll(" +", " ").trim();
    }

    /**
     * Merges two CSS style strings.
     * The {@code add} values take precedence where there is overlap.
     */
    public static String mergeCss(String base, String add) {
        if (base == null || base.isBlank()) {
            return add;
        }
        if (add == null || add.isBlank()) {
            return base;
        }
        return base.trim() + " " + add.trim();
    }



    /**
     * Strips CSS properties from {@code base} that conflict with{@code pending}.
     * If {@code pending} contains one member of a conflict pair, both members are removed from {@code base}.
     */
    public static String stripConflicting(String base, String pending) {
        if (base == null || base.isBlank() || pending == null || pending.isBlank()) {
            return base;
        }
        String result = base;
        for (String[] pair : CONFLICT_PAIRS) {
            if (pending.contains(pair[0]) || pending.contains(pair[1])) {
                result = result.replace(pair[0], "").replace(pair[1], "");
            }
        }
        return result.replaceAll(" +", " ").trim();
    }
}
