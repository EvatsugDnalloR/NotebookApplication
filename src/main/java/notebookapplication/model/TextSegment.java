package notebookapplication.model;

import java.io.Serializable;

/**
 * Immutable record representing a segment of text with consistent styling.
 * Supports serialization for saving and loading notebook content.
 */
public record TextSegment(String text, String cssStyle) implements Serializable {
    /**
     * Factory method for creating a styled text segment with specified formatting.
     *
     * @param text the text content
     * @param fontFamily the font family for styling
     * @param fontSize the font size in pixels
     * @param color the text colour
     * @param bold whether the text should be bold
     * @param italic whether the text should be italic
     * @param underline whether the text should be underlined
     * @return a new TextSegment with the specified text and CSS styling
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
