# Use Case Lists

## Use Cases:
- [Interact with Notebook App](#interact-with-notebook-app)
- [Basic Interactions](#basic-interactions)
- [Text Editing Features](#text-editing-features)
- [UndoRedo](#undoredo)
- [Save to File](#save-to-file)
- [Export File](#export-file)
- [Import File](#import-file)

## Interact with Notebook App
### Priority: `Must`
### Summary
User opens the application, and is able to perform all kinds of operations such as basic text writing, additional text editing, undo or redo the operations, and also choose to save the note file.
### Actors 
`User`, `File`
### Main Scenario
1. User start the application
2. The application displays the notebook interface and shows that no notebook has been created
3. User is able to create new Note Groups, and also create new Note Pages inside the Note Group for writing notes
4. User is able to perform extra text editing features to the notes written
5. User is able to undo or redo any operations performed
6. User quit the program, and being asked if he wants to save the current changes or not.
### Alternative Scenario
2. a.  If there is notebook file detected, then the app will load the notes from the file and shows the already existing notes
### Requirements
`Basic Interactions`, `Text Editing Features`, `UndoRedo`, `Save to File`, `Export File`, `Import File`

## Basic Interactions
### Priority: `Must`
### Summary
User is able to add new Note Groups, and add new Note Pages inside of a Group where user can write notes in.
### Actors
`User`
### Pre-condition
Application is started
### Post-consditions
New Note Groups and new Note Pages are added, and user is able to add texts inside of the Pages.
### Main Scenario
1. User add a Note Group
2. User add a Note Page inside the Note Group added
3. User add any texts to the Note Page

## Text Editing Features
### Priority: `Should`
### Summary
User is allowed to change fonts, font sizes, colors, state (bold, underlined and italic) of the texts, and is able to insert other symbols like bullet point, check boxes etc.
### Actors
`User`
### Pre-conditions
- Application is started
- Note Pages have been created for user to add texts
### Post-condition
The selected texts have been modified accroding to the editing operations performed.
### Main Scenario
1. User select some texts in the Note Page
2. User perform editing operations on the selected texts
3. Selected texts have been edited.
### Alternative Scenario
1. a1. User select nothing (empty text)\
a2. The edit operation does nothing
2. a1. User insert other symbols such as bullet points or check boxes\
a2. These symbols should be successfully inserted infront of the line of text selected
### Exception Scenario
- Trigger: no Note Page has been created yet
### Requirement
`Basic Interactions`

## UndoRedo
### Priority: `Should`
### Summary
User is allowed to undo and redo all kinds of text editing operations and the addition and deletion of Note Sections and Pages.
### Actors
`User`
### Pre-conditions
- Application is started
- There're some operations to undo or redo
### Post-condition
The recent operation has been undone or redone.
### Main Scenario
1. User has done some operations
2. User pressed `Ctrl + Z` or click the Undo option through menu bar
3. The most recent operation has been undone
### Alternative Scenario
1. a1. User has already undone some operations\
a2. User pressed `Ctrl + Y` or click the Redo option through menu bar\
a3. The most recent undone operation has been redone
### Exception Scenario
- Trigger: no operation to undo
- Trigger: no operation to redo
### Requirements:
`Basic Interactions`, `Text Editing Features`

## Save to File
### Priority: `Should`
### Summary
User is able to save their current notebook to a file that can be read by the program to initialise the notebook app while startup, either manual saving during program running or will be asked while exiting the program if notes not saved.
### Actors
`User`, `File`
### Pre-conditions
- Application is started
- The file for storing the content of the notebook exists
### Post-condition
All the content of the notebook have been stored to the file.
### Main Scenario
1. User made some changes to the notebook content
2. User trys to quit or close the program
3. A window pops out to ask the user if he wants to save the current changes or not
4. User choose 'yes' to save the notebook content to the local file and the program will be terminated
### Alternative Scenario
2. a1. User pressed `Ctrl + S` or clicked the Save option throught the menu bar\
a2. The current notebook content has been saved to the local file\
a3. User can exit the application without any pop out window
<!-- 4 -->
4. a. User choose 'no' to abancon the current changes, and the program will be terminated
### Exception Scenario
- Trigger: The file for storing the content of the notebook doesn't exists or corrupted when trying to save the changes

## Export File
### Priority: `Could`
### Summary
User is able to choose to export their notebook file to anywhere on the system (Default to the `Download` folder)
### Actors
`User`, `File`
### Pre-conditions
- Application is started
- The file to export exists
### Post-conditions
The file has been exported to the directory chosen by the user.
### Main Scenario
1. User has saved all the changes to the notebook content
2. User clicks Export option through the menu bar
3. System asks the user for the directory to export (default at Download folder)
4. User didn't change the export directory and the file has been successfully exported to the Download folder
### Alternative Scenario
1. a1. User didn't save the changes but still went to click the Export option through the menu bar\
a2. System will automatically save the current changes for the user\
a3. continue by following step 3 in the Main Scenario
<!-- 4 -->
4. a1. User changes the export directory to his desired one\
a2. The file has been successfully exported to the directory chosen by the user
### Exception Scenario
- Trigger: the file for storing the content of the notebook doesn't exists or corrupted when trying to export
### Requirement
`Save to File`

## Import File
### Priority: `Could`
### Summary
User is able to choose to import their notebook file such that the application can load all the existing notebook content from the file.
### Actors
`User`, `File`
### Pre-conditions
- Application is started
- The file to be exported is in the right format
### Post-conditions
The application loaded all the notebook content from the file imported, and replaced the existing file.
### Main Scenario
1. User clicks Import option through the menu bar
2. User choose the file to import
3. The system loads all the notebook content from the file imported
4. THe exising local file is replaced by the imported file for further saving changes
### Exception Scenario
- Trigger: the file to be imported is not in the correct format
### Requirement
`Save to File`