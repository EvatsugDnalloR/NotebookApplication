package notebookapplication.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

/**
 * Abstract base class providing property change support for model classes.
 * Implements the Observer pattern for UI synchronisation.
 *
 * <p>Implements {@link Serializable} so that subclasses ({@code NoteGroup},
 * {@code NotePage}) can be serialized and deserialized with their
 * {@code PropertyChangeSupport} intact.
 */
public abstract class NoteSubject implements Serializable {

    /** Support for managing property change listeners. */
    protected PropertyChangeSupport support;

    /**
     * Adds a property change listener to receive notifications about model changes.
     *
     * @param listener the PropertyChangeListener to be added
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    /**
     * Removes a property change listener from receiving notifications.
     *
     * @param listener the PropertyChangeListener to be removed
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
}