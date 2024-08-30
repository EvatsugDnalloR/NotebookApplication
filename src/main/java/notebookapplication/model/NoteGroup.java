package notebookapplication.model;

import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class NoteGroup extends ModelObserver{
    /** The name of the note group. */
    private String groupName;

    /** The list that contains all the note pages in this note group. */
    private List<NotePage> notePages;

    /**
     * Containing the page names and content of all the note pages
     *      inside {@code notePages} list.
     * The content and page names should be updated simultaneously
     *      when it is updated in the gui.
     * Useful for saving file and exporting file functionalities.
     */
    private StringBuilder groupContent;

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
        groupContent = new StringBuilder();
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
        groupContent = new StringBuilder();
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
            // add
            groupContent.append(STR."[page=\"\{notePages.getLast().getPageName()}\"]")
                    .append(STR."\{notePages.getLast().getContent()}[/page]");
            lastMatchEnd = matcher.end();
        }

        // Check if the entire input was matched, i.e. no rubbish info at the start or the end
        if (lastMatchEnd != input.length()) {
            throw new IllegalArgumentException("Input does not follow the required pattern");
        }
    }

    /**
     *
     * @param notePage
     */
    public void addPage(NotePage notePage) {
        var oldNotePages = this.notePages;
        notePages.add(notePage);
        groupContent.append(STR."[page=\"\{notePage.getPageName()}\"]\{notePage.getContent()}[/page]");
        support.firePropertyChange("newPage", oldNotePages, notePages);
    }

    /**
     *
     * @param position
     */
    public void deletePage(int position) {
        if (position < 0 || position >= notePages.size()) {
            throw new IllegalArgumentException("Position is out of bounds");
        }

        var oldNotePages = this.notePages;
        NotePage toBeDeleted = notePages.get(position);
        notePages.remove(position);

        // To remove the corresponding content from groupContent
        String pageTag = STR."[page=\"\{toBeDeleted.getPageName()}\"]\{toBeDeleted.getContent()}[/page]";

        // Find the correct occurrence of the pageTag, which is {position + 1}-th page tag
        int startIndex = -1;
        for (int i = 1; i < position; i++) {
            startIndex = groupContent.indexOf(pageTag, startIndex + 1);
            if (startIndex == -1) {
                throw new IllegalStateException("Page tag not found in groupContent");
            }
        }

        // Delete the correct occurrence of the pageTag
        groupContent.delete(startIndex, startIndex + pageTag.length());
        support.firePropertyChange("deletePage", oldNotePages, notePages);
    }

    /**
     *
     * @param currentPosition
     * @param newPosition
     */
    public void changePagesOrder(int currentPosition, int newPosition) {
        if (currentPosition < 0 || currentPosition >= notePages.size() ||
                newPosition < 0 || newPosition >= notePages.size()) {
            throw new IllegalArgumentException("Position is out of bounds");
        }

        var oldNotePages = this.notePages;

        // Get the NotePage to be moved
        NotePage toBeMoved = notePages.remove(currentPosition);
        notePages.add(newPosition, toBeMoved);

        // Rebuild the groupContent based on the new order of notePages
        groupContent = new StringBuilder(); // Clear the existing content
        for (var page : notePages) {
            groupContent.append(STR."[page=\"\{page.getPageName()}\"]\{page.getContent()}[/page]");
        }

        // Notify the observer
        support.firePropertyChange("changeOrder", oldNotePages, notePages);
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<NotePage> getNotePages() {
        return notePages;
    }

    public void setNotePages(List<NotePage> notePages) {
        this.notePages = notePages;
    }

    public String getGroupContent() {
        return groupContent.toString();
    }

    public void setGroupContent(String groupContent) {
        this.groupContent = new StringBuilder(groupContent);
    }
}
