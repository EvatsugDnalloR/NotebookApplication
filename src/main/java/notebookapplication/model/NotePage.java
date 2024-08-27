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

    public void insertContent(String content, int position) {
        String oldContent = this.content.toString();
        this.content.insert(position, content);
        support.firePropertyChange("content", oldContent, this.content.toString());
    }

    public void insertSymbol(Symbols symbolEnum, String symbol) {

    }
}
