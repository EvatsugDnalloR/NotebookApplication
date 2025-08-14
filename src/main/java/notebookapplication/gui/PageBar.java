package notebookapplication.gui;

import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import notebookapplication.model.NoteFacade;
import notebookapplication.model.NoteGroup;
import notebookapplication.model.NotePage;
import notebookapplication.model.EventPropertyNameEnum;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Optional;

public class PageBar extends VBox implements PropertyChangeListener {
    private final ToggleGroup toggleGroup = new ToggleGroup();
    private final NoteFacade facade;
    private NoteGroup currentGroup;

    public PageBar(NoteFacade facade) {
        this.facade = facade;
        this.currentGroup = facade.getCurrentGroup();

        facade.addPropertyChangeListener(this);
        currentGroup.addPropertyChangeListener(this);   // add listener to initial group

        initializePages();
        setupToggleGroup();
    }

    private void initializePages() {
        for (NotePage page : currentGroup.getPages()) {
            addPageButton(page);
        }
        selectCurrentPage();
//        currentGroup.addPropertyChangeListener(this);
    }

    private void addPageButton(NotePage page) {
        ToggleButton button = new ToggleButton(page.getTitle());
        button.setUserData(page);
        button.setToggleGroup(toggleGroup);

        // Add context menu
        button.setContextMenu(createPageContextMenu(page));

        button.setOnAction(e -> {
            if (button.isSelected()) {
                facade.switchToPage(page);
            }
        });

        getChildren().add(getChildren().size(), button);
        page.addPropertyChangeListener(this);
    }

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

    private void setupToggleGroup() {
        toggleGroup.selectedToggleProperty().addListener(
                (obs, oldToggle, newToggle) -> {
            if (newToggle == null && oldToggle != null) {
                toggleGroup.selectToggle(oldToggle);
            }
        });
    }

    private void cleanupCurrentGroup() {
        // Remove listener from current group
        if (currentGroup != null) {
            currentGroup.removePropertyChangeListener(this);
            for (NotePage page : currentGroup.getPages()) {
                page.removePropertyChangeListener(this);    // remove listeners from all pages in current group
            }
        }

        getChildren().clear();  // clear all page buttons
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        EventPropertyNameEnum event = EventPropertyNameEnum.fromPropertyName(evt.getPropertyName());
        if (event == null) { throw new IllegalArgumentException("Unknown property: " + evt.getPropertyName()); }

        switch (event) {
            case SWITCH_TO_GROUP:
                // Clean up previous group before switching
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
        }
    }

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

    private void updatePageName(NotePage page) {
        for (var node : getChildren()) {
            if (node instanceof ToggleButton button) {
                if (button.getUserData() == page) {
                    button.setText(page.getTitle());
                    break;
                }
            }
        }
    }

    private ContextMenu createPageContextMenu(NotePage page) {
        ContextMenu menu = new ContextMenu();

        // Rename option
        MenuItem renameItem = new MenuItem("Rename");
        renameItem.setOnAction(e -> renamePage(page));

        // Delete option
        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(e -> facade.removePage(page));

        menu.setOnShowing(e -> {
            deleteItem.setDisable(currentGroup.getPages().size() <= 1);  // update disable state dynamically
        });
        menu.getItems().addAll(renameItem, deleteItem);
        return menu;
    }

    private void renamePage(NotePage page) {
        TextInputDialog dialog = new TextInputDialog(page.getTitle());
        dialog.setTitle("Rename Page");
        dialog.setHeaderText("Enter new page title:");
        dialog.setContentText("Title:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(page::setTitle);
    }
}
