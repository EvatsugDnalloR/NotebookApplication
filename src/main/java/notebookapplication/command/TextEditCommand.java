package notebookapplication.command;

import notebookapplication.model.NotePage;

/**
 * A snapshot-based undo/redo command for text editing within a note page.
 *
 * <p>Rather than tracking individual keystrokes, this command captures the
 * entire HTML body content of a page before and after an edit session.
 * Undoing restores the previous HTML snapshot; redoing reapplies the edit.
 *
 * <p>Snapshots are typically taken at content-switch boundaries
 * (e.g., page switching, manual save) to batch a sequence of edits into
 * one undoable unit.
 */
public class TextEditCommand implements Command {

    private final NotePage page;
    private final String oldHtml;
    private final String newHtml;

    /**
     * Creates a text edit command that can toggle a page's content between
     * two HTML snapshots.
     *
     * @param page    the note page whose content was edited
     * @param oldHtml the HTML body content before the edit
     * @param newHtml the HTML body content after the edit
     */
    public TextEditCommand(NotePage page, String oldHtml, String newHtml) {
        this.page = page;
        this.oldHtml = oldHtml;
        this.newHtml = newHtml;
    }

    @Override
    public void execute() {
        page.setHtmlBody(newHtml);
    }

    @Override
    public void undo() {
        page.setHtmlBody(oldHtml);
    }
}
