package notebookapplication.gui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Optional;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import notebookapplication.model.EventPropertyNameEnum;
import notebookapplication.model.NoteFacade;
import notebookapplication.model.NoteGroup;
import notebookapplication.model.NotePage;


/**
 * A vertical bar (VBox) component that displays and manages note pages as toggle buttons.
 *
 * <p>Provides functionality for adding, removing, renaming pages, and switching between them.
 *
 * <p>Implements PropertyChangeListener to respond to model changes.
 */
public class PageBar extends VBox implements PropertyChangeListener {
    /** ToggleGroup that manages switching between NotePages (toggle buttons). */
    private final ToggleGroup toggleGroup = new ToggleGroup();

    /** The access point to the backend model of the notebook application.  */
    private final NoteFacade facade;

    /**
     * The current group being displayed by this PageBar.
     * This local reference is maintained for several reasons:
     *
     * <p>- Listener Management: Tracks which group's pages need event listeners
     *
     * <p>- Performance: Avoids repeated calls to facade.getCurrentGroup()
     *
     * <p>- State Consistency: Provides a stable reference during event processing
     *
     * <p>- Context Operations: Enables group-specific UI operations (context menus)
     *
     * <p>- Clean-up: Knows which group to remove listeners from during group switches
     *
     * <p>It serves specific UI management purposes that require a stable reference throughout
     * event handling operations.
     */
    private NoteGroup currentGroup;

    /**
     * Constructs a PageBar with a reference to the application facade.
     *
     * <p>Initialises the page buttons and sets up the toggle group behaviour.
     *
     * @param facade the main application facade for accessing model functionality
     */
    public PageBar(NoteFacade facade) {
        this.facade = facade;
        this.currentGroup = facade.getCurrentGroup();

        facade.addPropertyChangeListener(this);
        currentGroup.addPropertyChangeListener(this);   // add listener to initial group

        initializePages();
        setupToggleGroup();
    }

    /**
     * Initialises the page buttons by creating a toggle button for each page in the current group.
     *
     * <p>Called during construction and when switching groups.
     */
    private void initializePages() {
        for (NotePage page : currentGroup.getPages()) {
            addPageButton(page);
        }
        selectCurrentPage();
    }

    /**
     * Creates and adds a toggle button for a specific page to the UI.
     *
     * <p>Sets up the button's properties, context menu, and click behaviour.
     *
     * @param page the NotePage to create a button for
     */
    private void addPageButton(NotePage page) {
        ToggleButton button = new ToggleButton(page.getPageName());
        button.setUserData(page);
        button.setToggleGroup(toggleGroup);

        // Add context menu
        button.setContextMenu(createPageContextMenu(page));

        button.setOnAction(_ -> {
            if (button.isSelected()) {
                facade.switchToPage(page);
            }
        });

        getChildren().add(getChildren().size(), button);
        page.addPropertyChangeListener(this);
    }

    /**
     * Selects the toggle button corresponding to the currently active page.
     *
     * <p>Ensures the UI reflects the current model state.
     */
    private void selectCurrentPage() {
        for (var node : getChildren()) {
            if (node instanceof ToggleButton button) {
                if (button.getUserData() == facade.getCurrentPage()) {
                    toggleGroup.selectToggle(button);
                    break;
                }
            }
        }
    }

    /**
     * Configures the toggle group to prevent deselection of all buttons.
     *
     * <p>Ensures at least one page is always selected.
     */
    private void setupToggleGroup() {
        toggleGroup.selectedToggleProperty().addListener(
                (_, oldToggle, newToggle) -> {
                if (newToggle == null && oldToggle != null) {
                    toggleGroup.selectToggle(oldToggle);
                }
            }
        );
    }

    /**
     * Cleans up event listeners and UI elements for the current group before switching.
     *
     * <p>Prevents memory leaks and ensures a clean state for the new group.
     */
    private void cleanupCurrentGroup() {
        if (currentGroup != null) {
            currentGroup.removePropertyChangeListener(this);
            for (NotePage page : currentGroup.getPages()) {
                // remove listeners from all pages in the current group
                page.removePropertyChangeListener(this);
            }
        }

        getChildren().clear();  // clear all page buttons
    }

    /**
     * Handles property change events from the model.
     *
     * <p>Responds to group switching, page addition, removal, renaming, and selection changes.
     *
     * @param evt the property change event containing information about the change
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        EventPropertyNameEnum event = EventPropertyNameEnum.fromPropertyName(evt.getPropertyName());
        if (event == null) {
            throw new IllegalArgumentException("Unknown property: " + evt.getPropertyName());
        }

        switch (event) {
            case SWITCH_TO_GROUP:
                // Clean up the previous group before switching
                cleanupCurrentGroup();

                currentGroup = (NoteGroup) evt.getNewValue();
                currentGroup.addPropertyChangeListener(this);

                // Add listeners to all pages in the new group
                for (NotePage page : currentGroup.getPages()) {
                    page.addPropertyChangeListener(this);
                }

                initializePages();
                break;

            case ADD_PAGE:
                if (evt.getSource() == currentGroup) {
                    addPageButton((NotePage) evt.getNewValue());
                }
                break;

            case REMOVE_PAGE:
                if (evt.getSource() == currentGroup) {
                    removePageButton((NotePage) evt.getOldValue());
                }
                break;

            case SWITCH_TO_PAGE:
                selectCurrentPage();
                break;

            case PAGE_RENAME:
                updatePageName((NotePage) evt.getSource());
                break;

            default:
                //throw new IllegalArgumentException("Unknown property: " + event);
                break;
        }
    }

    /**
     * Removes the button for a specific page and cleans up its event listener.
     *
     * <p>Ensures at least one page remains by creating a new one if needed.
     *
     * @param page the page to remove from the UI
     * @post {@code getChildren().isEmpty() == False}
     */
    private void removePageButton(NotePage page) {
        page.removePropertyChangeListener(this);

        getChildren().removeIf(node -> {
            if (node instanceof ToggleButton button) {
                return button.getUserData() == page;
            }
            return false;
        });

        // Ensure at least one page remains
        if (getChildren().isEmpty()) {
            facade.createNewPage(currentGroup);
        }
    }

    /**
     * Updates the button text when a page is renamed.
     *
     * @param page the page that was renamed
     */
    private void updatePageName(NotePage page) {
        for (var node : getChildren()) {
            if (node instanceof ToggleButton button) {
                if (button.getUserData() == page) {
                    button.setText(page.getPageName());
                    break;
                }
            }
        }
    }

    /**
     * Creates a context menu with options to rename or delete a page.
     *
     * <p>The delete option is dynamically disabled for the last remaining page.
     *
     * @param page the page this context menu applies to
     * @return a configured ContextMenu instance
     */
    private ContextMenu createPageContextMenu(NotePage page) {
        ContextMenu menu = new ContextMenu();

        // Rename option
        MenuItem renameItem = new MenuItem("Rename");
        renameItem.setOnAction(_ -> renamePage(page));

        // Delete option
        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(_ -> facade.removePage(page));

        menu.setOnShowing(_ -> {    // update disable state dynamically
            deleteItem.setDisable(currentGroup.getPages().size() <= 1);
        });
        menu.getItems().addAll(renameItem, deleteItem);
        return menu;
    }

    /**
     * Shows a dialog to rename a page and updates the model with the new name.
     *
     * @param page the page to rename
     */
    private void renamePage(NotePage page) {
        TextInputDialog dialog = new TextInputDialog(page.getPageName());
        dialog.setTitle("Rename Page");
        dialog.setHeaderText("Enter new page title:");
        dialog.setContentText("Title:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(page::setPageName);
    }
}
