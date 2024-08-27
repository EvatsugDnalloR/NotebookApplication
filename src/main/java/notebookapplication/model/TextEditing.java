package notebookapplication.model;

import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.List;

public class TextEditing {
    public static String setColor(Colors colorEnum, String selectedText){
        return STR."[style=\"-fx-fill: \{colorEnum.color};\"]\{selectedText}[/style]";
    }

    public static String setFont(Fonts fontEnum, String selectedText) {
        return STR."[style=\"-fx-font-family: \{fontEnum.font};\"]\{selectedText}[/style]";
    }

    public static String setUnderlined(String selectedText) {
        return STR."[style=\"-fx-underline: true;\"]\{selectedText}[/style]";
    }

    public static String setBold(String selectedText) {
        return STR."[style=\"-fx-font-weight: bold;\"]\{selectedText}[/style]";
    }

    public static String setItalic(String selectedText) {
        return STR."[style=\"-fx-font-style: italic;\"\{selectedText}[/style]]";
    }

}
