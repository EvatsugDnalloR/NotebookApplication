package notebookapplication.model;

/**
 * A public enumerated class that contains strings of
 *   text editing symbols that can be inserted to other
 *   strings.
 */
public enum Symbols {
    BULLET_POINT("•"),
    HALLOW_BULLET_POINT("◦"),
    SQUARE_BULLET_POINT("▪"),
    HALLOW_SCARE_BULLET_POINT("▫"),
    UNIVERSAL_BULLET_POINT("∙"),
    ARROW("➤"),
    STAR("★"),
    CHECK_BOX("\\check_box"); // will be using built in javafx checkbox

    public final String symbol;

    Symbols(String symbol) {
        this.symbol = symbol;
    }
}
