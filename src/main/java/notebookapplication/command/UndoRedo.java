package notebookapplication.command;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Manages a stack-based undo/redo system for {@link Command} objects.
 *
 * <p>Maintains two stacks:
 * <ul>
 *   <li><b>undo stack</b> — commands that have been executed and can be undone</li>
 *   <li><b>redo stack</b> — commands that have been undone and can be redone</li>
 * </ul>
 *
 * <p>When a new command is executed (not via undo/redo), the redo stack is cleared.
 * This follows the standard undo/redo behaviour seen in most applications.
 */
public class UndoRedo {

    private final Deque<Command> undoStack = new ArrayDeque<>();
    private final Deque<Command> redoStack = new ArrayDeque<>();

    /**
     * Executes a command, pushes it onto the undo stack, and clears the redo stack.
     *
     * @param command the command to execute
     */
    public void execute(Command command) {
        command.execute();
        undoStack.push(command);
        redoStack.clear();
    }

    /**
     * Undoes the most recently executed command.
     *
     * <p>The undone command is moved to the redo stack so it can be redone later.
     * If the undo stack is empty, this method does nothing.
     */
    public void undo() {
        if (!undoStack.isEmpty()) {
            Command command = undoStack.pop();
            command.undo();
            redoStack.push(command);
        }
    }

    /**
     * Redoes the most recently undone command.
     *
     * <p>The redone command is moved back to the undo stack.
     * If the redo stack is empty, this method does nothing.
     */
    public void redo() {
        if (!redoStack.isEmpty()) {
            Command command = redoStack.pop();
            command.execute();
            undoStack.push(command);
        }
    }

    /**
     * Returns {@code true} if there is at least one command that can be undone.
     *
     * @return {@code true} if the undo stack is non-empty
     */
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    /**
     * Returns {@code true} if there is at least one command that can be redone.
     *
     * @return {@code true} if the redo stack is non-empty
     */
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }
}
