package notebookapplication.model;

import java.beans.PropertyChangeSupport;
import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;


/**
 * Represents a single page of content in a notebook with rich text formatting.
 *
 * <p>Content is stored as raw HTML body text (the inner content between
 * {@code <body>} and {@code </body>} from the HTMLEditor). The
 * {@link #toHtml()} and {@link #fromHtml(String)} methods handle wrapping
 * and unwrapping the standard {@code <div class='note-content'>} container.
 *
 * <p>Implements the model-observer pattern via property change notifications
 * for UI synchronisation.
 */
public class NotePage extends NoteSubject implements Serializable {

    @Serial
    private static final long serialVersionUID = 3L;
    private final UUID id;
    private String pageName;
    private String htmlBodyContent;
    private String fontFamily;

    /** Creates a new note page with default title and empty content. */
    public NotePage() {
        this.id = UUID.randomUUID();
        this.pageName = "Untitled Page";
        this.htmlBodyContent = null;
        this.fontFamily = "Arial";  // default font
        support = new PropertyChangeSupport(this);
    }

    /**
     * Creates a new note page with specified title.
     *
     * @param pageName The title of the note page
     */
    public NotePage(String pageName) {
        this.id = UUID.randomUUID();
        this.pageName = pageName;
        this.htmlBodyContent = null;
        this.fontFamily = "Arial";  // default font
        support = new PropertyChangeSupport(this);
    }

    public UUID getId() {
        return id;
    }

    public String getPageName() {
        return pageName;
    }

    /**
     * Updates the page title and notifies registered listeners.
     *
     * @param pageName the new title for the page
     */
    public void setPageName(String pageName) {
        String oldTitle = this.pageName;
        this.pageName = pageName;
        support.firePropertyChange(
                EventPropertyNameEnum.PAGE_RENAME.getPropertyName(), oldTitle, this.pageName
        );
    }

    /**
     * Font family getter.
     *
     * @return  String of font family the current page has
     */
    public String getFontFamily() {
        return fontFamily;
    }

    /**
     * Sets the font family for this page.
     * Page-level setting that does not affect per-character bold/italic/underline formatting.
     */
    public void setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
    }

    /**
     * Returns the raw HTML body content stored by the HTMLEditor.
     *
     * @return the HTML body string, or null if no content has been stored yet
     */
    public String getHtmlBody() {
        return htmlBodyContent;
    }

    /**
     * Stores raw HTML body content from the HTMLEditor and notifies listeners.
     *
     * @param html the HTML body content to store
     */
    public void setHtmlBody(String html) {
        String oldHtml = this.htmlBodyContent;
        this.htmlBodyContent = html;
        support.firePropertyChange(
                EventPropertyNameEnum.SET_CONTENT.getPropertyName(), oldHtml, this.htmlBodyContent
        );
    }

    /**
     * Returns the plain text content by stripping HTML tags.
     *
     * @return plain text representation of the stored HTML
     */
    public String getPlainTextContent() {
        if (htmlBodyContent == null) {
            return "";
        }
        return htmlBodyContent.replaceAll("<[^>]+>", "");
    }

    /**
     * Converts the content to a self-contained HTML fragment for storage and
     * for feeding into the HTMLEditor (after document wrapping by the Controller).
     *
     * @return HTML fragment wrapped in {@code <div class='note-content'>},
     *         or an empty div with a {@code <br>} if no content exists
     */
    public String toHtml() {
        if (htmlBodyContent != null && !htmlBodyContent.isEmpty()) {
            return "<div class='note-content'>" + htmlBodyContent + "</div>";
        }
        return "<div class='note-content'></div>";
    }

    /**
     * Loads content from an HTML fragment, stripping the wrapper div.
     *
     * @param html HTML fragment, optionally wrapped in
     *             {@code <div class='note-content'>}
     */
    public void fromHtml(String html) {
        String innerHtml = html;
        if (html.startsWith("<div class='note-content'>") && html.endsWith("</div>")) {
            innerHtml = html.substring("<div class='note-content'>".length(),
                    html.length() - "</div>".length());
        }
        setHtmlBody(innerHtml);
    }
}