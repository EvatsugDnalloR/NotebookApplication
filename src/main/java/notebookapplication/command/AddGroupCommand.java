package notebookapplication.command;

import notebookapplication.model.NoteFacade;
import notebookapplication.model.NoteGroup;

/**
 * Command that creates a new note group and supports undoing the creation
 * by removing the group.
 */
public class AddGroupCommand implements Command {

    private final NoteFacade facade;
    private NoteGroup addedGroup;

    public AddGroupCommand(NoteFacade facade) {
        this.facade = facade;
    }

    @Override
    public void execute() {
        addedGroup = facade.executeCreateGroup();
    }

    @Override
    public void undo() {
        if (addedGroup != null) {
            facade.executeRemoveGroup(addedGroup);
        }
    }
}
