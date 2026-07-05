# NotebookApplication
A OneNote-like notebook application using JavaFX as GUI framework.

## Overview
- Managing note pages within note groups just like OneNote
- Basic text editing features such as modifying colour, fonts, size of the texts in a note page
- Import and Export of note files with encryption
- Real-time file saving
- Unit tests for functions in the `notebookapplication.model` package

## Design
[UML Diagrams >>](uml_diagrams/diagrams.md)

## Features ToDo List
- [x] Basic text input management (with UndoRedo) and OneNote-like NoteGroup-NotePage management
- [x] Basic text editing features (colour, fonts, size etc.)
- [x] Completed basic UI, including toolbar, menubar etc.
- [x] Advanced text grouping features (e.g. bullet points, checkboxes,
  and this paragraph should now be grouped by this checkbox, just as in OneNote)
- [ ] Save and load notebook state to/from a local file
- [ ] Auto-save feature (save notebook state automatically on changes)
- [ ] UndoRedo feature for NoteGroup-NotePage management and text grouping features
- [ ] Better UI design (e.g. applying Material UI in JavaFX)