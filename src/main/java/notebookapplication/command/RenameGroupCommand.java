package notebookapplication.command;

import notebookapplication.model.NoteGroup;

/**
 * Command that renames a note group and supports undoing the rename
 * by restoring the previous name.
 */
public class RenameGroupCommand implements Command {

    private final NoteGroup group;
    private final String newName;
    private String oldName;

    /**
     * Creates a command to rename a note group.
     *
     * @param group   the group to rename
     * @param newName the new name for the group
     */
    public RenameGroupCommand(NoteGroup group, String newName) {
        this.group = group;
        this.newName = newName;
    }

    @Override
    public void execute() {
        oldName = group.getGroupName();
        group.setGroupName(newName);
    }

    @Override
    public void undo() {
        group.setGroupName(oldName);
    }
}
