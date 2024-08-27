package notebookapplication.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Abstract class that provides support for property change listeners.
 * This class is part of the Observer design pattern implementation.
 */
public abstract class ModelObserver {
    /** Support for managing property change listeners. */
    protected PropertyChangeSupport support;

    /**
     * Adds a property change listener to the listener list.
     *
     * @param listener  the PropertyChangeListener to be added
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    /**
     * Removes a property change listener from the listener list.
     *
     * @param listener  the PropertyChangeListener to be removed
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
}
