package notebookapplication.gui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Optional;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import notebookapplication.model.EventPropertyNameEnum;
import notebookapplication.model.NoteFacade;
import notebookapplication.model.NoteGroup;


/**
 * A horizontal bar (HBox) component that displays and manages note
 * groups as toggle buttons.
 *
 * <p>Provides functionality for adding, removing, renaming, moving
 * groups, and switching between them.
 *
 * <p>Implements PropertyChangeListener to respond to model changes.
 */
public class GroupBar extends HBox implements PropertyChangeListener {
    /** ToggleGroup that manages switching between NoteGroups. */
    private final ToggleGroup toggleGroup = new ToggleGroup();

    /** The access point to the backend model of the notebook application.  */
    private final NoteFacade facade;

    /**
     * Constructs a GroupBar with a reference to the application facade.
     *
     * @param facade the main application facade
     */
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

        button.setContextMenu(createGroupContextMenu(group));

        button.setOnAction(_ -> {
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
            }
        );
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        EventPropertyNameEnum event = EventPropertyNameEnum.fromPropertyName(evt.getPropertyName());
        if (event == null) {
            throw new IllegalArgumentException(
                    "Unknown property: " + evt.getPropertyName());
        }

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

            case MOVE_GROUP:
                refreshGroupOrder();
                break;

            case LOAD_NOTEBOOK:
                getChildren().clear();
                initializeGroups();
                break;

            default:
                break;
        }
    }

    /**
     * Rebuilds the toggle buttons to reflect the current group order.
     */
    private void refreshGroupOrder() {
        // Remove listeners before clearing to prevent memory leaks
        for (var node : getChildren()) {
            if (node instanceof ToggleButton button
                    && button.getUserData() instanceof NoteGroup g) {
                g.removePropertyChangeListener(this);
            }
        }
        getChildren().clear();
        initializeGroups();
    }

    private void removeGroupButton(NoteGroup group) {
        group.removePropertyChangeListener(this);

        getChildren().removeIf(node -> {
            if (node instanceof ToggleButton button) {
                return button.getUserData() == group;
            }
            return false;
        });

        if (getChildren().isEmpty()) {
            facade.createNewGroup();
        }
    }

    private void updateGroupName(NoteGroup group) {
        for (var node : getChildren()) {
            if (node instanceof ToggleButton button
                    && button.getUserData() == group) {
                button.setText(group.getGroupName());
                break;
            }
        }
    }

    private ContextMenu createGroupContextMenu(NoteGroup group) {
        ContextMenu menu = new ContextMenu();

        MenuItem renameItem = new MenuItem("Rename");
        renameItem.setOnAction(_ -> renameGroup(group));

        MenuItem moveLeftItem = new MenuItem("Move Left");
        moveLeftItem.setOnAction(_ -> facade.moveGroup(group, -1));

        MenuItem moveRightItem = new MenuItem("Move Right");
        moveRightItem.setOnAction(_ -> facade.moveGroup(group, 1));

        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(_ -> facade.removeGroup(group));

        menu.setOnShowing(_ -> {
            int idx = facade.getGroups().indexOf(group);
            moveLeftItem.setDisable(idx <= 0);
            moveRightItem.setDisable(idx < 0 || idx >= facade.getGroups().size() - 1);
            deleteItem.setDisable(facade.getGroups().size() <= 1);
        });

        menu.getItems().addAll(renameItem, moveLeftItem, moveRightItem,
                deleteItem);
        return menu;
    }

    private void renameGroup(NoteGroup group) {
        TextInputDialog dialog =
                new TextInputDialog(group.getGroupName());
        dialog.setTitle("Rename Group");
        dialog.setHeaderText("Enter new group name:");
        dialog.setContentText("Name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(
                name -> facade.renameGroup(group, name));
    }
}
