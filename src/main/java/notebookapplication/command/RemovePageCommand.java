package notebookapplication.command;

import notebookapplication.model.NoteFacade;
import notebookapplication.model.NoteGroup;
import notebookapplication.model.NotePage;

/**
 * Command that removes a note page and supports undoing the removal
 * by re-adding the page to its original group.
 */
public class RemovePageCommand implements Command {

    private final NoteFacade facade;
    private final NotePage page;
    private final NoteGroup group;

    public RemovePageCommand(NoteFacade facade, NotePage page,
                             NoteGroup group) {
        this.facade = facade;
        this.page = page;
        this.group = group;
    }

    @Override
    public void execute() {
        facade.executeRemovePage(page);
    }

    @Override
    public void undo() {
        group.addPage(page);
    }
}
