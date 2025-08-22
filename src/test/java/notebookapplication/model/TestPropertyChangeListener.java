package notebookapplication.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/** Helper class to capture all received property change events.  */
public class TestPropertyChangeListener implements PropertyChangeListener {
    private final List<PropertyChangeEvent> events = new ArrayList<>();

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        this.events.add(evt);
    }

    public List<String> getEventTypes() {
        return events.stream()
                .map(PropertyChangeEvent::getPropertyName)
                .collect(Collectors.toList());
    }

    public void reset() {
        events.clear();
    }

    public boolean containsEventType(String eventType) {
        return events.stream()
                .anyMatch(e -> eventType.equals(e.getPropertyName()));
    }
}
