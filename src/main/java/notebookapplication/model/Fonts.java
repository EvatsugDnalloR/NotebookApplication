package notebookapplication.model;

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
