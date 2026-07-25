package notebookapplication.command;

import notebookapplication.model.NoteFacade;
import notebookapplication.model.NoteGroup;

/**
 * Command that moves a note group left or right within the group list.
 * Undo reverses the direction.
 */
public class MoveGroupCommand implements Command {

    private final NoteFacade facade;
    private final NoteGroup group;
    private final int direction;

    public MoveGroupCommand(NoteFacade facade, NoteGroup group,
                            int direction) {
        this.facade = facade;
        this.group = group;
        this.direction = direction;
    }

    @Override
    public void execute() {
        facade.executeMoveGroup(group, direction);
    }

    @Override
    public void undo() {
        facade.executeMoveGroup(group, -direction);
    }
}
