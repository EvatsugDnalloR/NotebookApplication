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

    /**
     * Creates a command that moves a note page.
     *
     * @param facade    the facade to operate on
     * @param page      the page to move
     * @param direction the move direction (-1 = up, +1 = down)
     */
    public MovePageCommand(NoteFacade facade, NotePage page, int direction) {
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
