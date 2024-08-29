package notebookapplication.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link NotePage} class.
 * This class tests various functionalities of the NotePage class,
 *      including content insertion, deletion, replacement, symbol insertion,
 *      and formatting.
 */
public class NotePageTest {
    private static final String DEFAULT_PAGE_NAME = "Default Page Name";
    private static final String CONTENT =
            """
            This is the first line.
            This is a second line...""";
    private NotePage notePage;

    /**
     * Sets up a new NotePage instance with static default page name
     *      and content defined above before each test.
     */
    @BeforeEach
    public void setUp() {
        notePage = new NotePage(DEFAULT_PAGE_NAME, CONTENT);
    }

    /**
     * Tests the constructor of the NotePage class.
     * Verifies that the page name and content are correctly initialised.
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
     * Tests inserting content at the end of the note page.
     * Verifies that the content can be correctly appended to the end.
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
     * Tests inserting content character by character.
     * Simulates the case of user typing the characters.
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
     * Tests inserting content at various positions.
     * Verifies that the content is correctly inserted at the specified positions.
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
     * Tests inserting content with invalid positions.
     * Verifies that an IllegalArgumentException is thrown for invalid positions.
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
     * Tests deleting content character by character.
     * Simulate the case of user deleting content by pressing BACK_SPACE or DEL key.
     */
    @Test
    public void deleteByChar() {
        notePage.deleteContent(notePage.getContent().length() - 1, notePage.getContent().length());
        notePage.deleteContent(0, 1);
        System.out.println(notePage.getContent());
        assertEquals("his is the first line.\nThis is a second line..", notePage.getContent());
    }

    /**
     * Tests deleting content by string.
     * Simulate the case of user delete a bunch of characters by selecting them by mouse.
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
     * Tests deleting content with invalid positions.
     * Verifies that an IllegalArgumentException is thrown for invalid positions.
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

    /**
     * Tests replacing content with a new string.
     * Simulate the case of user selects a bunch of text and then replace it by typing characters
     *      or by pasting string with Ctrl + V.
     */
    @Test
    public void replaceString() {
        String replacement = "replaced by this string";
        notePage.replaceContent(8, 31, replacement);
        System.out.println(notePage.getContent());
        assertEquals(STR."This is \{replacement} a second line...", notePage.getContent());
    }

    /**
     * Tests replacing all content with a new string.
     * Verifies that the entire content is correctly replaced.
     */
    @Test
    public void replaceAll() {
        String replacement = "replaced by this string";
        notePage.replaceContent(0, notePage.getContent().length(), replacement);
        assertEquals(replacement, notePage.getContent());
    }

    /**
     * Tests replacing content with invalid positions.
     * Verifies that an IllegalArgumentException is thrown for invalid positions.
     */
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

    /**
     * Tests inserting a symbol at the beginning of a line.
     * Verifies that the symbol is correctly inserted to the front of the specified line
     *      with two spaces separating the symbol and the first character of the line.
     */
    @Test
    public void insertSingleSymbol() {
        notePage.insertSymbol(0, Symbols.BULLET_POINT);
        System.out.println(notePage.getContent());
        assertEquals(STR."\{Symbols.BULLET_POINT.symbol}  \{CONTENT}", notePage.getContent());
    }

    /**
     * Tests inserting multiple symbols at various lines.
     * For each line, the last inserted symbol should be at the most front position of the line.
     */
    @Test
    public void insertMultipleSymbols() {
        notePage.insertSymbol(0, Symbols.SQUARE_BULLET_POINT);
        notePage.insertSymbol(0, Symbols.CHECK_BOX);
        notePage.insertSymbol(1, Symbols.ARROW);
        notePage.insertSymbol(1, Symbols.STAR);
        System.out.println(notePage.getContent());
        assertEquals(
                STR."\{Symbols.CHECK_BOX.symbol}  \{Symbols.SQUARE_BULLET_POINT.symbol}  " +
                STR."This is the first line.\n\{Symbols.STAR.symbol}  \{Symbols.ARROW.symbol}  " +
                "This is a second line...", notePage.getContent());
    }

    /**
     * Tests inserting symbols with invalid line numbers.
     * Verifies that an IllegalArgumentException is thrown for invalid line numbers.
     */
    @Test
    public void insertSymbolException() {
        assertAll(() -> {
            assertThrows(IllegalArgumentException.class,
                    () -> notePage.insertSymbol(-1, Symbols.STAR));
            assertThrows(IllegalArgumentException.class,
                    () -> notePage.insertSymbol(-10, Symbols.CHECK_BOX));
            assertThrows(IllegalArgumentException.class,
                    () -> notePage.insertSymbol(2, Symbols.SQUARE_BULLET_POINT));
            assertThrows(IllegalArgumentException.class,
                    () -> notePage.insertSymbol(20, Symbols.HALLOW_BULLET_POINT));
        });
    }

    /**
     * Tests formatting a single character.
     * Verifies that the character is correctly surrounded with the specified style tag,
     *      while the rest of the content is not changed.
     */
    @Test
    public void formattingChar() {
        notePage.formatting(0, 1,
                () -> TextEditing.setFont(Fonts.MS_YAHEI, notePage.getContent().substring(0, 1)));
        System.out.println(notePage.getContent());
        assertEquals(STR."""
        [style="-fx-font-family: \{Fonts.MS_YAHEI.font};"]T[/style]his is the first line.
        This is a second line...""", notePage.getContent());
    }

    /**
     * Tests formatting some substrings.
     * Simulate the case of user selects a string by mouse and formats it by clicking one of the
     *      formatting buttons.
     */
    @Test
    public void formattingString() {
        notePage.formatting(0, 11,
                () -> TextEditing.setColor(Colors.RED, notePage.getContent().substring(0, 11)));
        notePage.formatting(notePage.getContent().length() - 24, notePage.getContent().length(),
                () -> TextEditing.setColor(Colors.BLUE,
                        notePage.getContent().substring(notePage.getContent().length() - 24)));
        System.out.println(notePage.getContent());
        assertEquals(STR.
                """
                [style="-fx-fill: \{Colors.RED.color};"]This is the[/style] first line.
                [style="-fx-fill: \{Colors.BLUE.color};"]This is a second line...[/style]""",
                notePage.getContent());
    }

    /**
     * Tests formatting with invalid strings and positions.
     * Verifies that an IllegalArgumentException is thrown for invalid formatting strings
     *      or positions.
     */
    @Test
    public void formattingException() {
        assertAll(() -> {
            assertThrows(IllegalArgumentException.class,
                    () -> notePage.formatting(0, 1,
                        () -> "invalid formatting string"));
            assertThrows(IllegalArgumentException.class,
                    () -> notePage.formatting(0, 1,
                        () -> "[style=\"\"\"]nearly correct formatting string[/style]"));
            assertThrows(IllegalArgumentException.class,
                   () -> notePage.formatting(-1, 2,
                       () -> "[style=\"...\"]correct formatting[/style]"));
            assertThrows(IllegalArgumentException.class,
                   () -> notePage.formatting(0, notePage.getContent().length() + 1,
                       () -> "[style=\"...\"]correct formatting[/style]"));
            assertThrows(IllegalArgumentException.class,
                   () -> notePage.formatting(5, 1,
                       () -> "[style=\"...\"]correct formatting[/style]"));
        });
    }
}
