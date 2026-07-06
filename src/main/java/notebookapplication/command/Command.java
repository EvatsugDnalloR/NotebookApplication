package notebookapplication.command;

/**
 * Represents a reversible operation in the notebook application.
 *
 * <p>Each concrete implementation encapsulates the state needed to
 * perform the operation ({@link #execute()}) and reverse it ({@link #undo()}).
 * Commands are managed by {@link UndoRedo}, which maintains undo and redo stacks.
 */
public interface Command {

    /**
     * Performs or re-performs the operation.
     *
     * <p>Called when the command is first executed, and again if the user redoes
     * a previously undone operation.
     */
    void execute();

    /**
     * Reverses the operation, restoring the state to what it was before
     * {@link #execute()} was called.
     */
    void undo();
}
