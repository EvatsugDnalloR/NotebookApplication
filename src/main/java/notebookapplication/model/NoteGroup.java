package notebookapplication.model;

import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

public class NoteGroup extends ModelObserver{
    private String groupName;
    private List<NotePage> notePages;

    NoteGroup() {
        //...
        notePages = new ArrayList<NotePage>();
        support = new PropertyChangeSupport(this);
    }

    public void addPage(NotePage notePage) {
        notePages.add(notePage);
    }

    public void deletePage(NotePage notePage) {
        notePages.remove(notePage);
    }

    public void changePagesOrder(NotePage notePage) {
        //...
    }
}
