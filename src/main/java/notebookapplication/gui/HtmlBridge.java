package notebookapplication.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fxmisc.richtext.InlineCssTextArea;
import org.fxmisc.richtext.model.StyledSegment;

/**
 * Converts between HTML body content (as stored in {@code NotePage})
 * and the styled document inside an {@link InlineCssTextArea}.
 *
 * <p>The HTML format is a list of paragraphs, each wrapped in a
 * {@code <div style='...'>} element carrying the paragraph-level CSS (e.g. {@code -fx-text-alignment}).
 * Inside each div, {@code <span style='...'>text</span>} elements carry the
 * character-level styles (bold, italic, colour, size, font).
 * Paragraph breaks are represented by newline characters between div groups.
 * Legacy HTML without div wrappers is still accepted.
 */
public final class HtmlBridge {

    private static final Pattern SPAN_PATTERN = Pattern.compile(
            "<span\\s+style='([^']*)'>(.*?)</span>", Pattern.DOTALL
    );

    private static final Pattern DIV_PATTERN = Pattern.compile(
            "^<div\\s+style='([^']*)'>(.*)</div>$", Pattern.DOTALL
    );

    private static final String WRAPPER_START = "<div class='note-content'>";
    private static final String WRAPPER_END = "</div>";

    /**
     * Populates the given text area from an HTML body string.
     *
     * <p>Paragraphs are split on newline boundaries.
     * Each paragraph may be wrapped in a {@code <div style='...'>} carrying its paragraph-level style;
     * within it {@code <span>} elements are parsed for text and character-level CSS styles.
     * {@code <br>} tags are silently stripped.
     */
    public static void populateArea(InlineCssTextArea area, String html) {
        area.clear();
        if (html == null || html.isBlank()) {
            return;
        }

        String inner = unwrap(html);
        // Strip <br> self-closing tags, which are HTML placeholders
        inner = inner.replaceAll("<br\\s*/?>", "");

        String[] paragraphs = inner.split("\n", -1);
        StringBuilder fullText = new StringBuilder();
        List<StyleRange> styleRanges = new ArrayList<>();
        List<String> paragraphStyles = new ArrayList<>();

        for (int p = 0; p < paragraphs.length; p++) {
            String paraHtml = paragraphs[p];
            String parStyle = null;

            // Optional <div style='...'> wrapper for paragraph-level CSS
            Matcher divMatcher = DIV_PATTERN.matcher(paraHtml);
            if (divMatcher.matches()) {
                parStyle = unescapeHtml(divMatcher.group(1).trim());
                paraHtml = divMatcher.group(2);
            }
            paragraphStyles.add(parStyle);

            Matcher m = SPAN_PATTERN.matcher(paraHtml);
            boolean foundSpan = false;

            while (m.find()) {
                foundSpan = true;
                String style = m.group(1).trim();
                String text = unescapeHtml(m.group(2));
                int start = fullText.length();
                fullText.append(text);
                if (!style.isEmpty()) {
                    styleRanges.add(new StyleRange(start, start + text.length(), style));
                }
            }

            if (!foundSpan) {
                // No spans — plain text paragraph
                fullText.append(unescapeHtml(paraHtml));
            }

            if (p < paragraphs.length - 1) {
                fullText.append("\n");
            }
        }

        if (!fullText.isEmpty()) {
            area.replaceText(0, 0, fullText.toString());
            for (StyleRange sr : styleRanges) {
                area.setStyle(sr.start, sr.end, sr.style);
            }
            for (int p = 0; p < paragraphStyles.size(); p++) {
                String ps = paragraphStyles.get(p);
                if (ps != null && !ps.isEmpty()) {
                    area.setParagraphStyle(p, ps);
                }
            }
        }
    }

    /**
     * Extracts the HTML body from the given text area.
     *
     * <p>Each paragraph is wrapped in {@code <div style='...'>} carrying its paragraph-level style,
     * followed by its character-level {@code <span>} elements.
     */
    public static String extractHtml(InlineCssTextArea area) {
        StringBuilder sb = new StringBuilder();
        sb.append(WRAPPER_START);

        for (int p = 0; p < area.getParagraphs().size(); p++) {
            var par = area.getParagraphs().get(p);
            String parStyle = par.getParagraphStyle();
            sb.append("<div");
            if (parStyle != null && !parStyle.isBlank()) {
                sb.append(" style='").append(escapeHtml(parStyle)).append("'");
            }
            sb.append(">");

            for (StyledSegment<String, String> seg : par.getStyledSegments()) {
                String style = seg.getStyle();
                String text = seg.getSegment();
                sb.append("<span style='")
                        .append(style != null ? style : "")
                        .append("'>")
                        .append(escapeHtml(text))
                        .append("</span>");
            }

            sb.append("</div>");
            if (p < area.getParagraphs().size() - 1) {
                sb.append("\n");
            }
        }

        sb.append(WRAPPER_END);
        return sb.toString();
    }

    // --- helpers ---

    private static String unwrap(String html) {
        String s = html.trim();
        if (s.startsWith(WRAPPER_START) && s.endsWith(WRAPPER_END)) {
            return s.substring(WRAPPER_START.length(), s.length() - WRAPPER_END.length());
        }
        return s;
    }

    private static String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private static String unescapeHtml(String text) {
        return text.replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'");
    }

    private record StyleRange(int start, int end, String style) { }
}
