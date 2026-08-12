# NotebookApplication

A OneNote-like notebook application built with JavaFX 24.

![Snapshots](images/snapshot_5_fixed.png)

---

## Features

### Done

**Organisation**

- [x] OneNote-style note groups and pages (add, remove, rename, navigate, reorder)
- [x] Undo / Redo for group and page operations (Command design pattern) with shortcuts Ctrl+Shift+Z/Ctrl+Shift+Y

**Rich text editing**

- [x] Bold, italic, underline with toolbar toggles and Ctrl+B/I/U shortcuts
- [x] Cut / copy / paste toolbar buttons
- [x] Font family combo box selector
- [x] Font size spinner selector
- [x] Text colour picker
- [x] Text alignment (left, centre, right)
- [x] Bullet points, numbered lists, and clickable checkboxes (text grouping)
- [x] Text-level undo/redo (Ctrl+Z / Ctrl+Y) — migrated from `HTMLEditor` to `RichTextFX`

**Persistence**

- [x] Manual save and load to local `.dat` file (Java serialization)
- [x] Auto-save on exit, autoload on startup, plus manual save/load with shortcuts Ctrl+S/Ctrl+L
- [x] Import / Export notebook data from / to other directories

**Quality**

- [x] Unit tests for the model and command layer
- [x] Better "About" dialogue content (author, repository link, licence)

### Roadmap

- [ ] Unit tests for classes in the `gui` package
- [ ] Installer for the app
- [ ] Subgroups under text grouping (e.g. hollow bullet points as a subgroup of bullet points)
- [ ] Font applies only to selected texts and paragraphs instead of globally
- [ ] In-page search (Ctrl+F) and global search across groups/pages
- [ ] Material UI refactoring (planned for v3.0.0)
- [ ] Dark mode / theme switching
- [ ] Export to PDF and Markdown
- [ ] Multiple notebooks (open, create, and switch between several `.dat` files)
- [ ] Password-protected / encrypted notebooks

---

## Architecture

```
notebookapplication/
├── model/       NoteFacade, NoteGroup, NotePage, NoteSubject
├── gui/         Controller, GroupBar, PageBar, MainFrame
└── command/     Command, UndoRedo, AddGroupCommand, …
```

- **Observer pattern** — model classes extend `NoteSubject`
  (`PropertyChangeSupport`); GUI components implement
  `PropertyChangeListener` for automatic UI synchronisation when
  the model changes.
- **Command pattern** — every mutating operation (create/remove
  group, create/remove page) is a `Command` object executed
  through an `UndoRedo` manager with two stacks. Navigation
  (switching groups/pages) is not undoable.
- **Facade pattern** — `NoteFacade` is the single entry point to
  the model layer; the GUI never touches `NoteGroup` or
  `NotePage` internals directly.

---

## Tech Stack

| Component                    | Version                            |
|------------------------------|------------------------------------|
| JavaFX (controls, fxml, web) | 24                                 |
| JDK                          | 24                                 |
| Maven                        | 3.x                                |
| RichTextFX                   | 0.11.5                             |
| JUnit Jupiter                | 5.10.3                             |
| Checkstyle                   | Google Checks                      |

---

## Getting Started

Clone or download the source code from the
[latest release](https://github.com/EvatsugDnalloR/NotebookApplication/releases/latest),
then build and run with Maven:

```bash
git clone https://github.com/EvatsugDnalloR/NotebookApplication.git   # if not downloading from release page
cd NotebookApplication   # open the directory
mvn clean javafx:run
```

Prerequisites: JDK 24 and Maven 3.x.

---
## High-Level Design

[UML Diagrams >>](uml_diagrams/diagrams.md)
