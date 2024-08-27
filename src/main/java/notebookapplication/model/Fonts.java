package notebookapplication.model;

/**
 * A public enumerated class that contains strings of
 *   fonts, which can be appended to the CSS style of
 *   font setting in javafx, such as {@code -fx-font-family: Fonts.font}.
 */
public enum Fonts {
    ARIAL("Arial"),
    VERDANA("Verdana"),
    GEORGIA("Georgia"),
    TIMES_NEW_ROMAN("Times New Roman"),
    COMIC_SANS_MS("Comic Sans MS"),
    MS_YAHEI("Microsoft YaHei UI");

    public final String font;

    Fonts(String font) {
        this.font = font;
    }
}
