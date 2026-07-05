package notebookapplication.model;

import java.beans.PropertyChangeSupport;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * Represents a single page of content in a notebook with rich text formatting capabilities.
 *
 * <p>Each page maintains a collection of text segments with individual styling and implemented
 * model-observer pattern by property change notifications, for UI synchronisation.
 *
 * <p>Content is stored in two forms:
 * <ul>
 *   <li>{@code htmlBodyContent} — raw HTML body from the HTMLEditor, takes precedence in
 *       {@link #toHtml()}. This is the primary storage when the GUI uses the HTML editor.</li>
 *   <li>{@code contentSegments} — structured list of {@link TextSegment} records with
 *       per-segment CSS styling. Used as fallback when no HTML body has been stored.</li>
 * </ul>
 */
public class NotePage extends NoteSubject implements Serializable {

    @Serial
    private static final long serialVersionUID = 2L;    // CHANGED: added for serialization stability

    private final UUID id;
    private String pageName;
    private final List<TextSegment> contentSegments;    // contents of the page stored

    // NEW: raw HTML body content from HTMLEditor, takes precedence in toHtml()
    private String htmlBodyContent;

    /** Creates a new note page with default title and empty content. */
    public NotePage() {
        this.id = UUID.randomUUID();
        this.pageName = "Untitled Page";
        this.contentSegments = new ArrayList<>();
        this.htmlBodyContent = null;    // NEW
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
        this.htmlBodyContent = null;    // NEW
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
        this.htmlBodyContent = null;    // NEW
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

    // NEW: getter and setter for raw HTML body content

    /**
     * Returns the raw HTML body content stored by the HTMLEditor.
     *
     * @return the HTML body string, or null if no HTML content has been stored yet
     */
    public String getHtmlBody() {
        return htmlBodyContent;
    }

    /**
     * Stores raw HTML body content from the HTMLEditor.
     *
     * <p>After calling this method, {@link #toHtml()} will return this content
     * (wrapped in the standard {@code <div class='note-content'>}) instead of
     * generating HTML from {@code contentSegments}.
     *
     * @param html the HTML body content to store
     */
    public void setHtmlBody(String html) {
        this.htmlBodyContent = html;
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
     * <p>This method invalidates any previously stored {@code htmlBodyContent} since
     * the content is being replaced programmatically.
     *
     * @param segments the new text segments to replace current content
     */
    public void setContent(List<TextSegment> segments) {
        this.htmlBodyContent = null;    // NEW: invalidate cached HTML
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
     * <p>This method invalidates any previously stored {@code htmlBodyContent} since
     * the content is being replaced programmatically.
     *
     * @param text the plain text content to set
     */
    public void setPlainText(String text) {
        this.htmlBodyContent = null;    // NEW: invalidate cached HTML
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

    // CHANGED: toHtml() now prefers htmlBodyContent when available

    /**
     * Converts the content to HTML for storage and rendering.
     *
     * <p>If {@code htmlBodyContent} has been set (e.g. by the HTMLEditor), that content
     * is returned wrapped in the standard {@code <div class='note-content'>} wrapper.
     * Otherwise, HTML is generated from the {@code contentSegments} list.
     *
     * @return HTML representation with styling
     */
    public String toHtml() {
        // NEW: prefer raw HTML body if available
        if (htmlBodyContent != null && !htmlBodyContent.isEmpty()) {
            return "<div class='note-content'>" + htmlBodyContent + "</div>";
        }

        // Fallback: generate from contentSegments
        StringBuilder html = new StringBuilder();
        html.append("<div class='note-content'>");

        if (contentSegments.isEmpty()) {
            html.append("<br>");    // NEW: ensure the editor has clickable content
        } else {
            for (TextSegment segment : contentSegments) {
                html.append("<span style='")
                        .append(segment.cssStyle())
                        .append("'>")
                        .append(escapeHtml(segment.text()))
                        .append("</span>");
            }
        }

        html.append("</div>");
        return html.toString();
    }

    // CHANGED: fromHtml() now also stores the raw HTML for HTMLEditor use

    /**
     * Loads content from HTML representation.
     *
     * <p>Stores the body content as raw HTML for the HTMLEditor and also attempts to
     * parse it into {@code TextSegment} objects for backward compatibility.
     *
     * @param html HTML string with styling, optionally wrapped in
     *             {@code <div class='note-content'>}
     */
    public void fromHtml(String html) {
        // NEW: strip wrapper div and store raw HTML
        String innerHtml = html;
        if (html.startsWith("<div class='note-content'>") && html.endsWith("</div>")) {
            innerHtml = html.substring("<div class='note-content'>".length(),
                    html.length() - "</div>".length());
        }
        this.htmlBodyContent = innerHtml;

        // Existing: parse into contentSegments for backward compatibility
        contentSegments.clear();
        String[] parts = innerHtml.split("<span style='|</span>");

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