package notebookapplication.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

/**
 *
 */
public class NotePageTest {
    private static final String DEFAULT_PAGE_NAME = "Default Page Name";
    private static final String CONTENT =
            """
            This is the first line.
            This is a second line...""";
    private NotePage notePage;

    /**
     *
     */
    @BeforeEach
    public void setUp() {
         notePage = new NotePage(DEFAULT_PAGE_NAME, CONTENT);
    }

    /**
     *
     */
    @Test
    public void testConstructor() {
        assertAll(() -> {
                    assertEquals(DEFAULT_PAGE_NAME, notePage.getPageName());
                    assertEquals(CONTENT, notePage.getContent());
                }
        );
    }

    /**
     *
     */
    @Test
    public void insertToTheEnd() {
        String[] toBeInserted = {"Some inserted string", "\nthe second inserted string"};

        notePage.insertContent(notePage.getContent().length(), toBeInserted[0]);
        System.out.println(notePage.getContent());
        assertEquals(STR."\{CONTENT}\{toBeInserted[0]}", notePage.getContent());

        notePage.insertContent(notePage.getContent().length(), toBeInserted[1]);
        System.out.println(notePage.getContent());
        assertEquals(STR."\{CONTENT}\{toBeInserted[0]}\{toBeInserted[1]}", notePage.getContent());
    }

    /**
     *
     */
    @Test
    public void insertByChar() {
        String inserted = "\nI'm inserting this line of string";
        char[] toBeInserted = inserted.toCharArray();

        for (var character : toBeInserted) {
            notePage.insertContent(notePage.getContent().length(), String.valueOf(character));
        }
        System.out.println(notePage.getContent());
        assertEquals(STR."\{CONTENT}\{inserted}", notePage.getContent());

        for (int i = 0; i < toBeInserted.length; i++) {
            notePage.insertContent(i, String.valueOf(toBeInserted[i]));
        }
        notePage.insertContent(inserted.length(), "\n");
        System.out.println(notePage.getContent());
        assertEquals(STR."\{inserted}\n\{CONTENT}\{inserted}", notePage.getContent());
    }

    /**
     *
     */
    @Test
    public void insertToAnywhere() {
        String toBeInserted = "\nThis line should be inserted\n";
        notePage.insertContent(0, toBeInserted);
        notePage.insertContent(notePage.getContent().length() - 14,
                toBeInserted);
        notePage.insertContent(toBeInserted.length() + 8,
                toBeInserted);
        System.out.println(notePage.getContent());
        assertEquals(
                STR."""
                        \{toBeInserted}This is \{toBeInserted}the first line.
                        This is a \{toBeInserted}second line...""",
                notePage.getContent()
        );
    }

    /**
     *
     */
    @Test
    public void insertContentException() {
        assertAll(() -> {
            assertThrows(IllegalArgumentException.class,
                    () -> notePage.insertContent(-1, "haha"));
            assertThrows(IllegalArgumentException.class,
                    () -> notePage.insertContent(notePage.getContent().length() + 1, "haha"));
            assertThrows(IllegalArgumentException.class,
                    () -> notePage.insertContent(-10, "haha"));
            assertThrows(IllegalArgumentException.class,
                    () -> notePage.insertContent(300, "haha"));
        });
    }

    /**
     *
     */
    @Test
    public void deleteByChar() {
        notePage.deleteContent(notePage.getContent().length() - 1, notePage.getContent().length());
        notePage.deleteContent(0, 1);
        System.out.println(notePage.getContent());
        assertEquals("his is the first line.\nThis is a second line..", notePage.getContent());
    }

    /**
     *
     */
    @Test
    public void deleteByString() {
        notePage.deleteContent(0, 23);
        System.out.println(notePage.getContent());
        assertEquals("\nThis is a second line...", notePage.getContent());

        notePage.deleteContent(0, notePage.getContent().length());
        assertTrue(notePage.getContent().isEmpty());
    }

    /**
     * 
     */
    @Test
    public void deleteContentException() {
        assertAll(() -> {
            assertThrows(IllegalArgumentException.class,
                    () -> notePage.deleteContent(-1, notePage.getContent().length()));
            assertThrows(IllegalArgumentException.class,
                    () -> notePage.deleteContent(0, notePage.getContent().length() + 1));
            assertThrows(IllegalArgumentException.class,
                    () -> notePage.deleteContent(5, 0));
        });
    }

    @Test
    public void replaceString() {
        String replacement = "replaced by this string";
        notePage.replaceContent(8, 31, replacement);
        System.out.println(notePage.getContent());
        assertEquals(STR."This is \{replacement} a second line...", notePage.getContent());
    }

    @Test
    public void replaceAll() {
        String replacement = "replaced by this string";
        notePage.replaceContent(0, notePage.getContent().length(), replacement);
        assertEquals(replacement, notePage.getContent());
    }

    @Test
    public void replaceContentException() {
        assertAll(() -> {
            assertThrows(IllegalArgumentException.class,
                    () -> notePage.replaceContent(
                            -1, notePage.getContent().length(), "haha"));
            assertThrows(IllegalArgumentException.class,
                    () -> notePage.replaceContent(
                            0, notePage.getContent().length() + 1, "haha"));
            assertThrows(IllegalArgumentException.class,
                    () -> notePage.replaceContent(5, 0, "haha"));
        });
    }

    @Test
    public void insertSingleSymbol() {
        // ...
    }
}
