package notebookapplication.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NoteGroupTest {

    private NoteGroup group;
    private TestPropertyChangeListener listener;

    @BeforeEach
    void setUp() {
        group = new NoteGroup();
        listener = new TestPropertyChangeListener();
        group.addPropertyChangeListener(listener);
    }

    // --- setGroupName ---

    @Test
    void setGroupName_UpdatesName() {
        group.setGroupName("Work");
        assertEquals("Work", group.getGroupName());
    }

    @Test
    void setGroupName_FiresGroupRenameEvent() {
        listener.reset();
        group.setGroupName("School");
        assertTrue(listener.containsEventType(
                EventPropertyNameEnum.GROUP_RENAME.getPropertyName()));
    }

    @Test
    void setGroupName_EventHasCorrectOldAndNewValues() {
        String oldName = group.getGroupName();
        group.setGroupName("Projects");
        assertEquals(oldName,
                listener.getEvents().getFirst().getOldValue());
        assertEquals("Projects",
                listener.getEvents().getFirst().getNewValue());
    }

    // --- addPage ---

    @Test
    void addPage_IncreasesPageCount() {
        int initial = group.getPages().size();
        group.addPage(new NotePage("Extra"));
        assertEquals(initial + 1, group.getPages().size());
    }

    @Test
    void addPage_FiresAddPageEvent() {
        listener.reset();
        group.addPage(new NotePage("New Page"));
        assertTrue(listener.containsEventType(
                EventPropertyNameEnum.ADD_PAGE.getPropertyName()));
    }

    // --- removePage ---

    @Test
    void removePage_RemovesExistingPage() {
        NotePage page = new NotePage("ToRemove");
        group.addPage(page);
        int before = group.getPages().size();

        group.removePage(page);

        assertEquals(before - 1, group.getPages().size());
        assertFalse(group.getPages().contains(page));
    }

    @Test
    void removePage_FiresRemovePageEvent() {
        NotePage page = new NotePage("Temp");
        group.addPage(page);
        listener.reset();

        group.removePage(page);

        assertTrue(listener.containsEventType(
                EventPropertyNameEnum.REMOVE_PAGE.getPropertyName()));
    }

    // --- getPageById ---

    @Test
    void getPageById_FindsExistingPage() {
        NotePage page = new NotePage("Target");
        group.addPage(page);

        NotePage found = group.getPageById(page.getId());
        assertNotNull(found);
        assertEquals("Target", found.getPageName());
    }

    @Test
    void getPageById_ReturnsNullForUnknownId() {
        NotePage found = group.getPageById(java.util.UUID.randomUUID());
        assertNull(found);
    }
}