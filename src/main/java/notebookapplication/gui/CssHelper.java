package notebookapplication.gui;


import javafx.scene.paint.Color;

/**
 * Static utility methods for manipulating inline CSS style strings used
 * by RichTextFX's {@link org.fxmisc.richtext.InlineCssTextArea}.
 *
 * <p>Formatting toggle buttons (bold, italic, underline),
 * accumulate pending CSS across multiple button presses with no text selected.
 * When the user finally types, the accumulated CSS is applied to the inserted characters.
 */
public final class CssHelper {

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
     * Replaces any CSS property matching {@code prefix} with {@code newProperty}.
     * Used for value-replacement properties (font-size, font-family, colour),
     * where the new value should replace the old one rather than stack.
     *
     * @param style       the CSS style string
     * @param prefix      the CSS property prefix (e.g. {@code "-fx-font-size:"})
     * @param newProperty the replacement property (pass {@code ""} to remove)
     * @return the style with the old property replaced
     */
    public static String replaceProperty(String style, String prefix, String newProperty) {
        if (style == null || style.isBlank()) {
            return newProperty;
        }
        String result = style.replaceAll(
                java.util.regex.Pattern.quote(prefix) + "[^;]*;",
                "");
        result = result.replaceAll(" +", " ").trim();
        if (result.isEmpty()) {
            return newProperty;
        }
        if (newProperty.isEmpty()) {
            return result;
        }
        return result + " " + newProperty;
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

    /** Extracts the numeric font size from a CSS style string. */
    public static Double parseFontSize(String style) {
        String value = getExtractedString(style, "-fx-font-size:");
        if (value == null) {
            return null;
        }
        value = value.replaceAll("(?i)(pt|px|em)$", "").trim(); // strip a trailing unit (pt, px, em)
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Extracts the fill colour from a CSS style string. */
    public static Color parseColor(String style) {
        String value = getExtractedString(style, "-fx-fill:");
        if (value == null) {
            return null;
        }
        try {
            return javafx.scene.paint.Color.web(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Extracts the value of a CSS property from a style string.
     *
     * <p>Locates the first occurrence of {@code prefix} (e.g. {@code "-fx-text-alignment:"})
     * and returns everything from the end of the prefix up to the next {@code ';'}
     * (or the end of the string), trimmed.
     * Returns {@code null} if the prefix is absent.
     *
     * @param style  the CSS style string to search, or {@code null}
     * @param prefix the CSS property prefix, including the trailing colon (e.g. {@code "-fx-fill:"})
     * @return the property value (trimmed), or {@code null} if the property is not present
     */
    public static String getExtractedString(String style, String prefix) {
        if (style == null) {
            return null;
        }
        int idx = style.indexOf(prefix);
        if (idx == -1) {
            return null;
        }
        int start = idx + prefix.length();
        int end = style.indexOf(";", start);
        if (end == -1) {
            end = style.length();
        }
        return style.substring(start, end).trim();
    }

    /** Converts a colour to {@code #RRGGBB} CSS form. */
    public static String colorToHex(Color color) {
        int r = (int) Math.round(color.getRed() * 255);
        int g = (int) Math.round(color.getGreen() * 255);
        int b = (int) Math.round(color.getBlue() * 255);
        return String.format("#%02X%02X%02X", r, g, b);
    }
}
