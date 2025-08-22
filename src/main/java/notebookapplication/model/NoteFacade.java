package notebookapplication.model;

import java.beans.PropertyChangeSupport;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * Main facade class for managing the complete notebook model.
 *
 * <p>Provides high-level operations for group and page management,
 * including model-observer pattern by property change notification support, for UI synchronisation.
 */
public class NoteFacade extends NoteSubject {
    private final List<NoteGroup> groups;
    private NoteGroup currentGroup;
    private NotePage currentPage;

    /** Creates a new notebook with a default group and page.  */
    public NoteFacade() {
        this.groups = new ArrayList<>();
        support = new PropertyChangeSupport(this);
        createDefaultNotebook();
    }

    /** Creates a default group when initialising the notebook.  */
    private void createDefaultNotebook() {
        NoteGroup defaultGroup = new NoteGroup();
        groups.add(defaultGroup);
        currentGroup = defaultGroup;
        currentPage = defaultGroup.getPages().getFirst();
    }

    /**
     * Creates a new group and adds it to the notebook, then switches to it.
     *
     * @post {@code currentGroup == newGroup}
     * @post {@code currentPage == newGroup.getPages().getFirst()}
     * @post {@code group.getPages().isEmpty() == False}
     */
    public void createNewGroup() {
        NoteGroup newGroup = new NoteGroup();
        groups.add(newGroup);
        support.firePropertyChange(
                EventPropertyNameEnum.ADD_GROUP.getPropertyName(), null, newGroup
        );
        switchToGroup(newGroup);
    }

    /**
     * Switches the current active group and notifies registered listeners.
     *
     * @param group the group to switch to
     */
    public void switchToGroup(NoteGroup group) {
        NoteGroup oldGroup = currentGroup;
        currentGroup = group;
        support.firePropertyChange(
                EventPropertyNameEnum.SWITCH_TO_GROUP.getPropertyName(), oldGroup, currentGroup
        );

        if (group.getPages().isEmpty()) {
            group.addPage(new NotePage());
        }
        switchToPage(group.getPages().getFirst());  // switch to the first NotePage of the NoteGroup
    }

    /**
     * Creates a new page in the specified group and switches to it.
     *
     * @param group the group where the new page should be created
     * @post {@code currentPage == newPage}
     */
    public void createNewPage(NoteGroup group) {
        NotePage newPage = new NotePage();
        group.addPage(newPage);
        switchToPage(newPage);
    }

    /**
     * Switches the current active page and notifies registered listeners.
     *
     * @param page the page to switch to
     */
    public void switchToPage(NotePage page) {
        NotePage oldPage = currentPage;
        currentPage = page;
        support.firePropertyChange(
                EventPropertyNameEnum.SWITCH_TO_PAGE.getPropertyName(), oldPage, currentPage
        );
    }

    /**
     * Removes a group from the notebook if it's not the last remaining group.
     *
     * <p>Automatically switches to another group if the current group is removed.
     *
     * @param group the group to remove
     * @pre {@code groups.size() > 1 && groups.contains(group)}
     * @post {@code (currentGroup == group) => (currentGroup == groups.getFirst())}
     */
    public void removeGroup(NoteGroup group) {
        if (groups.size() > 1 && groups.contains(group)) {
            groups.remove(group);
            support.firePropertyChange(
                    EventPropertyNameEnum.REMOVE_GROUP.getPropertyName(), group, null
            );

            if (currentGroup == group) {
                switchToGroup(groups.getFirst());
            }
        }
    }

    /**
     * Removes a page from its containing group if it's not the last remaining page.
     *
     * <p>Automatically switches to another page if the current page is removed.
     *
     * @param page the page to remove
     * @pre {@code group != null && group.getPages().size() > 1}
     * @post {@code (currentPage == page) => (currentPage == group.getPages().getFirst())}
     */
    public void removePage(NotePage page) {
        NoteGroup group = findGroupContaining(page);
        if (group != null && group.getPages().size() > 1) {
            group.removePage(page);

            if (currentPage == page) {
                switchToPage(group.getPages().getFirst());
            }
        }
    }

    // Helper method for finding the group containing the specified page
    private NoteGroup findGroupContaining(NotePage page) {
        for (NoteGroup group : groups) {
            if (group.getPages().contains(page)) {
                return group;
            }
        }
        return null;
    }

    /**
     * Saves the complete notebook state to a file.
     *
     * @param filePath the path where the notebook should be saved
     * @throws IOException if an I/O error occurs during saving
     * @pre the specified {@code filePath} exists
     */
    public void saveNotebook(String filePath) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(filePath))) {

            NotebookData data = new NotebookData(groups, currentGroup.getId(), currentPage.getId());
            oos.writeObject(data);
        }
    }

    /**
     * Loads a complete notebook state from a file.
     *
     * @param filePath the path from where the notebook should be loaded
     * @throws IOException            if an I/O error occurs during loading
     * @throws ClassNotFoundException if the serialized class cannot be found
     */
    public void loadNotebook(String filePath) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            NotebookData data = (NotebookData) ois.readObject();
            this.groups.clear();
            this.groups.addAll(data.groups());

            // Restore current selection
            for (NoteGroup group : groups) {
                if (group.getId().equals(data.currentGroupId())) {
                    currentGroup = group;
                    currentPage = group.getPageById(data.currentPageId());
                    break;
                }
            }
        }
    }

    // Getters for current state
    public NoteGroup getCurrentGroup() {
        return currentGroup;
    }

    public NotePage getCurrentPage() {
        return currentPage;
    }

    public List<NoteGroup> getGroups() {
        return new ArrayList<>(groups);
    }

    /**
     * Helper record for serialization.
     *
     * @param groups         the NoteGroup list to save
     * @param currentGroupId the group the user was opening before quitting the app
     * @param currentPageId  the page the user was opening before quitting the app
     */
    private record NotebookData(List<NoteGroup> groups, UUID currentGroupId,
                                UUID currentPageId) implements Serializable {
    }
}