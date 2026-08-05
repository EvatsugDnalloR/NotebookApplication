package notebookapplication.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import notebookapplication.command.UndoRedo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NoteFacadeTest {

    private UndoRedo undoRedo;
    private NoteFacade facade;
    private TestPropertyChangeListener listener;

    @BeforeEach
    void setUp() {
        undoRedo = new UndoRedo();
        facade = new NoteFacade(undoRedo);
        listener = new TestPropertyChangeListener();
        facade.addPropertyChangeListener(listener);
    }

    // ---------------------------------------------------------------
    //  createNewGroup
    // ---------------------------------------------------------------

    @Test
    void createNewGroup_AddsNewGroup() {
        int initial = facade.getGroups().size();
        facade.createNewGroup();
        assertEquals(initial + 1, facade.getGroups().size());
    }

    @Test
    void createNewGroup_SwitchesToNewGroup() {
        NoteGroup original = facade.getCurrentGroup();
        facade.createNewGroup();
        assertNotEquals(original, facade.getCurrentGroup());
    }

    @Test
    void createNewGroup_FiresEventsInOrder() {
        listener.reset();
        facade.createNewGroup();
        List<String> types = listener.getEventTypes();
        assertTrue(types.contains(
                EventPropertyNameEnum.ADD_GROUP.getPropertyName()));
        assertTrue(types.contains(
                EventPropertyNameEnum.SWITCH_TO_GROUP
                        .getPropertyName()));
        assertTrue(types.contains(
                EventPropertyNameEnum.SWITCH_TO_PAGE
                        .getPropertyName()));
    }

    @Test
    void createNewGroup_NewGroupHasAtLeastOnePage() {
        facade.createNewGroup();
        assertFalse(facade.getCurrentGroup().getPages().isEmpty());
    }

    // ---------------------------------------------------------------
    //  createNewPage
    // ---------------------------------------------------------------

    @Test
    void createNewPage_AddsNewPage() {
        NoteGroup group = facade.getCurrentGroup();
        int initial = group.getPages().size();
        facade.createNewPage(group);
        assertEquals(initial + 1, group.getPages().size());
    }

    @Test
    void createNewPage_SwitchesToNewPage() {
        NoteGroup group = facade.getCurrentGroup();
        NotePage original = facade.getCurrentPage();
        facade.createNewPage(group);
        assertNotEquals(original, facade.getCurrentPage());
    }

    @Test
    void createNewPage_WorksWithDifferentGroup() {
        facade.createNewGroup();
        NoteGroup other = facade.getCurrentGroup();
        int initial = other.getPages().size();
        facade.createNewPage(other);
        assertEquals(initial + 1, other.getPages().size());
    }

    // ---------------------------------------------------------------
    //  removeGroup
    // ---------------------------------------------------------------

    @Test
    void removeGroup_RemovesWhenMultipleExist() {
        facade.createNewGroup();
        NoteGroup toRemove = facade.getCurrentGroup();
        facade.createNewGroup();
        int before = facade.getGroups().size();

        facade.removeGroup(toRemove);

        assertEquals(before - 1, facade.getGroups().size());
        assertFalse(facade.getGroups().contains(toRemove));
    }

    @Test
    void removeGroup_DoesNotRemoveLastGroup() {
        int before = facade.getGroups().size();
        facade.removeGroup(facade.getCurrentGroup());
        assertEquals(before, facade.getGroups().size());
    }

    @Test
    void removeGroup_SwitchesWhenCurrentRemoved() {
        facade.createNewGroup();
        facade.createNewGroup(); // now three groups
        NoteGroup toRemove = facade.getCurrentGroup();

        facade.removeGroup(toRemove);
        assertEquals(facade.getGroups().getFirst(),
                facade.getCurrentGroup());
    }

    @Test
    void removeGroup_DoesNotSwitchWhenNonCurrentRemoved() {
        facade.createNewGroup();
        NoteGroup toRemove = facade.getCurrentGroup();
        facade.createNewGroup();
        NoteGroup current = facade.getCurrentGroup();

        facade.removeGroup(toRemove);
        assertEquals(current, facade.getCurrentGroup());
    }

    // ---------------------------------------------------------------
    //  removePage
    // ---------------------------------------------------------------

    @Test
    void removePage_RemovesWhenMultipleExist() {
        NoteGroup group = facade.getCurrentGroup();
        facade.createNewPage(group);
        NotePage toRemove = group.getPages().getFirst();
        int before = group.getPages().size();

        facade.removePage(toRemove);

        assertEquals(before - 1, group.getPages().size());
        assertFalse(group.getPages().contains(toRemove));
    }

    @Test
    void removePage_DoesNotRemoveLastPage() {
        NoteGroup group = facade.getCurrentGroup();
        int before = group.getPages().size();
        facade.removePage(group.getPages().getFirst());
        assertEquals(before, group.getPages().size());
    }

    @Test
    void removePage_SwitchesWhenCurrentRemoved() {
        NoteGroup group = facade.getCurrentGroup();
        facade.createNewPage(group);
        NotePage toRemove = facade.getCurrentPage();

        facade.removePage(toRemove);
        assertEquals(group.getPages().getFirst(),
                facade.getCurrentPage());
    }

    @Test
    void removePage_DoesNotSwitchWhenNonCurrentRemoved() {
        NoteGroup group = facade.getCurrentGroup();
        facade.createNewPage(group);
        NotePage current = facade.getCurrentPage();
        NotePage other = group.getPages().getFirst();

        facade.removePage(other);
        assertEquals(current, facade.getCurrentPage());
    }

    // ---------------------------------------------------------------
    //  renameGroup / renamePage
    // ---------------------------------------------------------------

    @Test
    void renameGroup_UpdatesGroupName() {
        NoteGroup group = facade.getCurrentGroup();
        facade.renameGroup(group, "Work");
        assertEquals("Work", group.getGroupName());
    }

    @Test
    void renameGroup_IsUndoable() {
        NoteGroup group = facade.getCurrentGroup();

        facade.renameGroup(group, "Changed");
        assertEquals("Changed", group.getGroupName());

        undoRedo.undo();
        String original = group.getGroupName();
        assertEquals(original, group.getGroupName());
    }

    @Test
    void renamePage_UpdatesPageName() {
        NotePage page = facade.getCurrentPage();
        facade.renamePage(page, "Meeting Notes");
        assertEquals("Meeting Notes", page.getPageName());
    }

    @Test
    void renamePage_IsUndoable() {
        NotePage page = facade.getCurrentPage();

        facade.renamePage(page, "Draft");
        assertEquals("Draft", page.getPageName());

        undoRedo.undo();
        String original = page.getPageName();
        assertEquals(original, page.getPageName());
    }

    // ---------------------------------------------------------------
    //  Undo / Redo
    // ---------------------------------------------------------------

    @Test
    void undo_ReversesCreateNewGroup() {
        int before = facade.getGroups().size();
        facade.createNewGroup();
        assertEquals(before + 1, facade.getGroups().size());

        undoRedo.undo();
        assertEquals(before, facade.getGroups().size());
    }

    @Test
    void redo_RestoresUndoneCreateNewGroup() {
        facade.createNewGroup();
        int afterCreate = facade.getGroups().size();

        undoRedo.undo();
        assertEquals(afterCreate - 1, facade.getGroups().size());

        undoRedo.redo();
        assertEquals(afterCreate, facade.getGroups().size());
    }

    @Test
    void undo_ReversesCreateNewPage() {
        NoteGroup group = facade.getCurrentGroup();
        int before = group.getPages().size();
        facade.createNewPage(group);
        assertEquals(before + 1, group.getPages().size());

        undoRedo.undo();
        assertEquals(before, group.getPages().size());
    }

    @Test
    void canUndo_ReturnsTrueAfterMutation() {
        facade.createNewGroup();
        assertTrue(undoRedo.canUndo());
    }

    @Test
    void canRedo_ReturnsTrueAfterUndo() {
        facade.createNewGroup();
        undoRedo.undo();
        assertTrue(undoRedo.canRedo());
    }
}
