package notebookapplication.model;

import java.io.Serializable;

/**
 * Represents a segment of text with consistent styling.
 */
public record TextSegment(String text, String cssStyle) implements Serializable {
    /**
     * Creates a segment with specified styling properties.
     *
     * @param text
     * @param fontFamily
     * @param fontSize
     * @param color
     * @param bold
     * @param italic
     * @param underline
     * @return
     */
    public static TextSegment createStyledSegment(
            String text, String fontFamily,
            int fontSize, String color,
            boolean bold, boolean italic, boolean underline) {

        StringBuilder style = new StringBuilder();
        style.append("font-family: '").append(fontFamily).append("'; ");
        style.append("font-size: ").append(fontSize).append("px; ");
        style.append("color: ").append(color).append("; ");

        if (bold) style.append("font-weight: bold; ");
        if (italic) style.append("font-style: italic; ");
        if (underline) style.append("text-decoration: underline; ");

        return new TextSegment(text, style.toString());
    }
}
