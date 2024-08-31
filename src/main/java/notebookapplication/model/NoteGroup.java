package notebookapplication.model;

import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a group of note pages in the notebook application.
 * This class extends {@link ModelObserver} and handles operations related to note pages
 *      such as adding, deleting, and reordering pages.
 * Calls {@code support.firePropertyChange} to notify observers about the changes.
 */
public class NoteGroup extends ModelObserver {
    /** The name of the note group. */
    private String groupName;

    /** The list that contains all the note pages in this note group. */
    private List<NotePage> notePages;

    /**
     * A default constructor for a NoteGroup when the page contents
     *      read from the scanner is empty.
     *
     * @param groupName the name of the NoteGroup
     * @pre note page content should be empty
     */
    NoteGroup(String groupName) {
        this.groupName = groupName;
        notePages = new ArrayList<>();
        support = new PropertyChangeSupport(this);  // initialise the observer
    }

    /**
     * Constructs a NoteGroup with the specified group name and initialises it with note pages
     *      parsed from the provided Scanner input.
     * The input should follow the custom page tag:
     * <p> {@code [page="page name"]content[/page]} </p>
     *
     * @param groupName the name of the note group
     * @param scanner   the Scanner containing the input to be parsed
     * @pre {@code scanner != null} && {@code !input.isEmpty()} && no rubbish info outside
     *      any page tag
     * @throws NullPointerException if {@code scanner == null}
     * @throws IllegalArgumentException if {@code input.isEmpty()} || there is rubbish info
     *      that makes not the entire input to be matched with the page tag pattern
     */
    NoteGroup(String groupName, final Scanner scanner) {
        this.groupName = groupName;
        notePages = new ArrayList<>();
        support = new PropertyChangeSupport(this);

        if (scanner == null) {
            throw new NullPointerException("Scanner cannot be null");
        }

        if (!scanner.hasNext()) {
            throw new IllegalArgumentException("Empty note should not use this constructor");
        }

        // reads the entire input from the scanner
        String input = scanner.useDelimiter("\\A").next();

        // match the custom page tags
        Matcher matcher = Pattern.compile("(?s)\\[page=\"([^\"]*)\"](.*?)\\[/page]")
                .matcher(input);

        int lastMatchEnd = 0;
        while (matcher.find()) {
            // check if no rubbish info between two page tags
            if (matcher.start() != lastMatchEnd) {
                throw new IllegalArgumentException("Input does not follow the required pattern");
            }

            notePages.add(new NotePage(matcher.group(1), matcher.group(2)));
            lastMatchEnd = matcher.end();
        }

        // Check if the entire input was matched, i.e. no rubbish info at the start or the end
        if (lastMatchEnd != input.length()) {
            throw new IllegalArgumentException("Input does not follow the required pattern");
        }
    }

    /**
     * Adds a new note page to the note group.
     *
     * @param notePage  the NotePage to be added
     * @pre {@code notePage != null}
     * @throws NullPointerException if {@code notePage == null}
     */
    public void addPage(NotePage notePage) {
        if (notePage == null) {
            throw new NullPointerException("NotePage to be added cannot be null");
        }

        var oldNotePages = this.notePages;
        notePages.add(notePage);
        support.firePropertyChange("newPage", oldNotePages, notePages);
    }

    /**
     * Deletes a note page from the note group at the specified position.
     *
     * @param position  the position of the NotePage to be deleted
     * @pre {@code position \in {0, ..., notePages.size() - 1}}
     * @throws IllegalArgumentException if the precondition is violated
     */
    public void deletePage(int position) {
        if (position < 0 || position >= notePages.size()) {
            throw new IllegalArgumentException("Position is out of bounds");
        }

        var oldNotePages = this.notePages;
        notePages.remove(position);
        support.firePropertyChange("deletePage", oldNotePages, notePages);
    }

    /**
     * Changes the order of the note pages in the note group.
     *
     * @param currentPosition   the current position of the NotePage to be moved
     * @param newPosition   the new position of the NotePage
     * @pre {@code currentPosition, newPosition \in {0, ..., notePages.size() - 1}}
     * @throws IllegalArgumentException if the precondition is violated
     */
    public void changePagesOrder(int currentPosition, int newPosition) {
        if (currentPosition < 0 || currentPosition >= notePages.size()
                || newPosition < 0 || newPosition >= notePages.size()) {
            throw new IllegalArgumentException("Position is out of bounds");
        }

        var oldNotePages = this.notePages;
        NotePage toBeMoved = notePages.remove(currentPosition); // Get the NotePage to be moved
        notePages.add(newPosition, toBeMoved);
        support.firePropertyChange("changeOrder", oldNotePages, notePages);
    }

    // getter of the group name
    public String getGroupName() {
        return groupName;
    }

    /**
     * Setter for the groupName.
     * Replace the groupName with the new one, and also need to notify the observer.
     *
     * @param groupName the new groupName
     */
    public void setGroupName(String groupName) {
        support.firePropertyChange("groupName", this.groupName, groupName);
        this.groupName = groupName;
    }

    // getter of the notePages list
    public List<NotePage> getNotePages() {
        return notePages;
    }

    // setter of the notePages list
    public void setNotePages(List<NotePage> notePages) {
        this.notePages = notePages;
    }
}
