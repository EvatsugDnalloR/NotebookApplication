package notebookapplication.command;

import notebookapplication.model.NoteFacade;
import notebookapplication.model.NoteGroup;
import notebookapplication.model.NotePage;

/**
 * Command that creates a new note page within a group and supports
 * undoing the creation by removing the page.
 */
public class AddPageCommand implements Command {

    private final NoteFacade facade;
    private final NoteGroup group;
    private NotePage addedPage;

    public AddPageCommand(NoteFacade facade, NoteGroup group) {
        this.facade = facade;
        this.group = group;
    }

    @Override
    public void execute() {
        addedPage = facade.executeCreatePage(group);
    }

    @Override
    public void undo() {
        if (addedPage != null) {
            facade.executeRemovePage(addedPage);
        }
    }
}
