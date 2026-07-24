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

import notebookapplication.command.AddGroupCommand;
import notebookapplication.command.AddPageCommand;
import notebookapplication.command.RemoveGroupCommand;
import notebookapplication.command.RemovePageCommand;
import notebookapplication.command.RenameGroupCommand;
import notebookapplication.command.RenamePageCommand;
import notebookapplication.command.UndoRedo;


/**
 * Main facade class for managing the complete notebook model.
 *
 * <p>Provides high-level operations for group and page management,
 * including model-observer pattern by property change notification support,
 * for UI synchronisation.
 *
 * <p>All mutating operations (create/remove group, create/remove page) are
 * routed through {@link UndoRedo} via the Command design pattern.
 * Each public mutator creates the appropriate {@link
 * notebookapplication.command.Command}, executes it through the
 * undo/redo manager, and the command calls the corresponding
 * {@code execute*} method on this facade to perform the actual state
 * change and fire property change events.
 */
public class NoteFacade extends NoteSubject {
    private final UndoRedo undoRedo;
    private final List<NoteGroup> groups;
    private NoteGroup currentGroup;
    private NotePage currentPage;

    /** Creates a new notebook with a default undo/redo manager. */
    public NoteFacade() {
        this(new UndoRedo());
    }

    /**
     * Creates a new notebook with the specified undo/redo manager.
     *
     * @param undoRedo the undo/redo manager to use for tracking mutations
     */
    public NoteFacade(UndoRedo undoRedo) {
        this.undoRedo = undoRedo;
        this.groups = new ArrayList<>();
        support = new PropertyChangeSupport(this);
        createDefaultNotebook();
    }

    /** Creates a default group when initialising the notebook. */
    private void createDefaultNotebook() {
        // Bypass UndoRedo for initial default setup
        NoteGroup defaultGroup = executeCreateGroup();
    }

    // ---------------------------------------------------------------
    //  Public API — routes through UndoRedo (called by GUI layer)
    // ---------------------------------------------------------------

    /**
     * Creates a new group (routed through UndoRedo).
     *
     * <p>The actual work is done by {@link #executeCreateGroup()},
     * called from {@link AddGroupCommand#execute()}.
     */
    public void createNewGroup() {
        undoRedo.execute(new AddGroupCommand(this));
    }

    /**
     * Creates a new page in the specified group (routed through UndoRedo).
     *
     * <p>The actual work is done by {@link #executeCreatePage(NoteGroup)},
     * called from {@link AddPageCommand#execute()}.
     *
     * @param group the group where the new page should be created
     */
    public void createNewPage(NoteGroup group) {
        undoRedo.execute(new AddPageCommand(this, group));
    }

    /**
     * Removes a group (routed through UndoRedo).
     *
     * <p>The actual work is done by {@link #executeRemoveGroup(NoteGroup)},
     * called from {@link RemoveGroupCommand#execute()}.
     *
     * @param group the group to remove
     */
    public void removeGroup(NoteGroup group) {
        undoRedo.execute(new RemoveGroupCommand(this, group));
    }

    /**
     * Removes a page (routed through UndoRedo).
     *
     * <p>The actual work is done by {@link #executeRemovePage(NotePage)},
     * called from {@link RemovePageCommand#execute()}.
     *
     * @param page the page to remove
     */
    public void removePage(NotePage page) {
        NoteGroup group = findGroupContaining(page);
        if (group != null) {
            undoRedo.execute(new RemovePageCommand(this, page, group));
        }
    }

    /**
     * Renames a group (routed through UndoRedo).
     *
     * @param group   the group to rename
     * @param newName the new name for the group
     */
    public void renameGroup(NoteGroup group, String newName) {
        undoRedo.execute(new RenameGroupCommand(group, newName));
    }

    /**
     * Renames a page (routed through UndoRedo).
     *
     * @param page    the page to rename
     * @param newName the new title for the page
     */
    public void renamePage(NotePage page, String newName) {
        undoRedo.execute(new RenamePageCommand(page, newName));
    }

    // ---------------------------------------------------------------
    //  Direct implementations — called by Command subclasses
    //  (bypass UndoRedo to avoid infinite recursion)
    // ---------------------------------------------------------------

    /**
     * Direct implementation of group creation.
     * Called by {@code AddGroupCommand.execute()}.
     *
     * @return the newly created group
     */
    public NoteGroup executeCreateGroup() {
        NoteGroup newGroup = new NoteGroup();
        groups.add(newGroup);
        support.firePropertyChange(
                EventPropertyNameEnum.ADD_GROUP.getPropertyName(), null,
                newGroup);
        switchToGroup(newGroup);
        return newGroup;
    }

    /**
     * Direct implementation of page creation.
     * Called by {@code AddPageCommand.execute()}.
     *
     * @param group the group to add the page to
     * @return the newly created page
     */
    public NotePage executeCreatePage(NoteGroup group) {
        NotePage newPage = new NotePage();
        group.addPage(newPage);
        switchToPage(newPage);
        return newPage;
    }

    /**
     * Direct implementation of group removal.
     * Called by {@code RemoveGroupCommand.execute()}.
     *
     * @param group the group to remove
     */
    public void executeRemoveGroup(NoteGroup group) {
        if (groups.size() > 1 && groups.contains(group)) {
            groups.remove(group);
            support.firePropertyChange(
                    EventPropertyNameEnum.REMOVE_GROUP.getPropertyName(),
                    group, null);
            if (currentGroup == group) {
                switchToGroup(groups.getFirst());
            }
        }
    }

    /**
     * Direct implementation of page removal.
     * Called by {@code RemovePageCommand.execute()}.
     *
     * @param page the page to remove
     */
    public void executeRemovePage(NotePage page) {
        NoteGroup group = findGroupContaining(page);
        if (group != null && group.getPages().size() > 1) {
            group.removePage(page);
            if (currentPage == page) {
                switchToPage(group.getPages().getFirst());
            }
        }
    }

    /**
     * Re-adds an existing group back into the notebook.
     * Called by {@code RemoveGroupCommand.undo()} to restore a
     * previously removed group with all its pages intact.
     *
     * @param group the group to re-add
     */
    public void executeAddGroup(NoteGroup group) {
        groups.add(group);
        support.firePropertyChange(
                EventPropertyNameEnum.ADD_GROUP.getPropertyName(), null,
                group);
    }

    // ---------------------------------------------------------------
    //  Navigation (NOT undoable — bypasses UndoRedo)
    // ---------------------------------------------------------------

    /**
     * Switches the current active group and notifies registered listeners.
     *
     * @param group the group to switch to
     */
    public void switchToGroup(NoteGroup group) {
        NoteGroup oldGroup = currentGroup;
        currentGroup = group;
        support.firePropertyChange(
                EventPropertyNameEnum.SWITCH_TO_GROUP.getPropertyName(),
                oldGroup, currentGroup);
        if (group.getPages().isEmpty()) {
            group.addPage(new NotePage());
        }
        switchToPage(group.getPages().getFirst());
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
                EventPropertyNameEnum.SWITCH_TO_PAGE.getPropertyName(),
                oldPage, currentPage);
    }

    // ---------------------------------------------------------------
    //  Persistence
    // ---------------------------------------------------------------

    /**
     * Saves the complete notebook state to a file.
     *
     * @param filePath the path where the notebook should be saved
     * @throws IOException if an I/O error occurs during saving
     */
    public void saveNotebook(String filePath) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(filePath))) {
            NotebookData data = new NotebookData(groups,
                    currentGroup.getId(), currentPage.getId());
            oos.writeObject(data);
        }
    }

    /**
     * Loads a complete notebook state from a file.
     *
     * @param filePath the path from where the notebook should be loaded
     * @throws IOException if an I/O error occurs during loading
     * @throws ClassNotFoundException if the serialized class cannot be
     *         found
     */
    public void loadNotebook(String filePath)
            throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois =
                new ObjectInputStream(new FileInputStream(filePath))) {
            NotebookData data = (NotebookData) ois.readObject();
            this.groups.clear();
            this.groups.addAll(data.groups());

            for (NoteGroup group : groups) {
                if (group.getId().equals(data.currentGroupId())) {
                    currentGroup = group;
                    currentPage =
                            group.getPageById(data.currentPageId());
                    break;
                }
            }

            // Notify UI to rebuild from loaded state
            support.firePropertyChange(
                    EventPropertyNameEnum.LOAD_NOTEBOOK
                            .getPropertyName(),
                    null, groups);
            support.firePropertyChange(
                    EventPropertyNameEnum.SWITCH_TO_GROUP
                            .getPropertyName(),
                    null, currentGroup);
            support.firePropertyChange(
                    EventPropertyNameEnum.SWITCH_TO_PAGE
                            .getPropertyName(),
                    null, currentPage);
        }
    }

    // ---------------------------------------------------------------
    //  Getters
    // ---------------------------------------------------------------

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
     * Returns the undo/redo manager used by this facade.
     *
     * @return the UndoRedo instance
     */
    public UndoRedo getUndoRedo() {
        return undoRedo;
    }

    // ---------------------------------------------------------------
    //  Helpers
    // ---------------------------------------------------------------

    private NoteGroup findGroupContaining(NotePage page) {
        for (NoteGroup group : groups) {
            if (group.getPages().contains(page)) {
                return group;
            }
        }
        return null;
    }

    /**
     * Helper record for serialization.
     */
    private record NotebookData(List<NoteGroup> groups,
                                UUID currentGroupId,
                                UUID currentPageId)
            implements Serializable {
    }
}
