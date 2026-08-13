package notebookapplication.model;

import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * Represents a collection of note pages organised together.
 *
 * <p>Implemented model-observer pattern by property change
 * notifications, for UI synchronisation.
 */
public class NoteGroup extends NoteSubject implements Serializable {
    private final UUID id;
    private String groupName;
    private final List<NotePage> pages;

    /** Creates a new note group with a default name and one empty page. */
    public NoteGroup() {
        this.id = UUID.randomUUID();
        this.groupName = "Untitled Group";
        this.pages = new ArrayList<>();
        this.pages.add(new NotePage()); // default first page
        support = new PropertyChangeSupport(this);
    }

    /**
     * Creates a note group with specified properties.
     *
     * @param id        the unique identifier for the group
     * @param groupName the name of the group
     * @param pages     the list of pages in this group
     */
    public NoteGroup(UUID id, String groupName, ArrayList<NotePage> pages) {
        this.id = id;
        this.groupName = groupName;
        this.pages = pages;
        support = new PropertyChangeSupport(this);
    }

    public UUID getId() {
        return id;
    }

    public String getGroupName() {
        return groupName;
    }

    /**
     * Updates the group name and notifies registered listeners.
     *
     * @param groupName the new name for the group
     */
    public void setGroupName(String groupName) {
        String oldName = this.groupName;
        this.groupName = groupName;
        support.firePropertyChange(EventPropertyNameEnum.GROUP_RENAME.getPropertyName(), oldName, this.groupName);
    }

    public List<NotePage> getPages() {
        return new ArrayList<>(pages);
    }

    /**
     * Adds a page to this group and notifies registered listeners.
     *
     * @param page the page to add to this group
     */
    public void addPage(NotePage page) {
        pages.add(page);
        support.firePropertyChange(EventPropertyNameEnum.ADD_PAGE.getPropertyName(), null, page);
    }

    /**
     * Removes a page from this group and notifies registered listeners.
     *
     * @param page the page to remove from this group
     */
    public void removePage(NotePage page) {
        pages.remove(page);
        support.firePropertyChange(EventPropertyNameEnum.REMOVE_PAGE.getPropertyName(), page, null);
    }

    /**
     * Swaps a page with its neighbour in the given direction.
     *
     * @param page      the page to move
     * @param direction -1 to move up, +1 to move down
     */
    public void swapPageOrder(NotePage page, int direction) {
        int idx = pages.indexOf(page);
        if (idx < 0) {
            return;
        }
        int newIdx = idx + direction;
        if (newIdx < 0 || newIdx >= pages.size()) {
            return;
        }
        pages.remove(idx);
        pages.add(newIdx, page);
        support.firePropertyChange(EventPropertyNameEnum.MOVE_PAGE.getPropertyName(), idx, newIdx);
    }

    /**
     * Finds a page in this group by its unique identifier.
     *
     * @param id the UUID of the page to find
     * @return the NotePage with the matching ID, or null if not found
     */
    public NotePage getPageById(UUID id) {
        for (NotePage page : pages) {
            if (page.getId().equals(id)) {
                return page;
            }
        }
        return null;
    }
}
