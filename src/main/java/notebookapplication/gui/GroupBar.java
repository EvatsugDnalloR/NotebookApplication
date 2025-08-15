package notebookapplication.gui;

import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import notebookapplication.model.NoteFacade;
import notebookapplication.model.NoteGroup;
import notebookapplication.model.EventPropertyNameEnum;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Optional;

public class GroupBar extends HBox implements PropertyChangeListener {
    private final ToggleGroup toggleGroup = new ToggleGroup();
    private final NoteFacade facade;

    public GroupBar(NoteFacade facade) {
        this.facade = facade;
        facade.addPropertyChangeListener(this);
        initializeGroups();
        setupToggleGroup();
    }

    private void initializeGroups() {
        for (NoteGroup group : facade.getGroups()) {
            addGroupButton(group);
        }
        selectCurrentGroup();
    }

    private void addGroupButton(NoteGroup group) {
        ToggleButton button = new ToggleButton(group.getGroupName());
        button.setUserData(group);
        button.setToggleGroup(toggleGroup);

        // Add context menu
        button.setContextMenu(createGroupContextMenu(group));

        button.setOnAction(e -> {
            if (button.isSelected()) {
                facade.switchToGroup(group);
            }
        });

        getChildren().add(getChildren().size(), button);

        group.addPropertyChangeListener(this);
    }

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

    private void setupToggleGroup() {
        toggleGroup.selectedToggleProperty().addListener(
                (_, oldToggle, newToggle) -> {
            if (newToggle == null && oldToggle != null) {
                toggleGroup.selectToggle(oldToggle);
            }
        });
    }

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

    private void updateGroupName(NoteGroup group) {
        for (var node : getChildren()) {
            if (node instanceof ToggleButton button && button.getUserData() == group) {
                button.setText(group.getGroupName());
                break;
            }
        }
    }

    private ContextMenu createGroupContextMenu(NoteGroup group) {
        ContextMenu menu = new ContextMenu();

        // Rename option
        MenuItem renameItem = new MenuItem("Rename");
        renameItem.setOnAction(e -> renameGroup(group));

        // Delete option
        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(e -> facade.removeGroup(group));
        menu.setOnShowing(e -> {
            deleteItem.setDisable(facade.getGroups().size() <= 1);  // update disable state dynamically
        });
        menu.getItems().addAll(renameItem, deleteItem);
        return menu;
    }

    private void renameGroup(NoteGroup group) {
        TextInputDialog dialog = new TextInputDialog(group.getGroupName());
        dialog.setTitle("Rename Group");
        dialog.setHeaderText("Enter new group name:");
        dialog.setContentText("Name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(group::setGroupName);
    }
}