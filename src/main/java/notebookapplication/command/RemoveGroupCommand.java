package notebookapplication.command;

import notebookapplication.model.NoteFacade;
import notebookapplication.model.NoteGroup;

/**
 * Command that removes a note group and supports undoing the removal
 * by re-adding the group with all its pages intact.
 */
public class RemoveGroupCommand implements Command {

    private final NoteFacade facade;
    private final NoteGroup group;

    public RemoveGroupCommand(NoteFacade facade, NoteGroup group) {
        this.facade = facade;
        this.group = group;
    }

    @Override
    public void execute() {
        facade.executeRemoveGroup(group);
    }

    @Override
    public void undo() {
        facade.executeAddGroup(group);
    }
}
