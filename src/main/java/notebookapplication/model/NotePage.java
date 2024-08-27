package notebookapplication.model;

import java.beans.PropertyChangeSupport;

public class NotePage extends ModelObserver{
    private String pageName;
    private StringBuilder content;

    NotePage(String pageName, String content){
        this.pageName = pageName;
        this.content = new StringBuilder(content);
        support = new PropertyChangeSupport(this);
    }

    public void insertContent(int caretPosition, String content) {
        String oldContent = this.content.toString();
        this.content.insert(caretPosition, content);
        support.firePropertyChange("content", oldContent, this.content.toString());
    }

    public void deleteContent(int startPosition, int endPosition) {
        String oldContent = this.content.toString();
        this.content.delete(startPosition, endPosition);
        support.firePropertyChange("content", oldContent, this.content.toString());
    }

    public void replaceContent(String newContent, int startPosition, int endPosition) {
        deleteContent(startPosition, endPosition);
        insertContent(startPosition, newContent);
    }

    public void insertSymbol(int lineNum, Symbols symbolEnum) {
        String oldContent = this.content.toString();
        String[] lines = content.toString().split("\n");

        if (lineNum < 0 || lineNum >= lines.length) {
            throw new IllegalArgumentException(STR."Line number \{lineNum} is out of bounds");
        }
        lines[lineNum] = STR."\{symbolEnum.symbol}  \{lines[lineNum]}";

        content = new StringBuilder(String.join("\n", lines));
        support.firePropertyChange("content", oldContent, content.toString());
    }

    public StringBuilder getContent() {
        return content;
    }

    public void setContent(StringBuilder content) {
        this.content = content;
    }

    public String getPageName() {
        return pageName;
    }

    public void setPageName(String pageName) {
        this.pageName = pageName;
    }
}
