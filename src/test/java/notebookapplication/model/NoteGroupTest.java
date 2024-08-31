package notebookapplication.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Scanner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link NoteGroup} class.
 * This class tests all the functionalities available inside a NoteGroup,
 *      including adding pages, delete pages, and reordering pages, but mainly
 *      to test the constructor where to correctly initialise the NoteGroup
 *      with the scanner input.
 */
public class NoteGroupTest {
    private static final String INPUT =
            "[page=\"Page 1\"]Some content...[/page][page=\"Page 2\"]Some other contents "
                    + "that splits over lines\nlike this...[/page]"
                    + "[page=\"Page with formats\"][style=\"-fx-underline: true;\"]"
                    + "This part of text should be bold[/style][/page]";
    private NoteGroup noteGroup;

    /**
     * Sets up the test environment before each test.
     * Initializes a NoteGroup with predefined input.
     */
    @BeforeEach
    public void setUp() {
        noteGroup = new NoteGroup(
                "Default NoteGroup", new Scanner(INPUT));
    }

    /**
     * Tests the constructor of NoteGroup.
     * Verifies that the group name and note pages are correctly initialised.
     */
    @Test
    public void testConstructor() {
        assertAll(() -> {
            assertEquals("Default NoteGroup", noteGroup.getGroupName());
            assertEquals(3, noteGroup.getNotePages().size());
            assertEquals("Page 1", noteGroup.getNotePages().getFirst().getPageName());
            assertEquals("Some content...", noteGroup.getNotePages().getFirst().getContent());
            assertEquals("Page 2", noteGroup.getNotePages().get(1).getPageName());
            assertEquals("Some other contents that splits over lines\nlike this...",
                    noteGroup.getNotePages().get(1).getContent());
            assertEquals("Page with formats", noteGroup.getNotePages().getLast().getPageName());
            assertEquals("[style=\"-fx-underline: true;\"]This part of text should be bold[/style]",
                    noteGroup.getNotePages().getLast().getContent());
        });
    }

    /**
     * Tests the constructor of NoteGroup for various invalid inputs.
     * Verifies that the appropriate exceptions are thrown.
     */
    @Test
    public void constructorException() {
        assertAll(() -> {
            assertThrows(IllegalArgumentException.class,
                    () -> noteGroup = new NoteGroup("Default NoteGroup",
                        new Scanner("[page=\"Page 1\"][/page][page=\"Page 2\"\"][/page]")));
            assertThrows(IllegalArgumentException.class,
                    () -> noteGroup = new NoteGroup("Default NoteGroup",
                        new Scanner(STR."\{INPUT}some other garbage information[/page]")));
            assertThrows(IllegalArgumentException.class,
                    () -> noteGroup = new NoteGroup("Default NoteGroup",
                        new Scanner("[page=\"nearly correct\"][page]")));
            assertThrows(IllegalArgumentException.class,
                    () -> noteGroup = new NoteGroup("Default NoteGroup",
                            new Scanner(
                            "[page=\"Page 1\"][/page]invalid[page=\"Page 2\"][/page]")));
            assertThrows(NullPointerException.class,
                    () -> noteGroup = new NoteGroup("Default NoteGroup",
                            null));
            assertThrows(IllegalArgumentException.class,
                    () -> noteGroup = new NoteGroup("Default NoteGroup",
                            new Scanner("")));
        });
    }

    /**
     * Tests the addPage method of NoteGroup.
     * Verifies that pages are correctly added and the content is updated.
     */
    @Test
    public void addPageTest() {
        noteGroup.addPage(new NotePage("Normal Name", "Normal content..."));
        noteGroup.addPage(new NotePage("Not normal name", "Not normal content..."));
        noteGroup.addPage(new NotePage("Formatted note",
                TextEditing.setColor(Colors.BLACK, "some text here...")));
        assertEquals(6, noteGroup.getNotePages().size());

        // Build the actual content string
        StringBuilder builder = new StringBuilder();
        noteGroup.getNotePages().forEach(notePage -> builder.append(
                STR."[page=\"\{notePage.getPageName()}\"]\{notePage.getContent()}[/page]"
        ));

        assertEquals(STR."\{INPUT}[page=\"Normal Name\"]Normal content...[/page]"
                        + "[page=\"Not normal name\"]Not normal content...[/page]"
                        + "[page=\"Formatted note\"][style=\"-fx-fill: "
                        + STR."\{Colors.BLACK.color};\"]some text here...[/style][/page]",
                builder.toString()
        );
    }

    /**
     * Tests the addPage method of NoteGroup for invalid input.
     * Verifies that a NullPointerException is thrown when adding a null page.
     */
    @Test
    public void addPageException() {
        assertThrows(NullPointerException.class, () -> noteGroup.addPage(null));
    }

    /**
     * Tests the deletePage method of NoteGroup.
     * Verifies that a single page is correctly deleted and the content is updated.
     */
    @Test
    public void deleteSinglePageTest() {
        noteGroup.deletePage(noteGroup.getNotePages().size() - 1);

        // Build the actual content string after deletion
        StringBuilder builder = new StringBuilder();
        noteGroup.getNotePages().forEach(notePage -> builder.append(
                STR."[page=\"\{notePage.getPageName()}\"]\{notePage.getContent()}[/page]"
        ));

        assertEquals(
                "[page=\"Page 1\"]Some content...[/page][page=\"Page 2\"]Some other contents "
                        + "that splits over lines\nlike this...[/page]", builder.toString()

        );
    }

    /**
     * Tests the deletePage method of NoteGroup by deleting all pages.
     * Verifies that all pages are correctly deleted and the notePages list is empty.
     */
    @Test
    public void deleteAllPagesTest() {
        for (int i = 0; i < noteGroup.getNotePages().size() + i; i++) {
            noteGroup.deletePage(noteGroup.getNotePages().size() - 1);
        }
        assertTrue(noteGroup.getNotePages().isEmpty());
    }

    /**
     * Tests the deletePage method of NoteGroup for invalid positions.
     * Verifies that the IllegalArgumentExceptions are thrown.
     */
    @Test
    public void deletePageException() {
        assertAll(() -> {
            assertThrows(IllegalArgumentException.class, () -> noteGroup.deletePage(-1));
            assertThrows(IllegalArgumentException.class,
                    () -> noteGroup.deletePage(noteGroup.getNotePages().size()));
            assertThrows(IllegalArgumentException.class, () -> noteGroup.deletePage(-10));
            assertThrows(IllegalArgumentException.class, () -> noteGroup.deletePage(100));
        });
    }

    /**
     * Tests the changePagesOrder method of NoteGroup.
     * Verifies that pages are correctly reordered and the content is updated.
     */
    @Test
    public void changePagesOrderTest() {
        noteGroup.changePagesOrder(noteGroup.getNotePages().size() - 1, 0);
        noteGroup.changePagesOrder(1, noteGroup.getNotePages().size() - 1);

        // Build the actual content string after reordering
        StringBuilder builder = new StringBuilder();
        noteGroup.getNotePages().forEach(notePage -> builder.append(
                STR."[page=\"\{notePage.getPageName()}\"]\{notePage.getContent()}[/page]"
        ));

        assertEquals("[page=\"Page with formats\"][style=\"-fx-underline: true;\"]"
                + "This part of text should be bold[/style][/page][page=\"Page 2\"]"
                + "Some other contents that splits over lines\nlike this...[/page]"
                + "[page=\"Page 1\"]Some content...[/page]",
                builder.toString());
    }

    /**
     * Tests the changePagesOrder method of NoteGroup for invalid positions.
     * Verifies that the appropriate exceptions are thrown.
     */
    @Test
    public void changePagesOrderException() {
        assertAll(() -> {
            assertThrows(IllegalArgumentException.class,
                    () -> noteGroup.changePagesOrder(-1, 0));
            assertThrows(IllegalArgumentException.class,
                    () -> noteGroup.changePagesOrder(0, -1));
            assertThrows(IllegalArgumentException.class,
                    () -> noteGroup.changePagesOrder(noteGroup.getNotePages().size(), 1));
            assertThrows(IllegalArgumentException.class,
                    () -> noteGroup.changePagesOrder(
                noteGroup.getNotePages().size() - 1, noteGroup.getNotePages().size()
                )
            );
        });
    }
}
