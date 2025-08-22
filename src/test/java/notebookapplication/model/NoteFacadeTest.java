package notebookapplication.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class NoteFacadeTest {
    private NoteFacade facade;
    private TestPropertyChangeListener listener;

    @BeforeEach
    void setUp() {
        facade = new NoteFacade();
        listener = new TestPropertyChangeListener();
        facade.addPropertyChangeListener(listener);
    }

    /** Testing if {@code createNewGroup} does add a new group to the notebook.  */
    @Test
    void createNewGroup_AddsNewGroup() {
        int initialGroupCount = facade.getGroups().size();  // arrange
        facade.createNewGroup();    // act
        assertEquals(initialGroupCount + 1, facade.getGroups().size());
    }

    /** Testing if {@code createNewGroup} does switch to the new group.  */
    @Test
    void createNewGroup_SwitchesToNewGroup() {
        NoteGroup originalGroup = facade.getCurrentGroup();  // arrange

        // Act
        facade.createNewGroup();
        NoteGroup newGroup = facade.getCurrentGroup();

        // Assert
        assertNotEquals(originalGroup, newGroup);
        assertTrue(facade.getGroups().contains(newGroup));
    }

    /**
     * Testing if {@code createNewGroup} does fire ADD_GROUP,
     * SWITCH_TO_GROUP, and SWITCH_TO_PAGE events in this order.
     */
    @Test
    void createNewGroup_FiresCorrectEventSequence() {
        listener.reset();   // reset listener to ignore initial setup events
        facade.createNewGroup();    // act

        // Assert - Check all events were fired
        List<String> eventTypes = listener.getEventTypes();
        assertTrue(eventTypes.size() >= 3, "Expected at least 3 events");
        assertTrue(listener.containsEventType(
                EventPropertyNameEnum.ADD_GROUP.getPropertyName()
                ), "ADD_GROUP event should be fired");
        assertTrue(listener.containsEventType(
                EventPropertyNameEnum.SWITCH_TO_GROUP.getPropertyName()
                ), "SWITCH_TO_GROUP event should be fired");

        assertTrue(listener.containsEventType(
                EventPropertyNameEnum.SWITCH_TO_PAGE.getPropertyName()
                ), "SWITCH_TO_PAGE event should be fired");
    }

    /** Testing if {@code createNewGroup} does ensure new group has at least one page.  */
    @Test
    void createNewGroup_NewGroupHasPages() {
        // Act
        facade.createNewGroup();
        NoteGroup newGroup = facade.getCurrentGroup();

        assertFalse(newGroup.getPages().isEmpty());  // assert
    }

    /** Testing if {@code createNewGroup} does switch to a new page created under the new group.  */
    @Test
    void createNewGroup_SwitchesToNewPage() {
        NotePage oldPage = facade.getCurrentPage(); // arrange

        // Act
        facade.createNewGroup();
        NotePage newPage = facade.getCurrentPage();

        // Assert
        assertNotEquals(oldPage, facade.getCurrentPage());
        assertTrue(facade.getCurrentGroup().getPages().contains(newPage));
    }

    /** Testing if {@code createNewPage} does add a new page to the current group.  */
    @Test
    void createNewPage_AddsNewPage() {
        // Arrange
        NoteGroup currentGroup = facade.getCurrentGroup();
        int initialPageCount = currentGroup.getPages().size();

        facade.createNewPage(currentGroup);   // act

        assertEquals(initialPageCount + 1, currentGroup.getPages().size());    // assert
    }

    /** Testing if {@code createNewPage} does switch to the new page.  */
    @Test
    void createNewPage_SwitchesToNewPage() {
        // Arrange
        NoteGroup currentGroup = facade.getCurrentGroup();
        NotePage originalPage = facade.getCurrentPage();

        // Act
        facade.createNewPage(currentGroup);
        NotePage newPage = facade.getCurrentPage();

        // Assert
        assertNotEquals(originalPage, newPage);
        assertTrue(currentGroup.getPages().contains(newPage));
    }

    /** Testing if {@code createNewPage} works with different groups.  */
    @Test
    void createNewPage_WorksWithDifferentGroups() {
        // Arrange
        facade.createNewGroup();
        NoteGroup newGroup = facade.getCurrentGroup();
        int initialPageCount = newGroup.getPages().size();

        facade.createNewPage(newGroup);  //act

        assertEquals(initialPageCount + 1, newGroup.getPages().size());   // assert
    }

    /**
     * Testing if {@code createNewPage} does fire ADD_PAGE,
     * SWITCH_TO_GROUP, and SWITCH_TO_PAGE events in this order.
     */
    @Test
    void createNewPage_FiresCorrectEventSequence() {
        listener.reset();   // reset listener to ignore initial setup events

        // Arrange
        facade.createNewGroup();
        facade.getCurrentGroup().addPropertyChangeListener(listener);
        facade.createNewPage(facade.getCurrentGroup());

        // Assert - Check all events were fired
        List<String> eventTypes = listener.getEventTypes();
        assertTrue(eventTypes.size() >= 3, "Expected at least 3 events");
        assertTrue(listener.containsEventType(
                EventPropertyNameEnum.ADD_PAGE.getPropertyName()
                ), "ADD_PAGE event should be fired");
        assertTrue(listener.containsEventType(
                EventPropertyNameEnum.SWITCH_TO_GROUP.getPropertyName()
                ), "SWITCH_TO_GROUP event should be fired");

        assertTrue(listener.containsEventType(
                EventPropertyNameEnum.SWITCH_TO_PAGE.getPropertyName()
                ), "SWITCH_TO_PAGE event should be fired");
    }

    /** Testing if {@code removeGroup} does remove specified group when multiple groups exist.  */
    @Test
    void removeGroup_RemovesGroupWhenMultipleExist() {
        // Arrange
        facade.createNewGroup();
        NoteGroup groupToRemove = facade.getCurrentGroup();
        facade.createNewGroup();   // ensure we have multiple groups
        int initialGroupCount = facade.getGroups().size();

        facade.removeGroup(groupToRemove);   // act

        // Assert
        assertEquals(initialGroupCount - 1, facade.getGroups().size());
        assertFalse(facade.getGroups().contains(groupToRemove));
    }

    /** Test if {@code removeGroup} does not remove the last group.  */
    @Test
    void removeGroup_DoesNotRemoveLastGroup() {
        // Arrange
        List<NoteGroup> groups = facade.getGroups();
        int initialGroupCount = groups.size();
        NoteGroup lastGroup = groups.getFirst();

        facade.removeGroup(lastGroup);  // act

        // Assert
        assertEquals(initialGroupCount, groups.size());
        assertTrue(groups.contains(lastGroup));
    }

    /** Testing if {@code removeGroup} does switch to the first group
     * when the current group is removed.
     */
    @Test
    void removeGroup_SwitchesWhenCurrentRemoved() {
        // Arrange
        facade.createNewGroup();
        NoteGroup groupToRemove = facade.getCurrentGroup();

        facade.removeGroup(groupToRemove);
        assertEquals(facade.getGroups().getFirst(), facade.getCurrentGroup());
    }

    /** Testing if {@code removeGroup} does not switch when non-current group is removed.   */
    @Test
    void removeGroup_DoesNotSwitchWhenNonCurrentRemoved() {
        // Arrange
        facade.createNewGroup();
        NoteGroup groupToRemove = facade.getCurrentGroup();
        facade.createNewGroup(); // now we have two groups
        NoteGroup currentGroup = facade.getCurrentGroup();  // the second added group

        facade.removeGroup(groupToRemove);  // remove the first group, not current
        assertEquals(currentGroup, facade.getCurrentGroup());
    }

    /**
     * Testing if {@code removeGroup} does fire REMOVE_GROUP event,
     * followed by SWITCH_TO_GROUP and SWITCH_TO_PAGE events
     * while the current group is removed.
     */
    @Test
    void removeGroup_FiresCorrectEventSequence() {
        listener.reset();   // reset listener to ignore initial setup events

        // Arrange
        facade.createNewGroup();
        facade.createNewGroup(); // ensure we have multiple groups
        NoteGroup groupToRemove = facade.getCurrentGroup();   // remove the current group

        facade.removeGroup(groupToRemove);  // act

        // Assert - Check all events were fired
        List<String> eventTypes = listener.getEventTypes();
        assertTrue(eventTypes.size() >= 3, "Expected at least 3 events");
        assertTrue(listener.containsEventType(
                EventPropertyNameEnum.REMOVE_GROUP.getPropertyName()
                ), "REMOVE_GROUP event should be fired");
        assertTrue(listener.containsEventType(
                EventPropertyNameEnum.SWITCH_TO_GROUP.getPropertyName()
                ), "SWITCH_TO_GROUP event should be fired");

        assertTrue(listener.containsEventType(
                EventPropertyNameEnum.SWITCH_TO_PAGE.getPropertyName()
                ), "SWITCH_TO_PAGE event should be fired");
    }

    /** Testing if {@code removePage} removes the specified page when multiple pages exist.  */
    @Test
    void removePage_RemovesPageWhenMultipleExist() {
        // Arrange
        NoteGroup currentGroup = facade.getCurrentGroup();
        facade.createNewPage(currentGroup); // ensure we have multiple pages
        NotePage pageToRemove = currentGroup.getPages().getFirst(); // the first page
        int initialPageCount = currentGroup.getPages().size();

        facade.removePage(pageToRemove);   // act

        // Assert
        assertEquals(initialPageCount - 1, currentGroup.getPages().size());
        assertFalse(currentGroup.getPages().contains(pageToRemove));
    }

    /** Testing if {@code removePage} does not remove the last page in a group.  */
    @Test
    void removePage_DoesNotRemoveLastPage() {
        // Arrange
        NoteGroup currentGroup = facade.getCurrentGroup();
        List<NotePage> pages = currentGroup.getPages();
        int initialPageCount = pages.size();
        NotePage lastPage = pages.getFirst();

        facade.removePage(lastPage);    // act

        // Assert
        assertEquals(initialPageCount, pages.size());
        assertTrue(pages.contains(lastPage));
    }

    /** Testing if removePage does switch to the first page when the current page is removed.  */
    @Test
    void removePage_SwitchesWhenCurrentRemoved() {
        // Arrange
        NoteGroup currentGroup = facade.getCurrentGroup();
        facade.createNewPage(currentGroup); // Now we have two pages
        NotePage pageToRemove = facade.getCurrentPage();

        facade.removePage(pageToRemove);
        assertEquals(currentGroup.getPages().getFirst(), facade.getCurrentPage());
    }

    /** Testing if {@code removePage} does not switch when non-current page is removed.  */
    @Test
    void removePage_DoesNotSwitchWhenNonCurrentRemoved() {
        // Arrange
        NoteGroup currentGroup = facade.getCurrentGroup();
        facade.createNewPage(currentGroup); // Now we have two pages
        NotePage currentPage = facade.getCurrentPage();
        NotePage pageToRemove = currentGroup.getPages().getFirst(); // The first page, not current

        facade.removePage(pageToRemove);
        assertEquals(currentPage, facade.getCurrentPage());
    }

    /** Testing if {@code removePage} does handle pages from different groups correctly.  */
    @Test
    void removePage_HandlesPagesFromDifferentGroups() {
        // Arrange
        facade.createNewGroup();
        NoteGroup otherGroup = facade.getCurrentGroup();
        facade.createNewPage(otherGroup); // Add a page to the new group
        NotePage pageInOtherGroup = otherGroup.getPages().getFirst();

        // Switch back to original group
        facade.switchToGroup(facade.getGroups().getFirst());
        NotePage currentPage = facade.getCurrentPage();

        // Act
        facade.removePage(pageInOtherGroup);

        // Assert - Should not affect current group or page
        assertEquals(currentPage, facade.getCurrentPage());
        // The page should still be removed from the other group
        assertFalse(otherGroup.getPages().contains(pageInOtherGroup));
    }

    /**
     * Testing if {@code removePage} does fire REMOVE_PAGE event,
     * followed by SWITCH_TO_PAGE events while the current page is removed.
     */
    @Test
    void removePage_FiresCorrectEventSequence() {
        listener.reset();   // reset listener to ignore initial setup events

        // Arrange
        facade.createNewGroup();
        facade.getCurrentGroup().addPropertyChangeListener(listener);

        // Ensure we have multiple pages in the group
        facade.createNewPage(facade.getCurrentGroup());
        NotePage pageToRemove = facade.getCurrentPage();   // remove the current page

        facade.removePage(pageToRemove);  // act

        // Assert - Check all events were fired
        List<String> eventTypes = listener.getEventTypes();
        assertTrue(eventTypes.size() >= 2, "Expected at least 2 events");
        assertTrue(listener.containsEventType(
                EventPropertyNameEnum.REMOVE_PAGE.getPropertyName()
                ), "REMOVE_PAGE event should be fired");
        assertTrue(listener.containsEventType(
                EventPropertyNameEnum.SWITCH_TO_PAGE.getPropertyName()
                ), "SWITCH_TO_PAGE event should be fired");
    }
}
