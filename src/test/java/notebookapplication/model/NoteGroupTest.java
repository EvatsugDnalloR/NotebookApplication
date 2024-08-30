package notebookapplication.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Scanner;

public class NoteGroupTest {
    private static final String INPUT =
            "[page=\"Page 1\"]Some content...[/page][page=\"Page 2\"]Some other contents " +
                    "that splits over lines\nlike this...[/page]" +
                    "[page=\"Page with formats\"][style=\"-fx-underline: true;\"]" +
                    "This part of text should be bold[/style][/page]";
    private NoteGroup noteGroup;

    @BeforeEach
    public void setUp() {
        noteGroup = new NoteGroup(
                "Default NoteGroup", new Scanner(INPUT));
    }

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
            assertEquals(INPUT, noteGroup.getGroupContent());
        });
    }

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
                            new Scanner("[page=\"Page 1\"][/page]invalid[page=\"Page 2\"][/page]")));
            assertThrows(NullPointerException.class,
                    () -> noteGroup = new NoteGroup("Default NoteGroup",
                            null));
            assertThrows(IllegalArgumentException.class,
                    () -> noteGroup = new NoteGroup("Default NoteGroup",
                            new Scanner("")));
        });
    }
}
