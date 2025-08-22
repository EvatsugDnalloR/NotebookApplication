package notebookapplication.model;

import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * Represents a single page of content in a notebook with rich text formatting capabilities.
 *
 * <p>Each page maintains a collection of text segments with individual styling and implemented
 * model-observer pattern by property change notifications, for UI synchronisation.
 */
public class NotePage extends NoteSubject implements Serializable {
    private final UUID id;
    private String pageName;
    private final List<TextSegment> contentSegments;    // contents of the page stored

    /** Creates a new note page with default title and empty content. */
    public NotePage() {
        this.id = UUID.randomUUID();
        this.pageName = "Untitled Page";
        this.contentSegments = new ArrayList<>();
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
        this.contentSegments = new ArrayList<>();
        support = new PropertyChangeSupport(this);
    }

    /**
     * Creates a note page with all properties (primarily for loading from storage).
     *
     * @param id the unique identifier for the page
     * @param pageName the title of the page
     * @param segments the text segments with styling information
     */
    public NotePage(UUID id, String pageName, List<TextSegment> segments) {
        this.id = id;
        this.pageName = pageName;
        this.contentSegments = new ArrayList<>(segments);
        support = new PropertyChangeSupport(this);
    }

    public UUID getId() {
        return id;
    }

    public String getPageName() {
        return pageName;
    }

    /**
     * Updates the page title and notifies registered listeners of the change.
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
     * Get all text segments as an ordered ArrayList.
     *
     * @return all text segments with their styling
     */
    public List<TextSegment> getContentSegments() {
        return new ArrayList<>(contentSegments);
    }

    /**
     * Replaces all content segments with new content and notifies listeners.
     *
     * @param segments the new text segments to replace current content
     */
    public void setContent(List<TextSegment> segments) {
        List<TextSegment> oldContentSegments = getContentSegments();
        contentSegments.clear();
        contentSegments.addAll(segments);
        support.firePropertyChange(
            EventPropertyNameEnum.SET_CONTENT.getPropertyName(), oldContentSegments, contentSegments
        );
    }

    /**
     * Replaces all content with plain text (no styling) and notifies listeners.
     *
     * @param text the plain text content to set
     */
    public void setPlainText(String text) {

        contentSegments.clear();
        contentSegments.add(new TextSegment(text, ""));
        support.firePropertyChange(
                EventPropertyNameEnum.SET_CONTENT.getPropertyName(), null, contentSegments
        );
    }

    /**
     * Returns a plain text representation of all content segments.
     *
     * @return concatenated text from all segments without styling information
     */
    public String getPlainTextContent() {
        StringBuilder sb = new StringBuilder();
        for (TextSegment segment : contentSegments) {
            sb.append(segment.text());
        }
        return sb.toString();
    }

    /**
     * Converts the content to HTML for storage and rendering.
     *
     * @return HTML representation with styling
     */
    public String toHtml() {
        StringBuilder html = new StringBuilder();
        html.append("<div class='note-content'>");

        for (TextSegment segment : contentSegments) {
            html.append("<span style='")
                    .append(segment.cssStyle())
                    .append("'>")
                    .append(escapeHtml(segment.text()))
                    .append("</span>");
        }

        html.append("</div>");
        return html.toString();
    }

    /**
     * Loads content from HTML representation.
     *
     * @param html HTML string with styling
     */
    public void fromHtml(String html) {
        contentSegments.clear();
        // Simplified parser - in practice use a proper HTML parser
        String[] parts = html.split("<span style='|</span>");

        for (int i = 1; i < parts.length; i += 2) {
            String styleAndText = parts[i];
            int endStyle = styleAndText.indexOf("'>");
            if (endStyle != -1) {
                String style = styleAndText.substring(0, endStyle);
                String text = unescapeHtml(styleAndText.substring(endStyle + 2));
                contentSegments.add(new TextSegment(text, style));
            }
        }
    }

    // Helper methods for HTML escaping
    private String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String unescapeHtml(String text) {
        return text.replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'");
    }
}