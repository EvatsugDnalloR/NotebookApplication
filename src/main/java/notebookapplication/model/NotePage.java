package notebookapplication.model;

import java.beans.PropertyChangeSupport;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a note page in a note group in the notebook application.
 * This class extends {@link ModelObserver} and handles all the text operations
 *   such as inserting characters or pasting strings into the note page, deleting
 *   one character or strings selected, and also the string replacement.
 * Calls {@code support.firePropertyChange} to notify observers about the changes.
 */
public class NotePage extends ModelObserver {
    /** The name of this note page to be shown in the gui. */
    private String pageName;

    /** The text content of the whole note page. */
    private StringBuilder content;

    /**
     * Constructs a NotePage with the specified name and content.
     *
     * @param pageName  the name of the page
     * @param content   the initial content of the page
     */
    NotePage(String pageName, String content) {
        this.pageName = pageName;
        this.content = new StringBuilder(content);
        support = new PropertyChangeSupport(this);  // initialise the observer
    }

    /**
     * Inserts content at the specified caret position.
     * Handles both character typing inserting operation and string pasting
     *   operation.
     *
     * @param caretPosition the position where user chose to insert the content
     * @param content   the content to be inserted
     * @pre {@code caretPosition \in {0, ..., content.length()}}
     * @throws IllegalArgumentException if caretPosition is not inside the {@code content}
     *      string bounds
     */
    public void insertContent(int caretPosition, String content) {
        if (caretPosition < 0 || caretPosition > this.content.length()) {
            throw new IllegalArgumentException(
                    STR."Caret position \{caretPosition} is out of bounds");
        }

        String oldContent = getContent();
        this.content.insert(caretPosition, content);

        // notify the observer with Property Name "insert"
        support.firePropertyChange("insert", oldContent, getContent());
    }

    /**
     * Deletes content between the specified start and end positions.
     * Handles both deletion of character due to BACK_SPACE or DEL key, and also
     *   the deletion of a selected string.
     *
     * @param startPosition the start position of the content to be deleted
     * @param endPosition   the end position of the content to be deleted
     * @pre {@code startPosition, endPosition \in {0, ..., this.content.length()}} &&
     *      {@code startPosition < endPosition}
     * @throws IllegalArgumentException if precondition is violated
     */
    public void deleteContent(int startPosition, int endPosition) {
        if (startPosition < 0
                || startPosition >= endPosition
                || endPosition > this.content.length()) {
            throw new IllegalArgumentException(
                    "Position indexes for deletion is out of bounds");
        }

        String oldContent = getContent();
        this.content.delete(startPosition, endPosition);

        // notify the observer with Property Name "delete"
        support.firePropertyChange("delete", oldContent, this.content.toString());
    }

    /**
     * Replaces content between the specified positions with new content.
     * Handles the case where user selects a string and then replaces it by typing
     *   a character or pasting another string.
     *
     * @param startPosition the start position of the content to be replaced
     * @param endPosition   the end position of the content to be replaced
     * @param newContent    the new content to be inserted
     * @pre {@code startPosition, endPosition \in {0, this.content.length()}} &&
     *      {@code startPosition < endPosition}
     * @throws IllegalArgumentException if precondition is violated
     */
    public void replaceContent(int startPosition, int endPosition, String newContent) {
        if (startPosition < 0
                || startPosition >= endPosition
                || endPosition > this.content.length()) {
            throw new IllegalArgumentException(
                    "Position indexes for deletion is out of bounds");
        }

        String oldContent = getContent();
        content.replace(startPosition, endPosition, newContent);
        support.firePropertyChange("replace", oldContent, getContent());
    }

    /**
     * Inserts a symbol to the front of a chosen line of string.
     * Handles the case where user presses the button to insert some special symbols
     *   such as bullet points or checkboxes, etc.
     *
     * @param lineNum   the line number where the symbol will be inserted
     * @param symbolEnum    the enum containing the symbol to be inserted
     * @pre {@code lineNum \in {0, ..., lines.length - 1}}
     * @throws IllegalArgumentException if {@code lineNum} is out of bounds of {@code lines}
     * @post    {@code symbolEnum.symbol} is inserted to the front of the chosen line
     *      with two spaces between the symbol and the start of the text,
     *      while the other lines are not modified
     */
    public void insertSymbol(int lineNum, Symbols symbolEnum) {
        String oldContent = getContent();
        String[] lines = content.toString().split("\n");

        if (lineNum < 0 || lineNum >= lines.length) {
            throw new IllegalArgumentException(
                    STR."Line number \{lineNum} is out of bounds");
        }

        lines[lineNum] = STR."\{symbolEnum.symbol}  \{lines[lineNum]}";
        content = new StringBuilder(String.join("\n", lines));

        // notify the observer with Property Name "symbol"
        support.firePropertyChange("symbol", oldContent, getContent());
    }

    /**
     * Replace the selected string with the same string but formatted with the
     *      custom style tag specified in {@link TextEditing} class.
     * Only accepting a lambda expression that takes the original content as input
     *      and returns a formatted string for the parameter {@code stringSupplier}.
     *
     * <p>Example usage:</p>
     * <pre>{@code
     * NotePage notePage = new NotePage("pageName", "Some default content");
     * // the indexes parsed to the substring inside the lambda function should
     * // match the indexes parsed to the formatting function
     * notePage.formatting(5, 12,
     *      () -> TextEdit.setBold(notePage.getContent().substring(5, 12)));
     * System.out.println(notePage.getContent());
     * // should prints "Some [style="-fx-font-weight: bold;"]default[/style] content"
     * }</pre>
     *
     * @param startPosition the start position of the content to be formatted
     * @param endPosition   the end position of the content to be formatted
     * @param stringSupplier   the lambda function that returns the string
     *                         processed by one of the functions in {@link TextEditing}
     * @pre {@code startPosition, endPosition \in {0, ..., this.content.length()}} &&
     *      {@code startPosition < endPosition} && {@code stringSupplier.get()} contains the
     *      correct custom style tag
     * @throws IllegalArgumentException if any precondition is violated
     * @post the selected content is surrounded with the custom style tag,
     *      but the content itself is not modified
     */
    public void formatting(int startPosition,
                           int endPosition,
                           StringSupplier stringSupplier) {
        if (startPosition < 0
                || startPosition >= endPosition
                || endPosition > this.content.length()) {
            throw new IllegalArgumentException(
                    "Position indexes for deletion is out of bounds");
        }

        String formattedString = stringSupplier.get();
        // check if formattedString contains the correct custom style tag
        if (!matchTag(formattedString)) {
            throw new IllegalArgumentException("Unsupported formatting String");
        }

        String oldContent = getContent();
        content.replace(startPosition, endPosition, formattedString);

        // notify the observer with Property Name "format"
        support.firePropertyChange("format", oldContent, getContent());
    }

    /**
     * Private auxiliary method to help {@code formatting} function to check
     *      if the formatted string parsed in contains the correct
     *      custom style tag specified in {@link TextEditing} class.
     *
     * @param formattedString   the string that should contain the custom style tag
     * @return  true if {@code formattedString} matches the pattern, false otherwise
     */
    private boolean matchTag(String formattedString) {
        /*
        Create the pattern of [style="..."]...[/style], where no other quotation mark
            is allowed inside the first ellipsis.
        The (?s) flag ensures the ".*?" matches the possible new lines
            inside the second ellipsis.
         */
        Pattern pattern = Pattern.compile("(?s)\\[style=\"[^\"]*\"].*?\\[/style]");
        return pattern.matcher(formattedString).matches();
    }

    // getter for string content of this.content
    public String getContent() {
        return this.content.toString();
    }

    // setter for this.content
    public void setContent(String content) {
        this.content = new StringBuilder(content);
    }

    // getter for the page name
    public String getPageName() {
        return this.pageName;
    }

    /**
     * Setter for the page name.
     * Replace the page name with the new one, and also need to notify the observer.
     *
     * @param pageName  the page name to replace {@code this.pageName}
     */
    public void setPageName(String pageName) {
        // notify the observer with Property Name "pageName"
        support.firePropertyChange("pageName", this.pageName, pageName);

        this.pageName = pageName;
    }
}
