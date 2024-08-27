package notebookapplication.model;

import java.beans.PropertyChangeSupport;

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
     * @pre {@code caretPosition \in {0, ..., content.length() - 1}}
     * @throws IllegalArgumentException if caretPosition is not inside the {@code content}
     *      string bounds
     */
    public void insertContent(int caretPosition, String content) {
        if (caretPosition < 0 || caretPosition > content.length()) {
            throw new IllegalArgumentException(
                    STR."Caret position \{caretPosition} is out of bounds");
        }

        String oldContent = this.content.toString();
        this.content.insert(caretPosition, content);

        // notify the observer, sending both old and new values
        support.firePropertyChange("content", oldContent, this.content.toString());
    }

    /**
     * Deletes content between the specified start and end positions.
     * Handles both deletion of character due to BACK_SPACE or DEL key, and also
     *   the deletion of a selected string.
     *
     * @param startPosition the start position of the content to be deleted
     * @param endPosition   the end position of the content to be deleted
     * @pre {@code startPosition, endPosition \in {0, ..., this.content.length() - 1}} &&
     *      {@code startPosition < endPosition}
     * @throws IllegalArgumentException if precondition is violated
     */
    public void deleteContent(int startPosition, int endPosition) {
        if (startPosition < 0
                || startPosition >= endPosition
                || endPosition >= this.content.length()) {
            throw new IllegalArgumentException("Position indexes for deletion is out of bounds");
        }

        String oldContent = this.content.toString();
        this.content.delete(startPosition, endPosition);

        // notify the observer
        support.firePropertyChange("content", oldContent, this.content.toString());
    }

    /**
     * Replaces content between the specified positions with new content.
     * Handles the case where user selects a string and then replaces it by typing
     *   a character or pasting another string.
     *
     * @param startPosition the start position of the content to be replaced
     * @param endPosition   the end position of the content to be replaced
     * @param newContent    the new content to be inserted
     * @pre satisfy the preconditions of both {@code deleteContent} and {@code insertContent} method
     * @throws IllegalArgumentException if precondition of {@code deleteContent} or
     *      {@code insertContent} has been violated
     */
    public void replaceContent(int startPosition, int endPosition, String newContent) {
        deleteContent(startPosition, endPosition);
        insertContent(startPosition, newContent);
    }

    /**
     * Inserts a symbol to the front of a chosen line of string.
     * Handles the case where user presses the button to insert some special symbols
     *   such as bullet points or checkboxes, etc.
     *
     * @param lineNum   the line number where the symbol will be inserted
     * @param symbolEnum    the enum containing the symbol to be inserted
     * @pre {@code lineNum \in {0, ..., lines.length - 1}}
     * @throws IllegalArgumentException if {@code lineNum} is out of bounds of lines
     * @post    {@code symbolEnum.symbol} is inserted to the front of the chosen line
     *      with two spaces between the symbol and the start of the text,
     *      while the other lines are not modified
     */
    public void insertSymbol(int lineNum, Symbols symbolEnum) {
        String oldContent = this.content.toString();
        String[] lines = content.toString().split("\n");

        if (lineNum < 0 || lineNum >= lines.length) {
            throw new IllegalArgumentException(STR."Line number \{lineNum} is out of bounds");
        }

        lines[lineNum] = STR."\{symbolEnum.symbol}  \{lines[lineNum]}";
        content = new StringBuilder(String.join("\n", lines));
        support.firePropertyChange("content", oldContent, content.toString());
    }

    // getter for string content of this.content
    public String getContent() {
        return content.toString();
    }

    // setter for this.content
    public void setContent(String content) {
        this.content = new StringBuilder(content);
    }

    // getter for the page name
    public String getPageName() {
        return pageName;
    }

    // setter for the page name
    public void setPageName(String pageName) {
        this.pageName = pageName;
    }
}
