package notebookapplication.model;

public enum Symbols {
    BULLET_POINT("•"),
    HALLOW_BULLET_POINT("◦"),
    SQUARE_BULLET_POINT("▪"),
    HALLOW_SCARE_BULLET_POINT("▫"),
    UNIVERSAL_BULLET_POINT("∙"),
    ARROW("➤"),
    STAR("★"),
    CHECK_BOX("\\check_box");

    public final String symbol;

    Symbols(String symbol) {
        this.symbol = symbol;
    }
}
