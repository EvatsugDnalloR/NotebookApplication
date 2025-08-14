package notebookapplication.model;

import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a group of note pages.
 */
public class NoteGroup extends NoteSubject implements Serializable {
    private final UUID id;
    private String name;
    private final List<NotePage> pages;

    /**
     *
     */
    public NoteGroup() {
        this.id = UUID.randomUUID();
        this.name = "Untitled Group";
        this.pages = new ArrayList<>();
        this.pages.add(new NotePage()); // default first page
        support = new PropertyChangeSupport(this);
    }

    /**
     *
     * @param id
     * @param name
     * @param pages
     */
    public NoteGroup(UUID id, String name, ArrayList<NotePage> pages) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.pages = pages;
        support = new PropertyChangeSupport(this);
    }

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        String oldName = this.name;
        this.name = name;
        support.firePropertyChange(EventPropertyNameEnum.GROUP_RENAME.getPropertyName(), oldName, this.name);
    }

    public List<NotePage> getPages() {
        return new ArrayList<>(pages);
    }

    public void addPage(NotePage page) {
        pages.add(page);
        support.firePropertyChange(EventPropertyNameEnum.ADD_PAGE.getPropertyName(), null, page);
    }

    public void removePage(NotePage page) {
        pages.remove(page);
    }

    /**
     *
     * @param id
     * @return
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