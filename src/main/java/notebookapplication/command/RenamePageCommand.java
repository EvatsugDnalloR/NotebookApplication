package notebookapplication.command;

import notebookapplication.model.NotePage;

/**
 * Command that renames a note page and supports undoing the rename
 * by restoring the previous title.
 */
public class RenamePageCommand implements Command {

    private final NotePage page;
    private final String newName;
    private String oldName;

    /**
     * Creates a command to rename a note page.
     *
     * @param page    the page to rename
     * @param newName the new title for the page
     */
    public RenamePageCommand(NotePage page, String newName) {
        this.page = page;
        this.newName = newName;
    }

    @Override
    public void execute() {
        oldName = page.getPageName();
        page.setPageName(newName);
    }

    @Override
    public void undo() {
        page.setPageName(oldName);
    }
}
