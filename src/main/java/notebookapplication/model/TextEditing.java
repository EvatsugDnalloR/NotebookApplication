package notebookapplication.model;

import java.text.MessageFormat;

/**
 * Utility class for text editing operations.
 * Provides methods to apply various styles to text such as
 *   colour, font, underline, bold, and italic.
 * Implemented by adding custom style tag to surround the text,
 *   such as {@code [style="..."]...[/style]}.
 */
public class TextEditing {
    /**
     * Sets the colour of the selected text.
     *
     * @param colorEnum the colour to be applied
     * @param selectedText  the text to be styled
     * @return  the styled text with the specified colour
     */
    public static String setColor(Colors colorEnum, String selectedText) {
        return MessageFormat.format("[style=\"-fx-fill: {0};\"]{1}[/style]",
                colorEnum.color, selectedText);
    }

    /**
     * Sets the font of the selected text.
     *
     * @param fontEnum  the font to be applied
     * @param selectedText  the text to be styled
     * @return  the styled text with the specified font
     */
    public static String setFont(Fonts fontEnum, String selectedText) {
//        return STR."[style=\"-fx-font-family: \{fontEnum.font};\"]\{selectedText}[/style]";
        return MessageFormat.format("[style=\"-fx-font-family: {0};\"]{1}[/style]",
                fontEnum.font, selectedText);
    }

    /**
     * Underlines the selected text.
     *
     * @param selectedText  the text to be styled
     * @return  the styled text with underline
     */
    public static String setUnderlined(String selectedText) {
        return MessageFormat.format("[style=\"-fx-underline: true;\"]{0}[/style]", selectedText);
    }

    /**
     * Sets the selected text to bold.
     *
     * @param selectedText  the text to be styled
     * @return  the styled text with bold font weight
     */
    public static String setBold(String selectedText) {
        return MessageFormat.format("[style=\"-fx-font-weight: bold;\"]{0}[/style]", selectedText);
    }

    /**
     * Sets the selected text to italic.
     *
     * @param selectedText  the text to be styled
     * @return  the styled text with italic font style
     */
    public static String setItalic(String selectedText) {
        return MessageFormat.format("[style=\"-fx-font-style: italic;\"{0}[/style]]", selectedText);
    }
}
