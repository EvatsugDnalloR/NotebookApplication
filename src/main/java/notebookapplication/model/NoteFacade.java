package notebookapplication.model;

import java.beans.PropertyChangeSupport;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Facade for managing the notebook model.
 */
public class NoteFacade extends NoteSubject {
    private final List<NoteGroup> groups;
    private NoteGroup currentGroup;
    private NotePage currentPage;

    public NoteFacade() {
        this.groups = new ArrayList<>();
        support = new PropertyChangeSupport(this);
        createDefaultNotebook();
    }

    private void createDefaultNotebook() {
        NoteGroup defaultGroup = new NoteGroup();
        groups.add(defaultGroup);
        currentGroup = defaultGroup;
        currentPage = defaultGroup.getPages().getFirst();
    }

    public void createNewGroup() {
        NoteGroup newGroup = new NoteGroup();
        groups.add(newGroup);
        support.firePropertyChange(EventPropertyNameEnum.ADD_GROUP.getPropertyName(),  null, newGroup);
        switchToGroup(newGroup);
    }

    public void switchToGroup(NoteGroup group) {
        NoteGroup oldGroup = currentGroup;
        currentGroup = group;
        support.firePropertyChange(EventPropertyNameEnum.SWITCH_TO_GROUP.getPropertyName(), oldGroup, currentGroup);

        if (group.getPages().isEmpty()) {
            group.addPage(new NotePage());
        }
        switchToPage(group.getPages().getFirst());  // switch to the first NotePage of the NoteGroup
    }

    public void createNewPage(NoteGroup group) {
        NotePage newPage = new NotePage();
        group.addPage(newPage);
        switchToPage(newPage);
    }

    public void switchToPage(NotePage page) {
        NotePage oldPage = currentPage;
        currentPage = page;
        support.firePropertyChange(EventPropertyNameEnum.SWITCH_TO_PAGE.getPropertyName(), oldPage, currentPage);
    }

    public void removeGroup(NoteGroup group) {
        if (groups.size() > 1 && groups.contains(group)) {
            groups.remove(group);
            support.firePropertyChange(EventPropertyNameEnum.REMOVE_GROUP.getPropertyName(), group, null);

            if (currentGroup == group) {
                switchToGroup(groups.getFirst());
            }
        }
    }

    public void removePage(NotePage page) {
        NoteGroup group = findGroupContaining(page);
        if (group != null && group.getPages().size() > 1) {
            group.removePage(page);
            group.support.firePropertyChange(EventPropertyNameEnum.REMOVE_PAGE.getPropertyName(), page, null);

            if (currentPage == page) {
                switchToPage(group.getPages().getFirst());
            }
        }
    }

    private NoteGroup findGroupContaining(NotePage page) {
        for (NoteGroup group : groups) {
            if (group.getPages().contains(page)) {
                return group;
            }
        }
        return null;
    }

    public void saveNotebook(String filePath) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(filePath))) {

            NotebookData data = new NotebookData(groups, currentGroup.getId(), currentPage.getId());
            oos.writeObject(data);
        }
    }

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

    /**
     * Helper record for serialization.
     *
     * @param groups
     * @param currentGroupId
     * @param currentPageId
     */
    private record NotebookData(List<NoteGroup> groups, UUID currentGroupId,
                                UUID currentPageId) implements Serializable {}

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
}