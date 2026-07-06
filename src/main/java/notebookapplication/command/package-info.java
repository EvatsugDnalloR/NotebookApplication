/**
 * The command module for the JavaFX NotebookApplication.
 *
 * <p>This module implements the Command design pattern to provide
 * undo and redo functionality for both text editing and application-level
 * operations.</p>
 *
 * <h2>Key Components:</h2>
 * <ul>
 *   <li><b>Command</b> — interface with {@code execute()} and {@code undo()}</li>
 *   <li><b>UndoRedo</b> — stack-based manager for undo and redo stacks</li>
 *   <li><b>TextEditCommand</b> — snapshot-based undo for page content edits</li>
 *   <li><b>AddGroupCommand / RemoveGroupCommand / RenameGroupCommand</b> —
 *       undoable note-group operations</li>
 *   <li><b>AddPageCommand / RemovePageCommand / RenamePageCommand</b> —
 *       undoable note-page operations</li>
 * </ul>
 *
 * <h2>Usage during integration:</h2>
 * <p>The {@code UndoRedo} manager is created by the controller and passed
 * into the facade. GUI components continue to call facade methods as before;
 * the facade internally wraps mutations in the appropriate {@code Command}
 * subclass and routes them through {@code UndoRedo.execute()}.</p>
 *
 * <p>Keyboard shortcuts (Ctrl+Z / Ctrl+Y) are handled at the scene level
 * by an event filter in the controller, calling
 * {@code UndoRedo.undo()} / {@code UndoRedo.redo()}.</p>
 */
package notebookapplication.command;
