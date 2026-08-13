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
 * A vertical bar (VBox) component that displays and manages note
 * pages as toggle buttons.
 *
 * <p>Provides functionality for adding, removing, renaming, moving
 * pages, and switching between them.
 *
 * <p>Implements PropertyChangeListener to respond to model changes.
 */
public class PageBar extends VBox implements PropertyChangeListener {
    private final ToggleGroup toggleGroup = new ToggleGroup();
    private final NoteFacade facade;

    private NoteGroup currentGroup;

    /**
     * Creates a page bar bound to the given facade.
     *
     * @param facade the facade providing notebook model operations
     */
    public PageBar(NoteFacade facade) {
        this.facade = facade;
        this.currentGroup = facade.getCurrentGroup();

        facade.addPropertyChangeListener(this);
        currentGroup.addPropertyChangeListener(this);

        initializePages();
        setupToggleGroup();
    }

    private void initializePages() {
        for (NotePage page : currentGroup.getPages()) {
            addPageButton(page);
        }
        selectCurrentPage();
    }

    private void addPageButton(NotePage page) {
        ToggleButton button = new ToggleButton(page.getPageName());
        button.setUserData(page);
        button.setToggleGroup(toggleGroup);

        button.setContextMenu(createPageContextMenu(page));

        button.setOnAction(_ -> {
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
                (_, oldToggle, newToggle) -> {
                if (newToggle == null && oldToggle != null) {
                    toggleGroup.selectToggle(oldToggle);
                }
            }
        );
    }

    private void cleanupCurrentGroup() {
        if (currentGroup != null) {
            currentGroup.removePropertyChangeListener(this);
            for (NotePage page : currentGroup.getPages()) {
                page.removePropertyChangeListener(this);
            }
        }
        getChildren().clear();
    }

    /** Rebuilds the toggle buttons to reflect the current page order. */
    private void refreshPageOrder() {
        for (NotePage page : currentGroup.getPages()) {
            page.removePropertyChangeListener(this);
        }
        getChildren().clear();
        initializePages();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        EventPropertyNameEnum event = EventPropertyNameEnum.fromPropertyName(evt.getPropertyName());
        if (event == null) {
            throw new IllegalArgumentException("Unknown property: " + evt.getPropertyName());
        }

        switch (event) {
            case SWITCH_TO_GROUP:
                cleanupCurrentGroup();
                currentGroup = (NoteGroup) evt.getNewValue();
                currentGroup.addPropertyChangeListener(this);
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

            case MOVE_PAGE:
                refreshPageOrder();
                break;

            case SWITCH_TO_PAGE:
                selectCurrentPage();
                break;

            case PAGE_RENAME:
                updatePageName((NotePage) evt.getSource());
                break;

            case LOAD_NOTEBOOK:
                cleanupCurrentGroup();
                currentGroup = facade.getCurrentGroup();
                currentGroup.addPropertyChangeListener(this);
                initializePages();
                break;

            default:
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
        if (getChildren().isEmpty()) {
            facade.createNewPage(currentGroup);
        }
    }

    private void updatePageName(NotePage page) {
        for (var node : getChildren()) {
            if (node instanceof ToggleButton button && button.getUserData() == page) {
                button.setText(page.getPageName());
                break;
            }
        }
    }

    private ContextMenu createPageContextMenu(NotePage page) {
        MenuItem renameItem = new MenuItem("Rename");
        renameItem.setOnAction(_ -> renamePage(page));

        MenuItem moveUpItem = new MenuItem("Move Up");
        moveUpItem.setOnAction(_ -> facade.movePage(page, -1));

        MenuItem moveDownItem = new MenuItem("Move Down");
        moveDownItem.setOnAction(_ -> facade.movePage(page, 1));

        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(_ -> facade.removePage(page));

        ContextMenu menu = new ContextMenu();
        menu.setOnShowing(_ -> {
            int idx = currentGroup.getPages().indexOf(page);
            moveUpItem.setDisable(idx <= 0);
            moveDownItem.setDisable(idx < 0 || idx >= currentGroup.getPages().size() - 1);
            deleteItem.setDisable(currentGroup.getPages().size() <= 1);
        });

        menu.getItems().addAll(renameItem, moveUpItem, moveDownItem, deleteItem);
        return menu;
    }

    private void renamePage(NotePage page) {
        TextInputDialog dialog = new TextInputDialog(page.getPageName());
        dialog.setTitle("Rename Page");
        dialog.setHeaderText("Enter new page title:");
        dialog.setContentText("Title:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> facade.renamePage(page, name));
    }
}
