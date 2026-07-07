package notebookapplication.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NotePageTest {
    private NotePage page;
    private TestPropertyChangeListener listener;

    @BeforeEach
    void setUp() {
        page = new NotePage();
        listener = new TestPropertyChangeListener();
        page.addPropertyChangeListener(listener);
    }

    // --- setPageName ---

    @Test
    void setPageName_UpdatesName() {
        page.setPageName("Meeting Notes");
        assertEquals("Meeting Notes", page.getPageName());
    }

    @Test
    void setPageName_FiresPageRenameEvent() {
        listener.reset();
        page.setPageName("New Title");
        assertTrue(listener.containsEventType(
                EventPropertyNameEnum.PAGE_RENAME.getPropertyName()));
    }

    @Test
    void setPageName_EventHasCorrectOldAndNewValues() {
        String oldName = page.getPageName();
        page.setPageName("Changed");
        assertEquals(1, listener.getEventTypes().size());
        assertEquals(oldName,
                listener.getEvents().getFirst().getOldValue());
        assertEquals("Changed",
                listener.getEvents().getFirst().getNewValue());
    }

    // --- setHtmlBody ---

    @Test
    void setHtmlBody_StoresContent() {
        page.setHtmlBody("<b>Hello</b>");
        assertEquals("<b>Hello</b>", page.getHtmlBody());
    }

    @Test
    void setHtmlBody_FiresSetContentEvent() {
        listener.reset();
        page.setHtmlBody("some html");
        assertTrue(listener.containsEventType(
                EventPropertyNameEnum.SET_CONTENT.getPropertyName()));
    }

    // --- toHtml ---

    @Test
    void toHtml_WrapsContentInDiv() {
        page.setHtmlBody("hello");
        assertEquals("<div class='note-content'>hello</div>",
                page.toHtml());
    }

    @Test
    void toHtml_ReturnsPlaceholderWhenContentIsNull() {
        assertEquals("<div class='note-content'><br></div>",
                page.toHtml());
    }

    @Test
    void toHtml_ReturnsPlaceholderWhenContentIsEmpty() {
        page.setHtmlBody("");
        assertEquals("<div class='note-content'><br></div>",
                page.toHtml());
    }

    // --- fromHtml ---

    @Test
    void fromHtml_StripsWrapperDivAndSetsContent() {
        page.fromHtml(
                "<div class='note-content'>inner text</div>");
        assertEquals("inner text", page.getHtmlBody());
    }

    @Test
    void fromHtml_HandlesHtmlWithoutWrapperDiv() {
        page.fromHtml("plain text");
        assertEquals("plain text", page.getHtmlBody());
    }

    // --- getPlainTextContent ---

    @Test
    void getPlainTextContent_StripsHtmlTags() {
        page.setHtmlBody("<b>bold</b> <i>italic</i>");
        assertEquals("bold italic", page.getPlainTextContent());
    }

    @Test
    void getPlainTextContent_ReturnsEmptyWhenNull() {
        assertEquals("", page.getPlainTextContent());
    }
}