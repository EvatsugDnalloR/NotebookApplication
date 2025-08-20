package notebookapplication.gui;

import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import notebookapplication.model.NoteFacade;
import notebookapplication.model.NoteGroup;
import notebookapplication.model.EventPropertyNameEnum;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Optional;


/**
 * A horizontal bar (HBox) component that displays and manages note groups as toggle buttons.
 *
 * <p>Provides functionality for adding, removing, renaming groups, and switching between them.
 *
 * <p>Implements PropertyChangeListener to respond to model changes.
 */
public class GroupBar extends HBox implements PropertyChangeListener {
    /** ToggleGroup that manages switching between NoteGroups (toggle buttons).  */
    private final ToggleGroup toggleGroup = new ToggleGroup();

    /** The access point to the backend model of the notebook application.  */
    private final NoteFacade facade;

    /**
     * Constructs a GroupBar with a reference to the application facade.
     *
     * <p>Initialises the group buttons and sets up the toggle group behaviour.
     *
     * @param facade the main application facade for accessing model functionality
     */
    public GroupBar(NoteFacade facade) {
        this.facade = facade;
        facade.addPropertyChangeListener(this);
        initializeGroups();
        setupToggleGroup();
    }

    /**
     * Initialises the group buttons by creating a toggle button for each existing group.
     *
     * <p>Called during construction to set up the initial state.
     */

    private void initializeGroups() {
        for (NoteGroup group : facade.getGroups()) {
            addGroupButton(group);
        }
        selectCurrentGroup();
    }

    /**
     * Creates and adds a toggle button for a specific group to the UI.
     *
     * <p>Sets up the button's properties, context menu, and click behaviour.
     *
     * @param group the NoteGroup to create a button for
     */
    private void addGroupButton(NoteGroup group) {
        ToggleButton button = new ToggleButton(group.getGroupName());
        button.setUserData(group);
        button.setToggleGroup(toggleGroup);

        // Add context menu
        button.setContextMenu(createGroupContextMenu(group));

        button.setOnAction(_ -> {
            if (button.isSelected()) {
                facade.switchToGroup(group);
            }
        });

        getChildren().add(getChildren().size(), button);

        group.addPropertyChangeListener(this);
    }

    /**
     * Selects the toggle button corresponding to the currently active group.
     *
     * <p>Ensures the UI reflects the current model state.
     */
    private void selectCurrentGroup() {
        for (var node : getChildren()) {
            if (node instanceof ToggleButton button) {
                if (button.getUserData() == facade.getCurrentGroup()) {
                    toggleGroup.selectToggle(button);
                    break;
                }
            }
        }
    }

    /**
     * Configures the toggle group to prevent deselection of all buttons.
     *
     * <p>Ensures at least one group is always selected.
     *
     * @post there is at least one group left in the ToggleGroup
     */
    private void setupToggleGroup() {
        toggleGroup.selectedToggleProperty().addListener(
                (_, oldToggle, newToggle) -> {
            if (newToggle == null && oldToggle != null) {
                toggleGroup.selectToggle(oldToggle);
            }
        });
    }

    /**
     * Handles property change events from the model.
     *
     * <p>Responds to group addition, removal, renaming, and selection changes.
     *
     * @param evt the property change event containing information about the change
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        EventPropertyNameEnum event = EventPropertyNameEnum.fromPropertyName(evt.getPropertyName());
        if (event == null) { throw new IllegalArgumentException("Unknown property: " + evt.getPropertyName()); }

        switch (event) {
            case ADD_GROUP:
                addGroupButton((NoteGroup) evt.getNewValue());
                break;

            case REMOVE_GROUP:
                removeGroupButton((NoteGroup) evt.getOldValue());
                break;

            case SWITCH_TO_GROUP:
                selectCurrentGroup();
                break;

            case GROUP_RENAME:
                updateGroupName((NoteGroup) evt.getSource());
                break;
        }
    }

    /**
     * Removes the button for a specific group and cleans up its event listener.
     *
     * <p>Ensures at least one group remains by creating a new one if needed.
     *
     * @param group the group to remove from the UI
     * @post {@code getChildren().isEmpty() == False}
     */
    private void removeGroupButton(NoteGroup group) {
        group.removePropertyChangeListener(this);

        getChildren().removeIf(node -> {
            if (node instanceof ToggleButton button) {
                return button.getUserData() == group;
            }
            return false;
        });

        // Ensure at least one group remains
        if (getChildren().isEmpty()) {
            facade.createNewGroup();
        }
    }

    /**
     * Updates the button text when a group is renamed.
     *
     * @param group the group that was renamed
     */
    private void updateGroupName(NoteGroup group) {
        for (var node : getChildren()) {
            // check if the param NoteGroup actually exists in the GroupBar
            if (node instanceof ToggleButton button && button.getUserData() == group) {
                button.setText(group.getGroupName());
                break;
            }
        }
    }

    /**
     * Creates a context menu with options to rename or delete a group.
     *
     * <p>The delete option is dynamically disabled for the last remaining group.
     *
     * @param group the group this context menu applies to
     * @return a configured ContextMenu instance
     */
    private ContextMenu createGroupContextMenu(NoteGroup group) {
        ContextMenu menu = new ContextMenu();

        // Rename option
        MenuItem renameItem = new MenuItem("Rename");
        renameItem.setOnAction(_ -> renameGroup(group));

        // Delete option
        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(_ -> facade.removeGroup(group));
        menu.setOnShowing(_ -> {
            deleteItem.setDisable(facade.getGroups().size() <= 1);  // update disable state dynamically
        });
        menu.getItems().addAll(renameItem, deleteItem);
        return menu;
    }

    /**
     * Shows a dialog to rename a group and updates the model with the new name.
     *
     * @param group the group to rename
     */
    private void renameGroup(NoteGroup group) {
        TextInputDialog dialog = new TextInputDialog(group.getGroupName());
        dialog.setTitle("Rename Group");
        dialog.setHeaderText("Enter new group name:");
        dialog.setContentText("Name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(group::setGroupName);
    }
}