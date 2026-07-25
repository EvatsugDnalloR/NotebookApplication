package notebookapplication.command;

import notebookapplication.model.NoteFacade;
import notebookapplication.model.NotePage;

/**
 * Command that moves a note page up or down within its group.
 * Undo reverses the direction.
 */
public class MovePageCommand implements Command {

    private final NoteFacade facade;
    private final NotePage page;
    private final int direction;

    public MovePageCommand(NoteFacade facade, NotePage page,
                           int direction) {
        this.facade = facade;
        this.page = page;
        this.direction = direction;
    }

    @Override
    public void execute() {
        facade.executeMovePage(page, direction);
    }

    @Override
    public void undo() {
        facade.executeMovePage(page, -direction);
    }
}
