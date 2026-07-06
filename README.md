# NotebookApplication
A OneNote-like notebook application using JavaFX as GUI framework.
![Snapshots](images/snapshot_1.png)

## Overview
- Managing note pages within note groups just like OneNote
- Basic text editing features such as modifying colour, fonts, size of the texts in a note page
- Import and Export of note files with encryption
- Manuel/Auto-file saving
- Unit tests for functions in the `notebookapplication.model` package

## Design
[UML Diagrams >>](uml_diagrams/diagrams.md)

## Features ToDo List
- [x] Basic text input and OneNote-like NoteGroup-NotePage management
- [x] Text editing features through `HTMLEditor`
- [x] Completed basic UI, including toolbar, menubar etc.
- [x] Advanced text grouping features (e.g. bullet points, checkboxes,
  and this paragraph should now be grouped by this checkbox, just as in OneNote)
- [x] Manuel save/load and Auto-save/load (while quitting app) to/from a local file
- [x] UndoRedo feature for NoteGroup-NotePage management
- [ ] Better "About" info in the Help menu
- [ ] Better UI design (e.g. applying Material UI in JavaFX)
- [ ] Switch from `HTMLEditor` to `RichTextFX` for proper keystroke undo/redo handling,
  and re-implement text editing features (colour, fonts, size, etc.)