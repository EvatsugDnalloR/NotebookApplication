/**
 * The GUI module for the JavaFX NotebookApplication.
 *
 * <p>This module contains the JavaFX-based user interface components that provide
 * visual representation and interaction with the notebook model.
 * It implements the Observer pattern to synchronise with model changes.</p>
 *
 * <h2>Key Components:</h2>
 * <ul>
 *   <li><b>MainFrame</b> - Main application class that initialises the JavaFX stage</li>
 *   <li><b>Controller</b> - Central controller that coordinates between model and view components</li>
 *   <li><b>GroupBar</b> - Custom UI component for managing and displaying note groups as tabs</li>
 *   <li><b>PageBar</b> - Custom UI component for managing and displaying note pages as tabs</li>
 * </ul>
 *
 * <p>The module provides a responsive interface for creating, editing, organising,
 * and navigating between notes and note groups. It handles user interactions and
 * translates them into model operations through the facade.</p>
 *
 * <p>The UI is designed with a main content area for note editing, a horizontal
 * group bar at the top, and a vertical page bar on the side for navigation,
 * as meant for OneNote-like UI.</p>
 */
package notebookapplication.gui;