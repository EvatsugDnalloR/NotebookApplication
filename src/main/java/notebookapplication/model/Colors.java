package notebookapplication.model;

/**
 * A public enumerated class that contains all the strings
 *   of colours, which can be appended to the CSS style
 *   of colour setting in javafx, such as
 *   \code{-fx-fill: Colors.color}.
 */
public enum Colors {
    RED("red"),
    BLUE("blue"),
    GREEN("green"),
    YELLOW("yellow"),
    ORANGE("orange"),
    PURPLE("purple");

    public final String color;

    Colors(String color) {
        this.color = color;
    }
}
